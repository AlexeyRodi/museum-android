package com.example.project1.museumroom

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class MuseumRoomViewModelFactory(private val museumRoomRepository: FakeMuseumRoomRepository): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MuseumRoomViewModel::class.java)){
            return MuseumRoomViewModel(museumRoomRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}