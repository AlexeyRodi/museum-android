package com.example.project1.museumroom

import com.example.project1.exhibit.Exhibit
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface MuseumRoomRepository {
    @GET("api/rooms")
    fun getMuseumRoom(): Call<List<MuseumRoom>>

    @GET("api/exhibits/by-room/{room_number}")
    fun getExhibitsByRoomNumber(@Path("room_number") roomNumber: Int): Call<List<Exhibit>>
}