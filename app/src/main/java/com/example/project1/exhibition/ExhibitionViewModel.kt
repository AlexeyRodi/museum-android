package com.example.project1.exhibition

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class ExhibitionViewModel(private val exhibitionRepository: ExhibitionRepository): ViewModel() {
    private val _exhibitionNames = MutableLiveData<List<String>>()
    val exhibitionNames: LiveData<List<String>> get() = _exhibitionNames


    fun loadExhibitionsNames(){
        viewModelScope.launch {
            try {
                val exhibition = exhibitionRepository.getExhibition()
                _exhibitionNames.value = exhibition.map { it.name }
            } catch (e: Exception){
                _exhibitionNames.value = emptyList()
            }
        }
    }

}