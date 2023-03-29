package com.expertuniversal.vipen.data

data class ViPenData(
    val viPenUserData: ViPenUserData,
    val viPen2Spector: ViPen2Spector
)

data class ViPen2Spector(
    val spectorData: List<Float>,
    val spectorLength: UInt,
    val signalData: List<Float>,
    val signalLength: UInt,
)

data class ViPenUserData(
    val temperature: Float,
    val excess: Float,
    val vibrationAcceleration: Float,
    val vibrationVelocity: Float
)
