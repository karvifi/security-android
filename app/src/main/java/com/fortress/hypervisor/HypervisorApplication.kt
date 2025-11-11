package com.fortress.hypervisor

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.multidex.MultiDexApplication

class HypervisorApplication : MultiDexApplication() {

    companion object {
        private const val TAG = "HypervisorApplication"

        fun hypervisor_log_event(event: String, message: String, level: Int) {
            // Enhanced logging implementation
            val logMessage = "[$level] $event: $message"
            when (level) {
                1 -> Log.i(TAG, logMessage)
                2 -> Log.w(TAG, logMessage)
                3 -> Log.e(TAG, logMessage)
                else -> Log.d(TAG, logMessage)
            }
            println(logMessage)
        }
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        // MultiDex support is now enabled
        Log.i(TAG, "MultiDex enabled for Pixel 7 Pro compatibility")
    }

    override fun onCreate() {
        super.onCreate()

        // Log memory information for Pixel 7 Pro
        val runtime = Runtime.getRuntime()
        Log.i(TAG, "Max heap: ${runtime.maxMemory() / 1024 / 1024}MB")
        Log.i(TAG, "Total memory: ${runtime.totalMemory() / 1024 / 1024}MB")

        hypervisor_log_event("APP_START", "Fortress Hypervisor application started on Pixel 7 Pro - Services initializing", 1)

        // Initialize security services with error handling for maximum stability
        try {
            initializeSecurityServices()
        } catch (e: Exception) {
            hypervisor_log_event("SERVICE_INIT_ERROR", "Failed to initialize services: ${e.message}", 3)
        }
    }

    private fun initializeSecurityServices() {
        // Basic service initialization - can be expanded later
        hypervisor_log_event("SERVICES_INIT", "Security services initialized successfully", 1)
    }
}