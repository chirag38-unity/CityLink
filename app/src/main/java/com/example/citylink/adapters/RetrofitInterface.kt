package com.example.citylink.adapters

import com.example.citylink.dataClasses.BusLiveLocation
import com.example.citylink.dataClasses.BusRoute
import com.example.citylink.dataClasses.BusStops
import com.example.citylink.dataClasses.PingResult
import com.example.citylink.dataClasses.RoadBlocks
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface RetrofitInterface {

    @POST("/print")
    fun executePing(@Body map: HashMap<String, Any>): Call<PingResult>

    @GET("/secure/busstops")
    suspend fun getBusStops(): Response<List<BusStops>>

    @GET("/secure/roadblocks")
    suspend fun getRoadBlocks(): Response<List<RoadBlocks>>

    @GET("/secure/busRoutes")
    suspend fun getBusRoutes() : Response<List<BusRoute>>

//    @GET("/busLiveLocation/{id}")
//    suspend fun getbusLiveLocation(@Path("id") id : String) : Response<BusLiveLocation>

    @POST("/secure/addalert")
    fun sendAlert(@Body map: HashMap<String, Any>): Call<PingResult>

//    @POST("/startService")
//    fun startService(@Body map : HashMap<String, Any> ) : Response<PingResult>
//
//    @POST("/endService/{userID}")
//    fun endService(@Path("userID") userID : String, @Body map : HashMap<String, Any> ) : Response<PingResult>
//
//    @POST("/updateLocation/{userID}")
//    fun updateLocation(@Path("userID") userID : String, @Body map : HashMap<String, Any> ) : Response<PingResult>

}