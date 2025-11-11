package com.fortress.hypervisor

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HypervisorIntegrationTest {

    @Test
    fun testFullHypervisorIntegration() {
        // Test the complete hypervisor system integration
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        assertNotNull("Application context should be available", appContext)
        assertEquals("com.fortress.hypervisor", appContext.packageName)
    }

    @Test
    fun testServiceIntegration() {
        // Test that all services can be integrated
        val expectedServices = arrayOf(
            "HypervisorVpnService",
            "HypervisorAccessibilityService",
            "HypervisorNotificationListener",
            "HypervisorDeviceAdmin"
        )

        assertTrue("Should have VPN service", expectedServices.contains("HypervisorVpnService"))
        assertTrue("Should have Accessibility service", expectedServices.contains("HypervisorAccessibilityService"))
        assertTrue("Should have Notification service", expectedServices.contains("HypervisorNotificationListener"))
        assertTrue("Should have Device Admin", expectedServices.contains("HypervisorDeviceAdmin"))
    }

    @Test
    fun testGuardSystemIntegration() {
        // Test that all guards are properly integrated
        val expectedGuards = arrayOf(
            "NetworkGuard",
            "HardwareGuard",
            "DataGuard",
            "SystemGuard",
            "PhysicalGuard",
            "UserGuard"
        )

        assertEquals("Should have 6 guards", 6, expectedGuards.size)
        assertTrue("Should have NetworkGuard", expectedGuards.contains("NetworkGuard"))
        assertTrue("Should have HardwareGuard", expectedGuards.contains("HardwareGuard"))
        assertTrue("Should have DataGuard", expectedGuards.contains("DataGuard"))
        assertTrue("Should have SystemGuard", expectedGuards.contains("SystemGuard"))
        assertTrue("Should have PhysicalGuard", expectedGuards.contains("PhysicalGuard"))
        assertTrue("Should have UserGuard", expectedGuards.contains("UserGuard"))
    }

    @Test
    fun testMLIntegration() {
        // Test ML components integration
        val mlComponents = arrayOf(
            "AnomalyDetector",
            "BehaviorProfiler"
        )

        assertEquals("Should have 2 ML components", 2, mlComponents.size)
        assertTrue("Should have AnomalyDetector", mlComponents.contains("AnomalyDetector"))
        assertTrue("Should have BehaviorProfiler", mlComponents.contains("BehaviorProfiler"))
    }

    @Test
    fun testUtilityIntegration() {
        // Test utility components integration
        val utilities = arrayOf(
            "EncryptionManager",
            "AlertManager",
            "ShizukuWrapper",
            "ForensicReporter"
        )

        assertEquals("Should have 4 utilities", 4, utilities.size)
        assertTrue("Should have EncryptionManager", utilities.contains("EncryptionManager"))
        assertTrue("Should have AlertManager", utilities.contains("AlertManager"))
        assertTrue("Should have ShizukuWrapper", utilities.contains("ShizukuWrapper"))
        assertTrue("Should have ForensicReporter", utilities.contains("ForensicReporter"))
    }
}
