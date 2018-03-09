package com.example.m.coreapplication

import android.app.Application
import com.example.m.coreapplication.data.ModulesManager
import kotlin.properties.Delegates

class CoreApp : Application() {

    val modulesManager: ModulesManager by lazy { ModulesManager(this) }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        var instance by Delegates.notNull<CoreApp>()
    }
}