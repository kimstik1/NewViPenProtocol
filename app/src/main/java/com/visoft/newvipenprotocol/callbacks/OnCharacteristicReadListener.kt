package com.visoft.newvipenprotocol.callbacks

interface OnCharacteristicReadListener {
    fun onCharacteristicReadListener(value: ByteArray, status: Int)
}