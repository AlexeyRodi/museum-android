package com.example.project1.exhibition

import java.util.Date

data class Exhibition(
    val name: String,
    val startDate: Date,
    val endDate: Date,
    val country: String,
    val venue: String,
    val responsiblePerson: String,
    val museum: Int
)
