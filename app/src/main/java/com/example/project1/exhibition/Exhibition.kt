package com.example.project1.exhibition

data class Exhibition(
    val exhibition_id: Int,
    val name: String,
    val start_date: String,
    val end_date: String,
    val country: String,
    val venue: String,
    val responsible_person: String,
    val museum: Int,
    val image: String?,
)
