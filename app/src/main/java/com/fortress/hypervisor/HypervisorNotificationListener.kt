package com.fortress.hypervisor

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

class HypervisorNotificationListener : NotificationListenerService() {

    companion object {
        private const val TAG = "HypervisorNotification"
    }

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "Hypervisor Notification Listener created")
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.i(TAG, "Hypervisor Notification Listener connected")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        sbn?.let {
            Log.d(TAG, "Notification posted: ${it.packageName} - ${it.notification.tickerText}")
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        sbn?.let {
            Log.d(TAG, "Notification removed: ${it.packageName}")
        }
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.i(TAG, "Hypervisor Notification Listener disconnected")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "Hypervisor Notification Listener destroyed")
    }
}