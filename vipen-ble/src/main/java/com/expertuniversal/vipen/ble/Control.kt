package com.expertuniversal.vipen.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.util.Log
import com.expertuniversal.vipen.callbacks.*
import com.expertuniversal.vipen.converter.DeviceData
import com.expertuniversal.vipen.converter.Converter
import com.expertuniversal.vipen.data.ViPen2Spector
import com.expertuniversal.vipen.data.ViPenData
import com.expertuniversal.vipen.data.ViPenFreshData
import kotlinx.coroutines.*
import no.nordicsemi.android.support.v18.scanner.*
import java.util.*

class Control(
    context: Context,
    private var onDataConvertedListener: OnDataConvertedListener
): OnConnectStateListener, OnServiceDiscoveredListener, OnCharacteristicReadListener, OnCharacteristicChangedListener, OnMtuChangedListener {

    private var characteristicCommands: CharacteristicFactory.CharacteristicCommands? = null
    private val freshData = ViPenFreshData()
    private var converter = Converter()

    private var limit: Int? = null

    private val scanCallback = object: ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
        }

        @SuppressLint("MissingPermission")
        override fun onBatchScanResults(results: MutableList<ScanResult>) {
            super.onBatchScanResults(results)
            results.map {
                val name = it?.device?.name

                if(name == VI_PEN2_DEVICE_NAME || name == VI_PEN_DEVICE_NAME) {
                    val scanner = BluetoothLeScannerCompat.getScanner()
                    scanner.stopScan(this)

                    characteristicCommands = CharacteristicFactory().getCharacteristicCommands(it.device.name)

                    CoroutineScope(Dispatchers.IO).launch {
                        connect(it.device)
                    }
                }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            onDataConvertedListener.onError(errorCode)
        }
    }

    private val gattCallback: CustomGattCallback.Controller = CustomGattCallback(
        context, this, this, this, this, this
    ).Controller()

    fun init() {
        startScan()
    }

    private fun startScan() {
        val scanner = BluetoothLeScannerCompat.getScanner()
        val settings: ScanSettings = ScanSettings.Builder()
                .setLegacy(false)
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .setReportDelay(5000)
                .setUseHardwareBatchingIfSupported(true)
                .build()

        val filters: MutableList<ScanFilter> = ArrayList()
        scanner.startScan(filters, settings, scanCallback)
    }

    private suspend fun connect(device: BluetoothDevice) = gattCallback.connectAsync(device)

    private suspend fun discoverService() = gattCallback.discoverServicesAsync()

    private suspend fun requestMtu() = gattCallback.requestMtu()

    private suspend fun downloadFirstData() = gattCallback.apply {
        val startChar = characteristicCommands!!.getStandardWriteCharacteristic()
        writeCharacteristic(startChar.serviceUUID, startChar.characteristicUUID, startChar.data)
        delay(1000)
        val readChar = characteristicCommands!!.getReadCharacteristic()
        readCharacteristic(readChar.serviceUUID, readChar.characteristicUUID)
    }

    private suspend fun downloadSpector() = gattCallback.apply {
        startIndicate()
        delay(200)
        characteristicCommands!!.getWaveStartCharacteristic().let {
            writeCharacteristic(it.serviceUUID, it.characteristicUUID, it.data)
        }
    }


    private suspend fun requestDeviceDataStatus() {
        delay(1000)
        gattCallback.requestDeviceDataStatus()
    }

    private suspend fun stopMeasuringSpector() {
        characteristicCommands!!.getStopCharacteristic().let {
            gattCallback.writeCharacteristic(it.serviceUUID, it.characteristicUUID, it.data)
        }
        delay(1000)
        characteristicCommands!!.getSpecialStartCharacteristic().let {
            gattCallback.writeCharacteristic(it.serviceUUID, it.characteristicUUID, it.data)
        }
        delay(1000)
        characteristicCommands!!.getReadCharacteristic().let {
            gattCallback.readCharacteristic(it.serviceUUID, it.characteristicUUID)
        }
    }

    private suspend fun stopMeasuring() {
        characteristicCommands!!.getStopCharacteristic().let {
            gattCallback.writeCharacteristic(it.serviceUUID, it.characteristicUUID, it.data)
        }
        val spector = converter.convertWave(freshData.headerSpectorArray, freshData.spectorArray)
        val signal = converter.convertWave(freshData.headerSignalArray, freshData.signalArray)

        val data = ViPenData(
            viPenUserData = converter.convertDefaultValue(
                DeviceData(characteristicCommands!!.getDeviceName() ,freshData.defaultArray)
            ),
            viPen2Spector = ViPen2Spector(
                spectorData = spector.first,
                spectorLength = spector.second,
                signalData = signal.first,
                signalLength = signal.second,
            ),
        )
        onDataConvertedListener.receiveViPen2Data(data)

        disconnect()
    }


    fun disconnect() = CoroutineScope(Dispatchers.IO).launch {
        gattCallback.closeConnection()
        freshData.refresh()
        characteristicCommands = null
        limit = null
    }

    override fun serviceDiscovered(status: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            requestMtu()
        }
    }

    override fun changeConnectStatus(status: Int, newStatus: Int) {
        when(newStatus) {
            BluetoothProfile.STATE_CONNECTED     -> {
                CoroutineScope(Dispatchers.IO).launch {
                    discoverService()
                }
            }
            BluetoothProfile.STATE_DISCONNECTING -> disconnect()
            BluetoothProfile.STATE_DISCONNECTED  -> disconnect()
        }
        when(status) {
            19 -> disconnect()
        }
    }

    override fun onCharacteristicReadListener(value: ByteArray, status: Int) {
        val scope = CoroutineScope(Dispatchers.IO)

        if(value.size == 17) {

            if(freshData.isDefaultReceived){

                scope.launch {requestDeviceDataStatus()}

            }else if(!characteristicCommands!!.isWaveSupported()){

                val defaultData = converter.convertDefaultValue(DeviceData(characteristicCommands!!.getDeviceName(), value))

                onDataConvertedListener.receiveStandardData(defaultData)

            }else if(characteristicCommands!!.isWaveSupported()){
                freshData.isDefaultReceived = true
                freshData.defaultArray = value

                scope.launch { requestDeviceDataStatus() }
            }

        } else if(value.size == 2) {

            if((status and 2) == 0) {
                scope.launch {
                    downloadSpector()
                }
            } else {
                scope.launch {
                    requestDeviceDataStatus()
                }
            }

        }else Log.wtf("Unknown read received", "${value.size}")
    }

    override fun onCharacteristicChanged(characteristic: BluetoothGattCharacteristic, value: ByteArray) {
        if(!freshData.isHeaderSpectorReceived){

            freshData.headerSpectorArray = value
            freshData.isHeaderSpectorReceived = true
            limit = converter.convertHeader(value).viPen2_Data_Blocks - 1

        }else if(!freshData.isSpectorReceived){

            freshData.spectorArray.add(value)
            if(value[0].toInt() == limit){

                freshData.isSpectorReceived = true

                CoroutineScope(Dispatchers.IO).launch {
                    stopMeasuringSpector()
                }
            }
        } else if(!freshData.isHeaderSignalReceived){

            freshData.headerSignalArray = value
            freshData.isHeaderSignalReceived = true
            limit = converter.convertHeader(value).viPen2_Data_Blocks -1

        }else if(!freshData.isSignalReceived){
            freshData.signalArray.add(value)

            if(value[0].toInt() == limit){

                freshData.isSignalReceived = true

                CoroutineScope(Dispatchers.IO).launch {
                    stopMeasuring()
                }
            }
        }else Log.wtf("WTF", "Data.size = ${value.size}")

    }

    override fun onMtuChanged(mtu: Int, status: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            downloadFirstData()
        }
    }
}