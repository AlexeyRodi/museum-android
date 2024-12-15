package com.example.project1.exhibition

class FakeExhibitionRepository: ExhibitionRepository {
    override suspend fun getExhibition(): List<Exhibition> {
        return listOf(
            Exhibition("Exhibition1"),
            Exhibition("Exhibition2"),
            Exhibition("Exhibition3"),
            Exhibition("Exhibition3"),
        )
    }
}