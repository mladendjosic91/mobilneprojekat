package com.example.rma_premiere

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.rma_premiere.data.local.db.appContext
import com.example.rma_premiere.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        appContext = applicationContext

        startKoin {
            androidContext(applicationContext)
            modules(appModule)
        }

        setContent {
            App()
        }
    }
}
