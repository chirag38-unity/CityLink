package com.example.citylink.ui.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.citylink.adapters.BusBtEmitReceiverManager
import com.example.citylink.adapters.ConnectionState
import com.example.citylink.dataClasses.BluetoothResources
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@HiltViewModel
class BluetoothServiceViewModel  @Inject constructor(
    private val busBtEmitReceiverManager: BusBtEmitReceiverManager
): ViewModel() {

    var initializingMessage = MutableLiveData<String?>(null)
        private set

    var errorMessage = MutableLiveData<String?>(null)
        private set

    var busId = MutableLiveData<String?>(null)
        private set

    var busDialog : Boolean = false
        private set

    var connectionState  = MutableLiveData<ConnectionState>(ConnectionState.Uninitialized)

    private fun subscribeToChanges(){
        viewModelScope.launch {
            busBtEmitReceiverManager.data.collect{ result ->
                when(result){
                    is BluetoothResources.Success -> {
                        connectionState.value = result.data.connectionState
                        busId.value = result.data.busCode
//                        busId.postValue(result.data.busCode)
                    }
                    is BluetoothResources.IntermConnect -> {
                        connectionState.value = ConnectionState.IntermConnect
                    }
                    is BluetoothResources.Loading -> {
                        initializingMessage.value = result.message
                        connectionState.value = ConnectionState.CurrentlyInitializing
                    }
                    is BluetoothResources.Error -> {
                        errorMessage.value = result.errorMessage
                        connectionState.value = ConnectionState.Uninitialized
                    }
                }
            }
        }
    }

    fun disconnect(){
        busBtEmitReceiverManager.disconnect()
    }

    fun reconnect(){
        busBtEmitReceiverManager.reconnect()
    }

    fun initializeConnection(){
        errorMessage.value = null
        subscribeToChanges()
        busBtEmitReceiverManager.startReceiving()
    }

    override fun onCleared() {
        super.onCleared()
        busBtEmitReceiverManager.closeConnection()
    }

}