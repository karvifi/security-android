package com.fortress.hypervisor

import android.net.VpnService
import android.content.Intent
import android.os.ParcelFileDescriptor
import android.util.Log

class HypervisorVpnService : VpnService() {

    companion object {
        private const val TAG = "HypervisorVpnService"
    }

    private var vpnInterface: ParcelFileDescriptor? = null

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "Hypervisor VPN Service created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(TAG, "Hypervisor VPN Service started")

        // For stability, we'll create a basic VPN interface without complex packet processing
        try {
            val builder = Builder()
                .setSession("Fortress Hypervisor VPN")
                .addAddress("192.168.0.1", 24)
                .addRoute("0.0.0.0", 0)

            vpnInterface = builder.establish()
            Log.i(TAG, "VPN interface established successfully")

        } catch (e: Exception) {
            Log.e(TAG, "Failed to establish VPN interface", e)
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        vpnInterface?.close()
        Log.i(TAG, "Hypervisor VPN Service destroyed")
    }
}