package com.fortress.hypervisor.modules

import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.Log
import java.io.File
import java.security.MessageDigest
import kotlin.math.abs

/**
 * Device Security Analyzer - Extracts best features from TrustDevice Android
 * Implements comprehensive device security analysis including:
 * - Device fingerprinting
 * - Rooted device detection
 * - Emulator detection
 * - Virtualization detection
 * - Risk scoring algorithm
 */
class DeviceSecurityAnalyzer(private val context: Context) {

    data class DeviceRiskScore(
        val riskScore: Double,
        val fingerprint: String,
        val riskLevel: String,
        val detectionResults: Map<String, Boolean>
    )

    /**
     * Main analysis function - combines all security checks
     */
    fun analyzeDeviceSecurity(): DeviceRiskScore {
        var riskScore = 0.0
        val detectionResults = mutableMapOf<String, Boolean>()

        // 1. Detect rooted device (+30 if true)
        val isRooted = isDeviceRooted()
        detectionResults["rooted"] = isRooted
        if (isRooted) {
            riskScore += 30.0
            Log.w("SECURITY", "Rooted device detected")
        }

        // 2. Detect emulator (+20 if true)
        val isEmulator = isRunningInEmulator()
        detectionResults["emulator"] = isEmulator
        if (isEmulator) {
            riskScore += 20.0
            Log.w("SECURITY", "Emulator detected")
        }

        // 3. Detect virtualization (+25 if true)
        val isVirtualized = isRunningInVirtualization()
        detectionResults["virtualization"] = isVirtualized
        if (isVirtualized) {
            riskScore += 25.0
            Log.w("SECURITY", "Virtualization detected")
        }

        // 4. Check for dangerous apps (+15 if found)
        val hasDangerousApps = checkForDangerousApps()
        detectionResults["dangerous_apps"] = hasDangerousApps
        if (hasDangerousApps) {
            riskScore += 15.0
            Log.w("SECURITY", "Dangerous applications detected")
        }

        // 5. Generate device fingerprint
        val fingerprint = generateDeviceFingerprint()

        // 6. Check system integrity (+10 if compromised)
        val systemIntegrity = checkSystemIntegrity()
        detectionResults["system_integrity"] = systemIntegrity
        if (!systemIntegrity) {
            riskScore += 10.0
            Log.w("SECURITY", "System integrity compromised")
        }

        return DeviceRiskScore(
            riskScore = riskScore.coerceIn(0.0, 100.0),
            fingerprint = fingerprint,
            riskLevel = getRiskLevel(riskScore),
            detectionResults = detectionResults
        )
    }

    /**
     * Rooted Device Detection - Multiple detection methods
     */
    private fun isDeviceRooted(): Boolean {
        return try {
            // Method 1: Check for su binary
            val suPaths = arrayOf(
                "/system/bin/su",
                "/system/xbin/su",
                "/sbin/su",
                "/system/su",
                "/system/bin/.ext/.su",
                "/system/usr/we-need-root/su-backup",
                "/system/xbin/mu"
            )

            for (path in suPaths) {
                if (File(path).exists()) {
                    Log.d("ROOT_CHECK", "SU binary found at: $path")
                    return true
                }
            }

            // Method 2: Check for Superuser.apk
            val superuserPaths = arrayOf(
                "/system/app/Superuser.apk",
                "/system/app/superuser.apk",
                "/data/app/com.noshufou.android.su*",
                "/data/app/eu.chainfire.supersu*"
            )

            for (path in superuserPaths) {
                if (File(path).exists() || File(path.replace("*", "")).exists()) {
                    Log.d("ROOT_CHECK", "Superuser app found at: $path")
                    return true
                }
            }

            // Method 3: Check build properties
            val buildTags = Build.TAGS
            if (buildTags != null && buildTags.contains("test-keys")) {
                Log.d("ROOT_CHECK", "Test keys found in build tags")
                return true
            }

            // Method 4: Check ro.secure property
            try {
                val process = Runtime.getRuntime().exec("getprop ro.secure")
                val input = process.inputStream.bufferedReader().readText()
                if (input.contains("0")) {
                    Log.d("ROOT_CHECK", "ro.secure property indicates root")
                    return true
                }
            } catch (e: Exception) {
                Log.d("ROOT_CHECK", "Could not check ro.secure property")
            }

            false
        } catch (e: Exception) {
            Log.e("ROOT_CHECK", "Error during root detection", e)
            false
        }
    }

    /**
     * Emulator Detection - Multiple detection methods
     */
    private fun isRunningInEmulator(): Boolean {
        return try {
            // Method 1: Check build properties
            val emulatorProps = arrayOf(
                "ro.kernel.qemu",
                "ro.hardware",
                "qemu.hw.mainkeys",
                "qemu.sf.fake_camera"
            )

            for (prop in emulatorProps) {
                try {
                    val process = Runtime.getRuntime().exec("getprop $prop")
                    val value = process.inputStream.bufferedReader().readText().trim()
                    if (value == "1" || value.contains("goldfish") || value.contains("ranchu")) {
                        Log.d("EMULATOR_CHECK", "Emulator property detected: $prop=$value")
                        return true
                    }
                } catch (e: Exception) {
                    // Continue checking other properties
                }
            }

            // Method 2: Check device model
            val model = Build.MODEL
            val emulatorModels = arrayOf(
                "sdk", "emulator", "android sdk built for x86",
                "Genymotion", "google_sdk"
            )

            for (emulatorModel in emulatorModels) {
                if (model.contains(emulatorModel, ignoreCase = true)) {
                    Log.d("EMULATOR_CHECK", "Emulator model detected: $model")
                    return true
                }
            }

            // Method 3: Check telephony
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
            if (telephonyManager != null) {
                val networkOperator = telephonyManager.networkOperatorName
                if (networkOperator == null || networkOperator == "Android") {
                    Log.d("EMULATOR_CHECK", "Emulator telephony detected")
                    return true
                }
            }

            // Method 4: Check file system
            val emulatorFiles = arrayOf(
                "/system/lib/libc_malloc_debug_qemu.so",
                "/sys/qemu_trace",
                "/system/bin/qemu-props"
            )

            for (file in emulatorFiles) {
                if (File(file).exists()) {
                    Log.d("EMULATOR_CHECK", "Emulator file detected: $file")
                    return true
                }
            }

            false
        } catch (e: Exception) {
            Log.e("EMULATOR_CHECK", "Error during emulator detection", e)
            false
        }
    }

    /**
     * Virtualization Detection - Detect various virtualization environments
     */
    private fun isRunningInVirtualization(): Boolean {
        return try {
            // Method 1: Check for Xposed framework
            val xposedPaths = arrayOf(
                "/system/lib/libxposed_art.so",
                "/system/lib64/libxposed_art.so",
                "/data/data/de.robv.android.xposed.installer"
            )

            for (path in xposedPaths) {
                if (File(path).exists()) {
                    Log.d("VIRTUALIZATION_CHECK", "Xposed framework detected: $path")
                    return true
                }
            }

            // Method 2: Check for Frida
            try {
                val process = Runtime.getRuntime().exec("ps")
                val output = process.inputStream.bufferedReader().readText()
                if (output.contains("frida") || output.contains("gum")) {
                    Log.d("VIRTUALIZATION_CHECK", "Frida detected in process list")
                    return true
                }
            } catch (e: Exception) {
                // Continue with other checks
            }

            // Method 3: Check for KVM
            val kvmFiles = arrayOf(
                "/dev/kvm",
                "/sys/module/kvm",
                "/proc/driver/kvm"
            )

            for (file in kvmFiles) {
                if (File(file).exists()) {
                    Log.d("VIRTUALIZATION_CHECK", "KVM virtualization detected: $file")
                    return true
                }
            }

            // Method 4: Check for Bochs
            try {
                val process = Runtime.getRuntime().exec("cat /proc/cpuinfo")
                val cpuInfo = process.inputStream.bufferedReader().readText()
                if (cpuInfo.contains("Bochs") || cpuInfo.contains("bochs")) {
                    Log.d("VIRTUALIZATION_CHECK", "Bochs virtualization detected")
                    return true
                }
            } catch (e: Exception) {
                // Continue with other checks
            }

            false
        } catch (e: Exception) {
            Log.e("VIRTUALIZATION_CHECK", "Error during virtualization detection", e)
            false
        }
    }

    /**
     * Check for dangerous applications
     */
    private fun checkForDangerousApps(): Boolean {
        val dangerousPackages = arrayOf(
            "com.noshufou.android.su",           // SuperSU
            "eu.chainfire.supersu",              // SuperSU
            "com.koushikdutta.superuser",        // Superuser
            "com.thirdparty.superuser",          // Superuser
            "com.yellowes.su",                   // Superuser
            "com.topjohnwu.magisk",             // Magisk
            "com.keramidas.TitaniumBackup",      // Titanium Backup
            "com.frida.main",                    // Frida
            "com.guardsquare.dexguard",          // DexGuard
            "com.genymotion.superuser"           // Genymotion
        )

        val packageManager = context.packageManager

        for (packageName in dangerousPackages) {
            try {
                packageManager.getPackageInfo(packageName, 0)
                Log.d("DANGEROUS_APPS", "Dangerous app detected: $packageName")
                return true
            } catch (e: PackageManager.NameNotFoundException) {
                // Package not found, continue checking
            }
        }

        return false
    }

    /**
     * Check system integrity
     */
    private fun checkSystemIntegrity(): Boolean {
        return try {
            // Check if system is in a known good state
            val systemPartition = File("/system")
            if (!systemPartition.exists() || !systemPartition.canRead()) {
                Log.d("SYSTEM_INTEGRITY", "System partition not accessible")
                return false
            }

            // Check for suspicious modifications
            val suspiciousFiles = arrayOf(
                "/system/bin/su",
                "/system/xbin/su",
                "/data/local/tmp/su"
            )

            for (file in suspiciousFiles) {
                if (File(file).exists()) {
                    Log.d("SYSTEM_INTEGRITY", "Suspicious file found: $file")
                    return false
                }
            }

            true
        } catch (e: Exception) {
            Log.e("SYSTEM_INTEGRITY", "Error checking system integrity", e)
            false
        }
    }

    /**
     * Generate comprehensive device fingerprint
     */
    private fun generateDeviceFingerprint(): String {
        return try {
            val fingerprintData = StringBuilder()

            // Hardware identifiers
            fingerprintData.append(Build.BOARD).append("|")
            fingerprintData.append(Build.BRAND).append("|")
            fingerprintData.append(Build.DEVICE).append("|")
            fingerprintData.append(Build.HARDWARE).append("|")
            fingerprintData.append(Build.MODEL).append("|")
            fingerprintData.append(Build.PRODUCT).append("|")
            fingerprintData.append(Build.SERIAL).append("|")

            // Software identifiers
            fingerprintData.append(Build.ID).append("|")
            fingerprintData.append(Build.VERSION.RELEASE).append("|")
            fingerprintData.append(Build.VERSION.SDK_INT).append("|")

            // Network identifiers
            try {
                val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as? WifiManager
                if (wifiManager != null) {
                    val wifiInfo = wifiManager.connectionInfo
                    fingerprintData.append(wifiInfo.macAddress).append("|")
                }
            } catch (e: Exception) {
                fingerprintData.append("unknown|")
            }

            // Telephony identifiers
            try {
                val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
                if (telephonyManager != null) {
                    fingerprintData.append(telephonyManager.deviceId).append("|")
                    fingerprintData.append(telephonyManager.subscriberId).append("|")
                }
            } catch (e: SecurityException) {
                fingerprintData.append("permission_denied|")
            } catch (e: Exception) {
                fingerprintData.append("unknown|")
            }

            // Android ID
            try {
                val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
                fingerprintData.append(androidId).append("|")
            } catch (e: Exception) {
                fingerprintData.append("unknown|")
            }

            // Generate SHA-256 hash of fingerprint
            val messageDigest = MessageDigest.getInstance("SHA-256")
            val hashBytes = messageDigest.digest(fingerprintData.toString().toByteArray())
            val fingerprint = hashBytes.joinToString("") { "%02x".format(it) }

            Log.d("DEVICE_FINGERPRINT", "Generated fingerprint: $fingerprint")
            fingerprint

        } catch (e: Exception) {
            Log.e("DEVICE_FINGERPRINT", "Error generating fingerprint", e)
            "error_generating_fingerprint"
        }
    }

    /**
     * Convert risk score to risk level
     */
    private fun getRiskLevel(score: Double): String = when {
        score >= 70.0 -> "CRITICAL"
        score >= 50.0 -> "HIGH"
        score >= 30.0 -> "MEDIUM"
        score >= 10.0 -> "LOW"
        else -> "VERY_LOW"
    }

    /**
     * Get detailed security report
     */
    fun getDetailedSecurityReport(): String {
        val analysis = analyzeDeviceSecurity()

        return """
            |=== DEVICE SECURITY ANALYSIS REPORT ===
            |Risk Score: ${analysis.riskScore}/100
            |Risk Level: ${analysis.riskLevel}
            |Device Fingerprint: ${analysis.fingerprint}
            |
            |Detection Results:
            |• Rooted Device: ${analysis.detectionResults["rooted"]}
            |• Running in Emulator: ${analysis.detectionResults["emulator"]}
            |• Virtualization Detected: ${analysis.detectionResults["virtualization"]}
            |• Dangerous Apps Found: ${analysis.detectionResults["dangerous_apps"]}
            |• System Integrity: ${analysis.detectionResults["system_integrity"]}
            |
            |Device Info:
            |• Model: ${Build.MODEL}
            |• Android Version: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})
            |• Build Fingerprint: ${Build.FINGERPRINT}
            |=====================================
        """.trimMargin()
    }
}