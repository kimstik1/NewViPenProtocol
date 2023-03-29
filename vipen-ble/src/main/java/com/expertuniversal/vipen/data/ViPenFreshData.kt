package com.expertuniversal.vipen.data

data class ViPenFreshData(
    var isSpectorReceived: Boolean = false,
    var isHeaderSpectorReceived: Boolean = false,
    var isSignalReceived: Boolean = false,
    var isHeaderSignalReceived: Boolean = false,
    var isDefaultReceived: Boolean = false,

    var spectorArray: MutableList<ByteArray> = mutableListOf(),
    var headerSpectorArray: ByteArray = byteArrayOf(),
    var signalArray: MutableList<ByteArray> = mutableListOf(),
    var headerSignalArray: ByteArray = byteArrayOf(),
    var defaultArray: ByteArray = byteArrayOf(),
){
    fun refresh(){
        isSpectorReceived = false
        isHeaderSpectorReceived = false
        isSignalReceived = false
        isHeaderSignalReceived = false
        isDefaultReceived = false
        spectorArray = mutableListOf()
        headerSpectorArray = byteArrayOf()
        signalArray = mutableListOf()
        headerSignalArray = byteArrayOf()
        defaultArray = byteArrayOf()
    }
}