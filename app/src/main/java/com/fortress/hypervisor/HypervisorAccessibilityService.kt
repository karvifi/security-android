package com.fortress.hypervisor

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.util.Log

class HypervisorAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "HypervisorAccessibility"
    }

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "Hypervisor Accessibility Service created")
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.i(TAG, "Hypervisor Accessibility Service connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // For stability, we'll log events without complex processing
        event?.let {
            Log.d(TAG, "Accessibility event: ${event.eventType} on ${event.packageName}")
        }
    }

    override fun onInterrupt() {
        Log.w(TAG, "Hypervisor Accessibility Service interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "Hypervisor Accessibility Service destroyed")
    }
}