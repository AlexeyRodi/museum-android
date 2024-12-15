package com.example.project1.museumroom

import retrofit2.http.GET

interface MuseumRoomRepository {
    @GET("api/rooms")
    suspend fun getMuseumRoom(): List<MuseumRoom>
}