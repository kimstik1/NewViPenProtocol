package com.expertuniversal.vipen.callbacks

interface OnCharacteristicReadListener {
    fun onCharacteristicReadListener(value: ByteArray, status: Int)
}