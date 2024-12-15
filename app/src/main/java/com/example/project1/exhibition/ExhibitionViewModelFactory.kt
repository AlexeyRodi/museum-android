package com.example.project1.exhibition

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ExhibitionViewModelFactory(private val exhibitionRepository: FakeExhibitionRepository): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExhibitionViewModel::class.java)) {
            return ExhibitionViewModel(exhibitionRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}