package com.visoft.newvipenprotocol.data

data class StVPen2Header(
    val viPen2_Get_Data_Command: Int,
    val viPen2_Get_Data_Block: Int,
    val viPen2_Get_Wave_ID: Int,
    val viPen2_Data_Blocks: Int,
    val timestamp: UInt,
    val coeff: Float,
    val dataType: UInt,
    val dataUnits: UInt,
    val dataLen: UInt,
    val dataDX: Float,
    val spectrumAvg: Int,
    val spectrumAvgMax: Int,
)

data class StVPen2Data(
    val stVPen2Header: StVPen2Header,
    val blocks: MutableList<StVPen2Block>
)

data class StVPen2Block(
    val ViPenDataBlock: Int,
    val ViPenWaveID: Int,
    val data: List<Short>
)

