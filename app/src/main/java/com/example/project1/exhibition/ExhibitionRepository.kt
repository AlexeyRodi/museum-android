package com.example.project1.exhibition

import retrofit2.http.GET

interface ExhibitionRepository {
    @GET("api/exhibitions")
    suspend fun getExhibitions(): List<Exhibition>
}