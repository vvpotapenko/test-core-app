package com.example.m.coreapplication.data.device.impl

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.example.m.coreapplication.data.BaseRx
import com.example.m.coreapplication.data.device.IConnectedDeviceListener
import com.example.m.coreapplication.data.device.IConnectedDeviceService
import com.example.m.deviceservice.IDeviceService
import io.reactivex.Single

class ConnectedDeviceService(private val context: Context) : BaseRx(), IConnectedDeviceService {

    private val listeners = mutableSetOf<IConnectedDeviceListener>()

    private var deviceService: IDeviceService? = null
    private var serviceExists: Boolean = false

    override fun initialize() {
        val i = Intent("com.example.m.deviceservice.BIND")
                .setPackage("com.example.m.deviceservice")
        serviceExists = context.bindService(i, serviceConnection, Context.BIND_AUTO_CREATE)

        if (!serviceExists) {
            // There is no device module
            deviceConnected()
        }
    }

    override fun destroy() {
        context.unbindService(serviceConnection)
    }

    override fun getVersion(): Single<String> {
        val single = Single.create<String> {
            if (serviceExists) {
                try {
                    val service = deviceService
                    if (service != null) {
                        it.onSuccess(service.version)
                    } else {
                        it.onError(Exception("There is no device here"))
                    }
                } catch (e: Exception) {
                    it.onError(e)
                }
            } else {
                it.onSuccess("Нет модуля устройства")
            }
        }
        return prepareRx(single)
    }

    override fun addListener(l: IConnectedDeviceListener) {
        listeners.add(l)
    }

    override fun removeListener(l: IConnectedDeviceListener) {
        listeners.remove(l)
    }

    private fun deviceConnected() {
        listeners.forEach { it.onDeviceConnected() }
    }

    private val serviceConnection by lazy {
        object : ServiceConnection {
            override fun onServiceDisconnected(componentName: ComponentName?) {
                deviceService = null
            }

            override fun onServiceConnected(componentName: ComponentName?, service: IBinder?) {
                deviceService = IDeviceService.Stub.asInterface(service)
                deviceConnected()
            }
        }
    }
}