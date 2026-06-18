package com.example.rma_premiere

import android.app.Application
import android.util.Log
import com.example.rma_premiere.data.local.db.appContext
import com.example.rma_premiere.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin

class ShowtimeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
        try {
            if (GlobalContext.getOrNull() == null) {
                startKoin {
                    androidContext(applicationContext)
                    modules(appModule)
                }
            }
        } catch (e: Exception) {
            Log.e("ShowtimeApp", "Koin init failed", e)
        }
    }
}
