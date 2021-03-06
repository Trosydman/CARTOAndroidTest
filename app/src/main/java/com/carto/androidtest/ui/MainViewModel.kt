package com.carto.androidtest.ui

import androidx.lifecycle.*
import com.carto.androidtest.BuildConfig
import com.carto.androidtest.domain.model.Poi
import com.carto.androidtest.repository.PoiRepository
import com.carto.androidtest.ui.MainEvents.MapEvents
import com.carto.androidtest.ui.MainEvents.PoisListEvents
import com.carto.androidtest.ui.MainStates.MapStates
import com.carto.androidtest.ui.MainStates.PoisListStates
import com.carto.androidtest.utils.LocationStatus
import com.carto.androidtest.utils.LocationStatusLiveData
import com.carto.androidtest.utils.Result
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject


const val CURRENT_FAKE_LOCATION_ID = "0"
const val CURRENT_FAKE_LOCATION_LAT = -33.3000802
const val CURRENT_FAKE_LOCATION_LNG = 149.0913524

@HiltViewModel
class MainViewModel @Inject constructor(
    repository: PoiRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val eventsChannel = Channel<MainEvents>()
    private val events = eventsChannel.receiveAsFlow()

    private val _states = MutableSharedFlow<MainStates>()
    val states = _states.asSharedFlow()

    val searchQuery = savedStateHandle.getLiveData("searchQuery", "")

    private val poisFlow = repository.getPois().map {
        when(it) {
            is Result.Loading -> TODO()
            is Result.Success -> {
                val poisList = it.data

                if (poisList.isEmpty()) {
                    // TODO as in Result.Error alternative
                }

                poisList.sortedBy { poi -> poi.title }
            }
            is Result.Error -> {
                // TODO

                emptyList()
            }
        }
    }
    val pois = poisFlow.asLiveData()

    val searchedPois = combine(
        poisFlow,
        searchQuery.asFlow()
    ) { pois, searchQuery ->
        if (searchQuery!= null && searchQuery.isNotEmpty()) {
            pois.filter {
                it.title.contains(
                    searchQuery,
                    ignoreCase = true
                )
            }
        } else {
            pois
        }
    }.asLiveData()

    lateinit var locationStatusLiveData: LocationStatusLiveData

    private val _selectedPoi = MutableLiveData<Poi?>()
    val selectedPoi: LiveData<Poi?>
        get() = _selectedPoi

    private val _originPoi = MutableLiveData<Poi?>()
    val originPoi: LiveData<Poi?>
        get() = _originPoi

    val destinationPoi: LiveData<Poi?>
        get() = _selectedPoi

    private var isPreparingRoute: Boolean = false
    private var isNavigating: Boolean = false

    init {
        viewModelScope.launch {
            events.collect {
                Timber.i("Event triggered => ${it::class.java.name}")

                when (it) {

                    is MapEvents -> {
                        when (it) {
                            is MapEvents.OnMapReady -> {
                                sendStateToUI(MapStates.ApplyInitialMapSetup)
                                sendStateToUI(
                                    MapStates.AddCurrentFakeLocationMarker(
                                        LatLng(
                                            CURRENT_FAKE_LOCATION_LAT,
                                            CURRENT_FAKE_LOCATION_LNG
                                        )
                                    )
                                )
                            }

                            is MapEvents.OnMarkerClicked -> {
                                if (isNavigating) {
                                    return@collect
                                }

                                val isLocationEnabled =
                                    locationStatusLiveData.value?.isLocationEnabled ?: false

                                if (isPreparingRoute) {
                                    if (!isLocationEnabled) {
                                        _originPoi.value = pois.value?.first { poi ->
                                            poi.id == it.relatedPoiId
                                        }
                                    } else {
                                        _selectedPoi.value = pois.value?.first { poi ->
                                            poi.id == it.relatedPoiId
                                        }
                                    }

                                    prepareRoute()
                                } else {
                                    _selectedPoi.value = pois.value?.first { poi ->
                                        poi.id == it.relatedPoiId
                                    }

                                    showPoiDetails()
                                }

                                sendStateToUI(
                                    MapStates.HighlightSelectedMarker(
                                        animateCamera = !isPreparingRoute
                                    )
                                )
                            }

                            is MapEvents.OnPoiDetailsHide -> {
                                sendStateToUI(MapStates.ShowFab(isPreparingRoute = isPreparingRoute))
                                if (isPreparingRoute.not()) {
                                    sendStateToUI(MapStates.ResetHighlightedMarker)
                                }
                            }

                            is MapEvents.OnCurrentLocationMarkerClicked -> {
                                if (isPreparingRoute.not()) {
                                    sendStateToUI(MapStates.HidePoiDetails)
                                    sendStateToUI(MapStates.HighlightCurrentLocation)
                                }
                            }

                            is MapEvents.OnFabClicked -> {
                                if (isPreparingRoute.not() && isNavigating.not()) {
                                    sendStateToUI(MapStates.HighlightCurrentLocation)
                                } else if (isPreparingRoute && isNavigating.not()) {
                                    startNavigation()
                                } else if (isNavigating) {
                                    stopNavigation()
                                }
                            }

                            is MapEvents.OnDirectionsFabClicked -> {
                                prepareRoute()
                            }

                            is MapEvents.OnRouteDetailsClosed -> {
                                closeRouteDetails()
                            }

                            is MapEvents.OnBackPressed -> {
                                when {

                                    isNavigating -> {
                                        stopNavigation()
                                    }

                                    isPreparingRoute -> {
                                        closeRouteDetails()
                                    }

                                    selectedPoi.value != null -> {
                                        _selectedPoi.value = null
                                        sendStateToUI(MapStates.HidePoiDetails)
                                    }

                                    else -> {
                                        sendStateToUI(MapStates.FinishApp)
                                    }
                                }
                            }

                            is MapEvents.OnSearchButtonClicked -> {
                                sendStateToUI(MapStates.OpenPoiList)
                            }


                            is MapEvents.OnHelpButtonClicked -> {
                                sendStateToUI(MapStates.ShowHelpMessage)
                            }

                            else -> {
                                if (BuildConfig.DEBUG) {
                                    throw IllegalStateException(
                                        "Unknown MapEvents instance: ${it::class.java.simpleName}"
                                    )
                                }
                            }
                        }
                    }

                    is PoisListEvents -> {
                        when (it) {
                            is PoisListEvents.OnCloseButtonClicked -> {
                                sendStateToUI(PoisListStates.PopBackStack)
                            }

                            is PoisListEvents.OnPoiItemClicked -> {
                                val poiId = it.poi.id
                                searchQuery.value = ""

                                sendStateToUI(MapStates.HighlightPoiMarker(poiId))
                                sendStateToUI(PoisListStates.PopBackStack)
                            }

                            is PoisListEvents.OnSearchFieldTextChanged -> {
                                searchQuery.value = it.query
                            }

                            else -> {
                                if (BuildConfig.DEBUG) {
                                    throw IllegalStateException(
                                        "Unknown PoisListEvents instance: "
                                                + it::class.java.simpleName
                                    )
                                }
                            }
                        }
                    }

                    else -> {
                        if (BuildConfig.DEBUG) {
                            throw IllegalStateException(
                                "Unknown MainEvents instance: ${it::class.java.simpleName}")
                        }
                    }
                }
            }
        }
    }

    private suspend fun closeRouteDetails() {
        resetRoute()

        showPoiDetails()
        sendStateToUI(MapStates.HighlightSelectedMarker())
        _selectedPoi.value = _originPoi.value
        _originPoi.value = null
    }

    fun onPermissionsResult(arePermissionsGranted: Boolean) = viewModelScope.launch {
        locationStatusLiveData.postValue(LocationStatus(arePermissionsGranted))
    }

    private suspend fun showPoiDetails() {
        val isLocationEnabled = locationStatusLiveData.value?.isLocationEnabled ?: false

        sendStateToUI(MapStates.HideCurrentLocationFab)

        if (isLocationEnabled) {
            sendStateToUI(MapStates.ShowDistanceToCurrentLocation)
            sendStateToUI(MapStates.ShowPoiDetails())
        } else {
            sendStateToUI(MapStates.ShowPoiDetails(_originPoi.value))
        }
    }

    private suspend fun prepareRoute() {
        isPreparingRoute = true
        val isLocationEnabled = locationStatusLiveData.value?.isLocationEnabled ?: false

        sendStateToUI(MapStates.HidePoiDetails)
        sendStateToUI(MapStates.SetRouteMarkers(isLocationEnabled))
        sendStateToUI(MapStates.ShowRouteDetails(isLocationEnabled))
        sendStateToUI(MapStates.CameraOnRoute(isLocationEnabled))
        sendStateToUI(MapStates.DrawRouteOnMap(isLocationEnabled))
    }

    private suspend fun resetRoute() {
        isPreparingRoute = false

        sendStateToUI(MapStates.CloseRouteDetails)
        sendStateToUI(MapStates.ResetRoute)
        sendStateToUI(MapStates.ResetHighlightedMarker)
        sendStateToUI(MapStates.ShowFab(isPreparingRoute))
    }

    private suspend fun startNavigation() {
        isNavigating = true

        sendStateToUI(MapStates.StartNavigation)
        sendStateToUI(MapStates.ShowFab(isNavigating = isNavigating))
    }

    private suspend fun stopNavigation() {
        isNavigating = false

        sendStateToUI(MapStates.StopNavigation)
        sendStateToUI(MapStates.ShowFab(isPreparingRoute = isPreparingRoute))
    }

    private suspend fun sendStateToUI(states: MainStates) = viewModelScope.launch {
        _states.emit(states)
    }
}
