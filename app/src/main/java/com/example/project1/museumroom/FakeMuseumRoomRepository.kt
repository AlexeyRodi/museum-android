package com.example.project1.museumroom

class FakeMuseumRoomRepository: MuseumRoomRepository {
    override suspend fun getMuseumRoom(): List<MuseumRoom> {
        return listOf(
            MuseumRoom("Number 1"),
            MuseumRoom("Number 2"),
            MuseumRoom("Number 3"),
        )
    }
}