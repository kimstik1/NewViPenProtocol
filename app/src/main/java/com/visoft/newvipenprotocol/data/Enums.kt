package com.visoft.newvipenprotocol.data

enum class VIPEN2_BT_COMMAND(val value: UInt){
    NONE(0u),
    START(1u),
    STOP(2u),
    IDLE(3u),
    OFF(4u),
    CALIBRATION_0(0x10u),
    CALIBRATION_20(0x11u),
    CALIBRATION_1KHZ(0x20u),
    CALIBRATION_SAVE(0x30u),
    TEST(0x80u),
    SET_DATA(0x40u),
}

enum class MEAS_TYPE(val value: UInt){
    SPECTRUM(0u),
    WAVEFORM(1u),
    SPECTRUM_SLOW(2u),
    WAVEFORM_SLOW(3u),
    SPECTRUM_ENV(4u),
    WAVEFORM_ENV(5u)
}

enum class MEAS_UNITS(val value: UInt){
    ACCELERATION(0u),
    VELOCITY(1u),
    DISPLACEMENT(2u),
}

enum class SETUP_DX(val value: UInt){
    DX_256_HZ (0u),
    DX_640_HZ (1u),
    DX_2560_HZ (2u),
    DX_6400_HZ (3u),
    DX_25600_HZ (4u),
}

enum class SETUP_ALLX(val value: UInt){
    ALLX_256(0u),
    ALLX_1K(1u),
    ALLX_2K(2u),
    ALLX_8K(3u),
}

enum class SETUP_AVG(val value: UInt){
    AVG_NO(0u),
    AVG_4_STOP(1u),
    AVG_10_STOP(2u),
    AVG_999(3u),
}