package com.example.citylink.adapters

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import com.example.citylink.dataClasses.BluetoothResources
import com.example.citylink.dataClasses.BusBtEmit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject

@SuppressLint("MissingPermission")
class BusBtEmitReceiveManager @Inject constructor(
    private val bluetoothAdapter: BluetoothAdapter,
    private val context: Context
) : BusBtEmitReceiverManager {

    private val BT_DEVICE_NAME = "BLE Device"
    private val BUS_ID_SERVICE_UUID = "00001010-0000-1000-8000-00805f9b34fb"
    private val BUS_ID_CHARACTERISTICS_UUID = "00001212-0000-1000-8000-00805f9b34fb"

    private val bleScanner by lazy {
        bluetoothAdapter.bluetoothLeScanner
    }

    private val scanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()

    private val coroutine = CoroutineScope(Dispatchers.Default)

    private var gatt : BluetoothGatt? = null
    private var isScanning = false

    override val data: MutableSharedFlow<BluetoothResources<BusBtEmit>> = MutableSharedFlow()

    //Scan & Gatt Callbacks-------------------------------------------------------------------------

    private val scanCallback = object : ScanCallback(){

        override fun onScanResult(callbackType: Int, result: ScanResult) {
//            Timber.tag("BLEReceiveManager").d(result.toString())
            if(result.device.name == BT_DEVICE_NAME){
                coroutine.launch {
                    data.emit(BluetoothResources.Loading(message = "Connecting to bus..."))
                }
                if(isScanning){
                    result.device.connectGatt(context, false, gattCallBack, BluetoothDevice.TRANSPORT_LE)
                    isScanning = false
                    bleScanner.stopScan(this)
                }
            }
        }

    }

//    private var currentConnectionAttempt = 1 //Connection Attempts-----------------------------------
//    private val MAX_CONNECTION_ATTEMPTS = 5

    private val gattCallBack = object : BluetoothGattCallback(){
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if(status == BluetoothGatt.GATT_SUCCESS){
                if(newState == BluetoothProfile.STATE_CONNECTED){
                    coroutine.launch {
                        data.emit(BluetoothResources.Loading(message = "Discovering services..."))
                    }
                    gatt.discoverServices()
                    this@BusBtEmitReceiveManager.gatt = gatt
                } else if(newState == BluetoothProfile.STATE_DISCONNECTED){
                    coroutine.launch {
                        data.emit(BluetoothResources.Success(data = BusBtEmit("0", ConnectionState.Disconnected)))
                    }
                    gatt.close()
                }
            }else{
                gatt.close()
//                currentConnectionAttempt+=1
//                coroutine.launch {
//                    data.emit(BluetoothResources.Loading(message = "Attempting to connect $currentConnectionAttempt/$MAX_CONNECTION_ATTEMPTS..."))
//                }
//                if(currentConnectionAttempt <= MAX_CONNECTION_ATTEMPTS){
//                    startReceiving()
//                }else{
//                    coroutine.launch {
//                        data.emit(BluetoothResources.Error(errorMessage = "Could not find the Bus you are boarding"))
//                    }
//                }
                startReceiving()
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            with(gatt){
                printGattTable()
                coroutine.launch {
                    data.emit(BluetoothResources.IntermConnect("Bus Found Nearby"))
                }
//                gatt.requestMtu(517)
                gatt.readRemoteRssi()
            }
        }

        override fun onReadRemoteRssi(gatt: BluetoothGatt?, rssi: Int, status: Int) {
            if(status == BluetoothGatt.GATT_SUCCESS){
                coroutine.launch {
                    data.emit(BluetoothResources.IntermConnect(message = "Reading RSSI value..."))
                }
                Timber.tag("RSSI").d("RSSI value -> $rssi")
                if( rssi > -35){
                    Timber.tag("RSSI").d("RSSI value threshold reached:")
                    gatt?.requestMtu(517)
                }
            }
        }

        override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
            val characteristic = findCharacteristics(BUS_ID_SERVICE_UUID, BUS_ID_CHARACTERISTICS_UUID)
            if(characteristic == null){
                coroutine.launch {
                    data.emit(BluetoothResources.Error(errorMessage = "Could not find busId publisher..."))
                }
                return
            }
            enableNotification(characteristic)
        }

        @Deprecated("Deprecated in Java")
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            with(characteristic){
                when(uuid){
                    UUID.fromString(BUS_ID_CHARACTERISTICS_UUID) -> {
                        //XX XX XX XX XX XX

                        Timber.tag("BusID").d(value.toString())
                        val busId = String(value)
                        Timber.tag("BusID").d("busId -> ${busId}")
                        val busIdResult = BusBtEmit(
                            busId,
                            ConnectionState.Connected
                        )
                        coroutine.launch {
                            data.emit(
                                BluetoothResources.Success(data = busIdResult)
                            )
                        }
                    }
                    else -> Unit
                }
            }
        }


    }

    //Characteristics functions---------------------------------------------------------------------

    private fun enableNotification(characteristic: BluetoothGattCharacteristic){
        val ccdUUID = UUID.fromString(CCCD_DESCRIPTOR_UUID)
        val payload = when {
            characteristic.isIndicatable() -> BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
            characteristic.isNotifiable() -> BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            else -> return
        }
        characteristic.getDescriptor(ccdUUID)?.let { cccdDescriptor ->
            if(gatt?.setCharacteristicNotification(characteristic, true) == false){
                Timber.tag("BLEReceiveManager").d("set characteristics notification failed")
                return
            }
            writeDescription(cccdDescriptor, payload)
        }
    }

    private fun writeDescription(descriptor: BluetoothGattDescriptor, payload: ByteArray){
        gatt?.let { gatt ->
            descriptor.value = payload
            gatt.writeDescriptor(descriptor)
        } ?: error("Not connected to a BLE device!")
    }

    private fun findCharacteristics(serviceUUID: String, characteristicsUUID:String): BluetoothGattCharacteristic?{
        return gatt?.services?.find { service ->
            service.uuid.toString() == serviceUUID
        }?.characteristics?.find { characteristics ->
            characteristics.uuid.toString() == characteristicsUUID
        }
    }

    //Interface Function Implementations------------------------------------------------------------

    override fun startReceiving() {
        coroutine.launch {
            data.emit(BluetoothResources.Loading(message = "Scanning Bus devices..."))
        }
        isScanning = true
        bleScanner.startScan(null,scanSettings,scanCallback)
    }

    override fun reconnect() {
        gatt?.connect()
    }

    override fun disconnect() {
        gatt?.disconnect()
    }

    override fun closeConnection() {
        bleScanner.stopScan(scanCallback)
        val characteristic = findCharacteristics(BUS_ID_SERVICE_UUID, BUS_ID_CHARACTERISTICS_UUID)
        if(characteristic != null){
            disconnectCharacteristic(characteristic)
        }
        gatt?.close()
    }

    private fun disconnectCharacteristic(characteristic: BluetoothGattCharacteristic){
        val cccdUuid = UUID.fromString(CCCD_DESCRIPTOR_UUID)
        characteristic.getDescriptor(cccdUuid)?.let { cccdDescriptor ->
            if(gatt?.setCharacteristicNotification(characteristic,false) == false){
                Timber.tag("BusIdReceiveManager").d("Set charateristics notification failed")
                return
            }
            writeDescription(cccdDescriptor, BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE)
        }
    }

}