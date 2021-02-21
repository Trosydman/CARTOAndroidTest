package com.carto.androidtest.ui

import androidx.lifecycle.*
import com.carto.androidtest.BuildConfig
import com.carto.androidtest.domain.model.Poi
import com.carto.androidtest.repository.PoiRepository
import com.carto.androidtest.ui.MainEvents.MapEvents
import com.carto.androidtest.ui.MainStates.MapStates
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

                poisList
            }
            is Result.Error -> {
                // TODO

                emptyList()
            }
        }
    }.asLiveData()

    private val _selectedPoi = MutableLiveData<Poi>()
    val selectedPoi: LiveData<Poi>
        get() = _selectedPoi

    init {
        viewModelScope.launch {
            events.collect {
                when (it) {

                    is MapEvents.OnMapReady -> {
                        sendStateToUI(MapStates.ApplyInitialMapSetup)
                        sendStateToUI(MapStates.AddCurrentFakeLocationMarker(LatLng(
                            CURRENT_FAKE_LOCATION_LAT, CURRENT_FAKE_LOCATION_LNG
                        )))
                    }

                    is MapEvents.OnMarkerClicked -> {
                        _selectedPoi.value = pois.value?.first{ poi ->
                            poi.id == it.relatedPoiId
                        }
                        sendStateToUI(MapStates.HideCurrentLocationFab)
                    }

                    is MapEvents.OnPoiDetailsHide ->{
                        sendStateToUI(MapStates.ShowCurrentLocationFab)
                        sendStateToUI(MapStates.ResetHighlightedMarker)
                    }

                    is MapEvents.OnCurrentLocationMarkerClicked -> {
                        sendStateToUI(MapStates.HidePoiDetails)
                    }

                    else -> {
                        if (BuildConfig.DEBUG) {
                            throw IllegalStateException("Unknown state: ${it::class.java.simpleName}")
                        }
                    }
                }
            }
        }
    }

    private suspend fun sendStateToUI(states: MainStates) = viewModelScope.launch {
        statesChannel.send(states)
    }

}
