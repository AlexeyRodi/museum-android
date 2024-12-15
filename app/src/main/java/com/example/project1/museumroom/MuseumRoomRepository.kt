package com.example.project1.museumroom

interface MuseumRoomRepository {
    suspend fun getMuseumRoom(): List<MuseumRoom>
}