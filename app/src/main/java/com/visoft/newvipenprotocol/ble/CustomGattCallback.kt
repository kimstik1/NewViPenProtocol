package com.visoft.newvipenprotocol.ble

import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
import android.content.Context
import android.os.Build
import com.visoft.newvipenprotocol.callbacks.*
import com.visoft.newvipenprotocol.data.*
import kotlinx.coroutines.delay
import java.util.*

@SuppressLint("MissingPermission")
class CustomGattCallback(private val context: Context, private val onConnectStateListener: OnConnectStateListener, private val onServiceDiscoveredListener: OnServiceDiscoveredListener, private val onCharacteristicChangedListener: OnCharacteristicChangedListener, private val onCharacteristicReadListener: OnCharacteristicReadListener, private val onMtuChanged: OnMtuChangedListener): BluetoothGattCallback() {

    var gatt: BluetoothGatt? = null

    override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
        super.onConnectionStateChange(gatt, status, newState)
        onConnectStateListener.changeConnectStatus(status, newState)
    }

    override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
        super.onServicesDiscovered(gatt, status)
        onServiceDiscoveredListener.serviceDiscovered(status)
    }

    override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
        super.onMtuChanged(gatt, mtu, status)
        onMtuChanged.onMtuChanged(mtu, status)
    }

    override fun onCharacteristicRead(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
        super.onCharacteristicRead(gatt, characteristic, status)
        characteristic?.value?.let {
            onCharacteristicReadListener.onCharacteristicReadListener(status = status, value = it)
        }
    }

    override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
        super.onCharacteristicChanged(gatt, characteristic)
        characteristic?.let {
            onCharacteristicChangedListener.onCharacteristicChanged(it, it.value)
        }
    }

    override fun onCharacteristicRead(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, value: ByteArray, status: Int) {
        super.onCharacteristicRead(gatt, characteristic, value, status)
        onCharacteristicReadListener.onCharacteristicReadListener(value, status)
    }

    override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, value: ByteArray) {
        super.onCharacteristicChanged(gatt, characteristic, value)
        println("onCharacteristicChanged / Characteristic UUID: ${characteristic.uuid}")
        onCharacteristicChangedListener.onCharacteristicChanged(characteristic, value)
    }

    inner class Controller {

        suspend fun writeFirstCharacteristic(): Boolean {
            val service = isGattNotNull().getService(UUID.fromString(viPen2MainServiceUUID))
                ?: throw Exception("Service is not available") //413557AA-213F-4279-8530-D38E41390000
            val firstWrite = service.getCharacteristic(UUID.fromString(C_WRITE))
                ?: throw NullPointerException("Characteristic C_WRITE is not available") //42EC1288-B8A0-43DB-AE00-29F942ED0002

            val data = ViPen_Command_Data_START_MEASURING_WAVE

            return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                isGattNotNull().writeCharacteristic(firstWrite, data, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT) == BluetoothStatusCodes.SUCCESS
            } else {
                firstWrite.value = data
                firstWrite.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                isGattNotNull().writeCharacteristic(firstWrite)
            }
        }

        suspend fun readFirstCharacteristic(): Boolean {
            val service = isGattNotNull().getService(UUID.fromString(viPen2MainServiceUUID))
                ?: throw Exception("Service is not available") // 413557AA-213F-4279-8530-D38E41390000
            val firstRead = service.getCharacteristic(UUID.fromString(C_READ))
                ?: throw NullPointerException("Characteristic C_READ is not available") // 42EC1288-B8A0-43DB-AE00-29F942ED0001

            return isGattNotNull().readCharacteristic(firstRead)
        }

        suspend fun writeSecondCharacteristicSpector(): Boolean {
            val service = isGattNotNull().getService(UUID.fromString(viPen2MainServiceUUID))
                ?: throw Exception("Service is not available") // 413557AA-213F-4279-8530-D38E41390000
            val secondWrite = service.getCharacteristic(UUID.fromString(C_WAV_WRITE))
                ?: throw NullPointerException("Characteristic C_WAV_WRITE is not available") // 42EC1288-B8A0-43DB-AE00-29F942ED0003

            return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                isGattNotNull().writeCharacteristic(secondWrite, ViPen_Command_Data_START_SPECTOR, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT) == BluetoothStatusCodes.SUCCESS
            } else {
                secondWrite.value = ViPen_Command_Data_START_SPECTOR // byteArrayOf(0x10, 0)
                secondWrite.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                isGattNotNull().writeCharacteristic(secondWrite)
            }
        }

        suspend fun requestDeviceDataStatus(): Boolean {
            val service = isGattNotNull().getService(UUID.fromString(viPen2MainServiceUUID))
                ?: throw Exception("Service is not available") // 413557AA-213F-4279-8530-D38E41390000
            val firstRead = service.getCharacteristic(UUID.fromString(C_WRITE))
                ?: throw NullPointerException("Characteristic C_READ is not available") //42EC1288-B8A0-43DB-AE00-29F942ED0002

            return isGattNotNull().readCharacteristic(firstRead)
        }

        suspend fun startIndicate() {
            val service = isGattNotNull().getService(UUID.fromString(viPen2MainServiceUUID)) ?: throw Exception("Service is not available") // 413557AA-213F-4279-8530-D38E41390000
            val secondIndicate = service.getCharacteristic(UUID.fromString(C_WAV_INDICATE)) ?: throw NullPointerException("Characteristic C_WAV_INDICATE is not available") // 42EC1288-B8A0-43DB-AE00-29F942ED0004


            val CLIENT_CHARACTERISTIC_CONFIG: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
            val descriptor: BluetoothGattDescriptor = secondIndicate.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG)

            isGattNotNull().setCharacteristicNotification(secondIndicate, true)

            writeDescriptor(descriptor, ENABLE_INDICATION_VALUE)
        }

        suspend fun requestMtu() = isGattNotNull().requestMtu(512)

        suspend fun discoverServicesAsync() = isGattNotNull().discoverServices()

        private suspend fun writeDescriptor(descriptor: BluetoothGattDescriptor, payload: ByteArray): Boolean {
            isGattNotNull().apply {
                return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    writeDescriptor(descriptor, payload) == BluetoothStatusCodes.SUCCESS
                } else {
                    descriptor.value = payload
                    writeDescriptor(descriptor)
                }
            }
        }

        suspend fun connectAsync(device: BluetoothDevice): Boolean {
            gatt = device.connectGatt(context, false, this@CustomGattCallback, BluetoothDevice.TRANSPORT_LE)
            delay(100)
            return isGattNotNull().connect()
        }

        suspend fun stopMeasuring(): Boolean {
            val service = isGattNotNull().getService(UUID.fromString(viPen2MainServiceUUID))
                ?: throw Exception("Service is not available") //413557AA-213F-4279-8530-D38E41390000
            val secondWrite: BluetoothGattCharacteristic = service.getCharacteristic(UUID.fromString(C_WRITE))
                ?: throw NullPointerException("Characteristic C_WRITE is not available") //42EC1288-B8A0-43DB-AE00-29F942ED0002

            return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                isGattNotNull().writeCharacteristic(secondWrite, ViPen_Command_Data_STOP_MEASURING, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT) == BluetoothStatusCodes.SUCCESS
            } else {
                secondWrite.value = ViPen_Command_Data_STOP_MEASURING
                secondWrite.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                isGattNotNull().writeCharacteristic(secondWrite)
            }
        }

        private suspend fun isGattNotNull(): BluetoothGatt {
            return if(gatt == null) {
                throw Exception("BluetoothGatt is null")
            } else {
                gatt as BluetoothGatt
            }
        }

        fun closeConnection() {
            gatt?.disconnect()
            gatt?.close()
            gatt = null
        }
    }


    companion object {
        private val ViPen_Command_Data_START_MEASURING_SPECTOR = byteArrayOf(
            1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 3, 0, 0, 0, 4, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
        )

        private val ViPen_Command_Data_START_MEASURING_WAVE = byteArrayOf(
            1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 3, 0, 0, 0, 4, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
        )
        private val ViPen_Command_Data_START_SPECTOR = byteArrayOf(0x10, 0) // Starts measurement (presses the button)
        private val ViPen_Command_Data_STOP_MEASURING = byteArrayOf(2, 0) // Stops measurement (releases the button)
    }
}