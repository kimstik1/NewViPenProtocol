package com.expertuniversal.vipen.callbacks

import android.bluetooth.BluetoothGattCharacteristic

interface OnCharacteristicChangedListener {

    fun onCharacteristicChanged(characteristic: BluetoothGattCharacteristic, value: ByteArray)
}