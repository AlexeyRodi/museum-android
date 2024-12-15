package com.example.project1.exhibition

interface ExhibitionRepository {
    suspend fun getExhibition(): List<Exhibition>
}