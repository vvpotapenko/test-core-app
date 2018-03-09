package com.example.m.coreapplication.data.device

import io.reactivex.Single

interface IConnectedDeviceService {

    fun initialize()
    fun destroy()

    fun addListener(l: IConnectedDeviceListener)
    fun removeListener(l: IConnectedDeviceListener)
    fun getVersion(): Single<String>
}

interface IConnectedDeviceListener {

    fun onDeviceConnected()
}