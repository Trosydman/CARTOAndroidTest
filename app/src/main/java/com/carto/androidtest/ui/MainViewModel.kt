package com.carto.androidtest.ui

import androidx.lifecycle.*
import com.carto.androidtest.BuildConfig
import com.carto.androidtest.domain.model.Poi
import com.carto.androidtest.repository.PoiRepository
import com.carto.androidtest.ui.MainEvents.MapEvents
import com.carto.androidtest.ui.MainEvents.PoisListEvents
import com.carto.androidtest.ui.MainStates.MapStates
import com.carto.androidtest.ui.MainStates.PoisListStates
import com.carto.androidtest.utils.Result
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


const val CURRENT_FAKE_LOCATION_ID = "0"
const val CURRENT_FAKE_LOCATION_LAT = -33.3000802
const val CURRENT_FAKE_LOCATION_LNG = 149.0913524

@HiltViewModel
class MainViewModel @Inject constructor(repository: PoiRepository) : ViewModel() {

    val eventsChannel = Channel<MainEvents>()
    private val events = eventsChannel.receiveAsFlow()

    private val statesChannel = Channel<MainStates>()
    val states = statesChannel.receiveAsFlow()

    val pois = repository.getPois().map {
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
    }.asLiveData()

    private val _selectedPoi = MutableLiveData<Poi?>()
    val selectedPoi: LiveData<Poi?>
        get() = _selectedPoi

    private var isPreparingRoute: Boolean = false

    init {
        viewModelScope.launch {
            events.collect {
                when (it) {

                    is MapEvents -> {
                        when (it) {
                            is MapEvents.OnMapReady -> {
                                sendStateToUI(MapStates.ApplyInitialMapSetup)
                                sendStateToUI(MapStates.AddCurrentFakeLocationMarker(
                                    LatLng(
                                    CURRENT_FAKE_LOCATION_LAT, CURRENT_FAKE_LOCATION_LNG
                                )
                                ))
                            }

                            is MapEvents.OnMarkerClicked -> {
                                _selectedPoi.value = pois.value?.first { poi ->
                                    poi.id == it.relatedPoiId
                                }

                                if (isPreparingRoute) {
                                    prepareRoute()
                                } else {
                                    showPoiDetails()
                                }

                                sendStateToUI(MapStates.HighlightSelectedMarker(
                                    animateCamera = !isPreparingRoute)
                                )
                            }

                            is MapEvents.OnPoiDetailsHide -> {
                                sendStateToUI(MapStates.ShowFab(isPreparingRoute))
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

                            is MapEvents.OnCurrentLocationFabClicked -> {
                                if (isPreparingRoute.not()) {
                                    sendStateToUI(MapStates.HighlightCurrentLocation)
                                }
                            }

                            is MapEvents.OnDirectionsFabClicked -> {
                                prepareRoute()
                            }

                            is MapEvents.OnRouteDetailsClosed -> {
                                resetRoute()

                                showPoiDetails()
                                sendStateToUI(MapStates.HighlightSelectedMarker())
                            }

                            is MapEvents.OnBackPressed -> {
                                when {

                                    isPreparingRoute -> {
                                        resetRoute()

                                        showPoiDetails()
                                        sendStateToUI(MapStates.HighlightSelectedMarker())
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

                            else -> {
                                if (BuildConfig.DEBUG) {
                                    throw IllegalStateException(
                                        "Unknown MapEvents instance: ${it::class.java.simpleName}")
                                }
                            }
                        }
                    }

                    is PoisListEvents -> {
                        when (it) {
                            is PoisListEvents.OnCloseButtonClicked -> {
                                sendStateToUI(PoisListStates.PopBackStack)
                            }

                            else -> {
                                if (BuildConfig.DEBUG) {
                                    throw IllegalStateException(
                                        "Unknown PoisListEvents instance: ${it::class.java.simpleName}")
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

    private suspend fun showPoiDetails() {
        sendStateToUI(MapStates.HideCurrentLocationFab)
        sendStateToUI(MapStates.ShowPoiDetails)
        sendStateToUI(MapStates.ShowDistanceToCurrentLocation)
    }

    private suspend fun prepareRoute() {
        isPreparingRoute = true

        sendStateToUI(MapStates.HidePoiDetails)
        sendStateToUI(MapStates.ShowRouteDetails())
        sendStateToUI(MapStates.CameraOnRoute())
        sendStateToUI(MapStates.DrawRouteOnMap())
    }

    private suspend fun resetRoute() {
        isPreparingRoute = false

        sendStateToUI(MapStates.CloseRouteDetails)
        sendStateToUI(MapStates.ResetRoute)
        sendStateToUI(MapStates.ResetHighlightedMarker)
        sendStateToUI(MapStates.ShowFab(isPreparingRoute))
    }

    private suspend fun sendStateToUI(states: MainStates) = viewModelScope.launch {
        statesChannel.send(states)
    }
}
