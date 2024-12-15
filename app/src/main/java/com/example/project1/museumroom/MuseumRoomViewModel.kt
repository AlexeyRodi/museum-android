package com.example.project1.museumroom

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class MuseumRoomViewModel(private val museumRoomRepository: MuseumRoomRepository): ViewModel() {
    private val _museumRoomNumbers = MutableLiveData<List<String>>()
    val museumRoomNumbers: LiveData<List<String>> get() = _museumRoomNumbers


    fun loadExhibitionsNames(){
        viewModelScope.launch {
            try {
                val museumRoom = museumRoomRepository.getMuseumRoom()
                _museumRoomNumbers.value = museumRoom.map { it.roomNumber }
            } catch (e: Exception){
                _museumRoomNumbers.value = emptyList()
            }
        }
    }

}