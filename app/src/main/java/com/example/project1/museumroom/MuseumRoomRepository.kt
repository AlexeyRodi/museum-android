package com.example.project1.museumroom

import com.example.project1.exhibit.Exhibit
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface MuseumRoomRepository {
    @GET("api/rooms")
    fun getMuseumRoom(): Call<List<MuseumRoom>>

    @GET("api/rooms/{room_id}")
    fun getRoomDetails(@Path("room_id") roomId: Int): Call<MuseumRoom>

    @GET("api/rooms/{room_id}/exhibits")
    fun getExhibitsByRoomId(@Path("room_id") roomId: Int): Call<List<Exhibit>>
}