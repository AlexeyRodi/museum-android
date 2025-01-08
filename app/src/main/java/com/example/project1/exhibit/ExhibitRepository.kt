package com.example.project1.exhibit

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ExhibitRepository {
    @GET("api/exhibits")
    fun getExhibit(): Call<List<Exhibit>>

    @GET("api/exhibits/{exhibit_id}/")
    fun getExhibitDetails(@Path("exhibit_id") id: Int): Call<Exhibit>

    @PUT("api/exhibits/update/{exhibit_id}/")
    fun updateExhibit( @Path("exhibit_id") id: Int, @Body exhibit: Exhibit): Call<Exhibit>

    @POST("api/exhibits/add/")
    fun addExhibit(@Body exhibit: Exhibit): Call<Exhibit>

    @DELETE("api/exhibits/delete/{exhibit_id}/")
    fun deleteExhibit(@Path("exhibit_id") id: Int): Call<Void>
}