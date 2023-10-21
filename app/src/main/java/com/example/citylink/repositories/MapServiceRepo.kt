package com.example.citylink.repositories

import com.example.citylink.adapters.RetrofitInterface
import com.example.citylink.dataClasses.BusLiveLocation
import com.example.citylink.dataClasses.BusRoute
import com.example.citylink.dataClasses.BusStops
import com.example.citylink.dataClasses.RoadBlocks
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MapServiceRepo @Inject constructor(
     val retrofitInterface: RetrofitInterface
) {
    fun getBusStops() : Flow<List<BusStops>> = flow {
        val response =  retrofitInterface.getBusStops()
        if(response.isSuccessful){
            val data = response.body()!!
            emit(data)
        }else{
            Timber.e("FetchBusStops e:",response.errorBody())
        }
    }.flowOn(Dispatchers.IO)

    fun getBusRoutes() : Flow<List<BusRoute>> = flow{
        val response = retrofitInterface.getBusRoutes()
        if (response.isSuccessful) {
            val data = response.body()!!
            emit(data)
        }else {
            Timber.e("FetchBusRoutes e:",response.errorBody())
        }
    }

    fun getRoadBlocks() : Flow<List<RoadBlocks>> = flow {
        val response =  retrofitInterface.getRoadBlocks()
        if(response.isSuccessful){
            val data = response.body()!!
            emit(data)
        }else{
            Timber.e("FetchRoadBlocks e:",response.errorBody())
        }
    }.flowOn(Dispatchers.IO)

//    fun getBusLiveLocation(id : String) : Flow<BusLiveLocation> = flow {
//        val response = retrofitInterface.getbusLiveLocation(id)
//        if(response.isSuccessful){
//            val data = response.body()!!
//            emit(data)
//        }else{
//            Timber.e("FetchBusLiveLocation e:",response.errorBody())
//        }
//    }.flowOn(Dispatchers.IO)

}