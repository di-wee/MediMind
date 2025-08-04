package com.example.medimind.viewmodel

import androidx.lifecycle.*
import com.example.medimind.data.IntakeHistoryResponse
import com.example.medimind.network.ApiClient
import kotlinx.coroutines.launch

class IntakeHistoryViewModel : ViewModel() {

    private val _history = MutableLiveData<List<IntakeHistoryResponse>>()
    val history: LiveData<List<IntakeHistoryResponse>> = _history

    fun fetchHistory(patientId: String) {
        viewModelScope.launch {
            try {
                val response = ApiClient.retrofitService.getIntakeHistory(patientId) // ✅ fixed
                _history.value = response
            } catch (e: Exception) {
                // Handle error
                _history.value = emptyList()
            }
        }
    }
}
