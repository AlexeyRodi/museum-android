package com.example.project1.exhibition

data class Exhibition(
    val exhibition_id: Int?,
    val name: String,
    val start_date: String,
    val end_date: String,
    val responsible_person: String,
    val image: String? = null,
    val image_upload: String? = null,
)
