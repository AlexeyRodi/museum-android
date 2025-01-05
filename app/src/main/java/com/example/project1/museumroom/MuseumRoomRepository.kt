package com.example.project1.museumroom

import retrofit2.Call
import retrofit2.http.GET

interface MuseumRoomRepository {
    @GET("api/rooms")
    fun getMuseumRoom(): Call<List<MuseumRoom>>
}