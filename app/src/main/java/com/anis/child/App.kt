package com.anis.child

import android.app.Application
import com.anis.child.network.NetworkProvider

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        NetworkProvider.init(this)
    }
}
