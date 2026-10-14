package com.slem.gazastor

import android.app.Application
import com.slem.gazastor.core.di.AppModule

class GazaStorApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AppModule.init(this)
    }
}
