package com.example.project1.exhibition

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ExhibitionRepository {
    @GET("api/exhibitions")
    fun getExhibitions(): Call<List<Exhibition>>

    @GET("api/exhibitions/{exhibition_id}/")
    fun getExhibitionDetail(@Path("exhibition_id") id: Int): Call<Exhibition>

    @PUT("api/exhibitions/update/{exhibition_id}/")
    fun updateExhibition( @Path("exhibition_id") id: Int, @Body exhibition: Exhibition): Call<Exhibition>

    @POST("api/exhibitions/add/")
    fun addExhibition(@Body exhibition: Exhibition): Call<Exhibition>
}