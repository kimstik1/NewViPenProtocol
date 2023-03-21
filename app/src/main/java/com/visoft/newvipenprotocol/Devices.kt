package com.visoft.newvipenprotocol

const val VI_PEN_DEVICE_NAME = "ViPen"
const val viPenMainServiceUUID = "378B4074-C2B8-45FF-894C-418739E60000"
const val viPenRead = "3890BE9F-3A5E-459D-B799-102365770001"
const val viPenWrite = "3890BE9F-3A5E-459D-B799-102365770002"

const val VI_PEN2_DEVICE_NAME = "ViP-2"
const val viPen2MainServiceUUID = "413557AA-213F-4279-8530-D38E41390000"
const val C_READ = "42EC1288-B8A0-43DB-AE00-29F942ED0001"
const val C_WRITE = "42EC1288-B8A0-43DB-AE00-29F942ED0002"
const val C_WAV_WRITE = "42EC1288-B8A0-43DB-AE00-29F942ED0003"
const val C_WAV_INDICATE = "42EC1288-B8A0-43DB-AE00-29F942ED0004"

const val ViPen_State_Stopped: Int = (0 shl 0)
const val ViPen_State_Started: Int = (1 shl 0)
const val ViPen_State_NoData: Int = (0 shl 1)
const val ViPen_State_Data: Int = (1 shl 1)