package com.example.project1.exhibit

import retrofit2.Call
import retrofit2.http.GET

interface ExhibitRepository {
    @GET("api/exhibits")
    fun getExhibit(): Call<List<Exhibit>>
}