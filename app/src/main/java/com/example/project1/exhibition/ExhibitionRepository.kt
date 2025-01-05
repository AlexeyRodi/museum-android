package com.example.project1.exhibition

import retrofit2.Call
import retrofit2.http.GET

interface ExhibitionRepository {
    @GET("api/exhibitions")
    fun getExhibitions(): Call<List<Exhibition>>
}