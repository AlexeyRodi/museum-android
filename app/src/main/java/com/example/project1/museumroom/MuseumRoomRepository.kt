package com.example.project1.museumroom

import com.example.project1.exhibit.Exhibit
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface MuseumRoomRepository {
    @GET("api/rooms")
    fun getMuseumRoom(): Call<List<MuseumRoom>>

    @GET("api/rooms/{room_id}")
    fun getRoomDetails(@Path("room_id") roomId: Int): Call<MuseumRoom>

    @GET("api/rooms/{room_id}/exhibits")
    fun getExhibitsByRoomId(@Path("room_id") roomId: Int): Call<List<Exhibit>>

    @POST("api/rooms/add/")
    fun addMuseumRoom(@Body room: MuseumRoom): Call<MuseumRoom>

    @PUT("api/rooms/update/{room_id}/")
    fun updateRoom( @Path("room_id") id: Int, @Body room: MuseumRoom): Call<MuseumRoom>

    @DELETE("api/rooms/delete/{room_id}/")
    fun deleteRoom(@Path("room_id") id: Int): Call<Void>
}