package com.example.project1.exhibit

data class Exhibit(
    val exhibit_id: Int,
    val name: String,
    val description: String,
    val creationYear: Int,
    val creator: String,
    val room: String,
    val image: String?
)
