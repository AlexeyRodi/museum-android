package com.example.project1.exhibit

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface ExhibitRepository {
    @GET("api/exhibits")
    fun getExhibit(): Call<List<Exhibit>>

    @GET("api/exhibits/{exhibit_id}/")
    fun getExhibitDetails(@Path("exhibit_id") id: Int): Call<Exhibit>
}