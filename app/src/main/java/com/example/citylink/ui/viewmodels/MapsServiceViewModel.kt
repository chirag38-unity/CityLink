package com.example.citylink.ui.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.citylink.dataClasses.BusLiveLocation
import com.example.citylink.dataClasses.BusRoute
import com.example.citylink.dataClasses.BusStops
import com.example.citylink.dataClasses.RoadBlocks
import com.example.citylink.repositories.MapServiceRepo
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import io.socket.client.Socket
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MapsServiceViewModel @Inject constructor(
    val mapServiceRepo: MapServiceRepo,
    val socket : Socket
) : ViewModel() {
    var busStopsLiveData : MutableLiveData<List<BusStops>> = MutableLiveData()
        private set

    var busRoutesLiveData : MutableLiveData<List<BusRoute>> = MutableLiveData()
        private set
    var roadBlockLiveData : MutableLiveData<List<RoadBlocks>> = MutableLiveData()
        private set

    var busRouteLiveData : MutableLiveData<List<LatLng>> = MutableLiveData()
        private set
    var bus1LocationLiveData : MutableLiveData<BusLiveLocation> = MutableLiveData()
        private set
    var bus2LocationLiveData : MutableLiveData<BusLiveLocation> = MutableLiveData()
        private set

    private val gson = Gson()

    fun getBusStopsLD() {
        viewModelScope.launch {
            mapServiceRepo.getBusStops()
                .catch { e->
                    Timber.tag("MapsServiceViewModel busStops error").d(e)
                }.buffer().collect{ response->
                    busStopsLiveData.value = response
                }
        }
    }

    fun getBusRoutesLD() {
        viewModelScope.launch {
            mapServiceRepo.getBusRoutes()
                .catch {e->
                    Timber.tag("MapsServiceViewModel busRoutes error").d(e)
                }.buffer().collect{
                    busRoutesLiveData.value = it
                }
        }
    }

    fun getRoadBlocksLD() {
        viewModelScope.launch {
            mapServiceRepo.getRoadBlocks()
                .catch { e->
                    Timber.d(e.message)
                }.buffer().collect{ response->
                    roadBlockLiveData.value = response
                }
        }
    }

    fun observeBusLiveLoc() {
        viewModelScope.launch {
            if(!socket.connected()){
                socket.connect()
            }
            socket.on("LiveBusData") { args->
                val bus1JsonObject = args[0].toString()
                val bus2JsonObject = args[1].toString()
                bus1LocationLiveData.postValue(gson.fromJson(bus1JsonObject, BusLiveLocation::class.java))
                bus2LocationLiveData.postValue(gson.fromJson(bus2JsonObject, BusLiveLocation::class.java))
            }
        }
    }

    fun removeBusLiveLocObserver() {
        viewModelScope.launch{
            socket.off("LiveBusData")
        }
    }

}