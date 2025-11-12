package com.fortress.hypervisor.modules

import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.PermissionInfo
import android.util.Log
import timber.log.Timber

/**
 * Threat Analyzer - Extracts best features from Android Malware Analyzer
 * Implements comprehensive threat analysis including:
 * - Permission-based risk analysis
 * - Signature matching
 * - Behavioral threat detection
 * - Threat scoring algorithm
 */
class ThreatAnalyzer(private val packageManager: PackageManager) {

    data class AppInfo(
        val packageName: String,
        val appName: String,
        val versionName: String,
        val permissions: List<String>,
        val apiCalls: List<String> = emptyList()
    )

    data class ThreatAnalysis(
        val threatScore: Double,
        val riskLevel: String,
        val permissionRisk: Double,
        val signatureRisk: Double,
        val behaviorRisk: Double,
        val riskFactors: List<String>
    )

    /**
     * Main threat analysis function
     */
    fun calculateThreatScore(app: AppInfo): ThreatAnalysis {
        val riskFactors = mutableListOf<String>()

        // Permission-based threat (40% weight)
        val permissionRisk = analyzePermissions(app.permissions)
        if (permissionRisk > 50) {
            riskFactors.add("High-risk permissions detected")
        }

        // Signature matching (30% weight)
        val signatureRisk = checkMalwareSignatures(app.packageName)
        if (signatureRisk > 0) {
            riskFactors.add("Malware signature detected")
        }

        // Behavior-based threat (30% weight)
        val behaviorRisk = analyzeBehavior(app.apiCalls)
        if (behaviorRisk > 50) {
            riskFactors.add("Suspicious behavior detected")
        }

        val threatScore = (permissionRisk * 0.4) + (signatureRisk * 0.3) + (behaviorRisk * 0.3)
        val finalScore = threatScore.coerceIn(0.0, 100.0)

        return ThreatAnalysis(
            threatScore = finalScore,
            riskLevel = getRiskLevel(finalScore),
            permissionRisk = permissionRisk,
            signatureRisk = signatureRisk,
            behaviorRisk = behaviorRisk,
            riskFactors = riskFactors
        )
    }

    /**
     * Permission-based risk analysis
     */
    private fun analyzePermissions(permissions: List<String>): Double {
        var riskScore = 0.0
        val dangerousPermissions = mapOf(
            // Critical permissions (25-30 points each)
            "android.permission.READ_PHONE_STATE" to 30.0,
            "android.permission.ACCESS_FINE_LOCATION" to 25.0,
            "android.permission.CAMERA" to 25.0,
            "android.permission.RECORD_AUDIO" to 25.0,
            "android.permission.READ_SMS" to 30.0,
            "android.permission.READ_CONTACTS" to 20.0,
            "android.permission.WRITE_SMS" to 25.0,
            "android.permission.CALL_PHONE" to 20.0,

            // High-risk permissions (15-20 points each)
            "android.permission.ACCESS_COARSE_LOCATION" to 15.0,
            "android.permission.READ_EXTERNAL_STORAGE" to 15.0,
            "android.permission.WRITE_EXTERNAL_STORAGE" to 15.0,
            "android.permission.INTERNET" to 10.0,
            "android.permission.ACCESS_NETWORK_STATE" to 5.0,
            "android.permission.WAKE_LOCK" to 10.0,

            // Medium-risk permissions (5-10 points each)
            "android.permission.VIBRATE" to 5.0,
            "android.permission.FLASHLIGHT" to 5.0,
            "android.permission.BLUETOOTH" to 8.0,
            "android.permission.BLUETOOTH_ADMIN" to 10.0,

            // Low-risk permissions (1-5 points each)
            "android.permission.RECEIVE_BOOT_COMPLETED" to 3.0,
            "android.permission.SYSTEM_ALERT_WINDOW" to 8.0,
            "android.permission.FOREGROUND_SERVICE" to 5.0
        )

        for (permission in permissions) {
            val risk = dangerousPermissions[permission] ?: 0.0
            riskScore += risk

            if (risk > 20) {
                Log.w("THREAT_ANALYZER", "High-risk permission detected: $permission (+$risk points)")
            }
        }

        // Analyze permission combinations
        riskScore += analyzePermissionCombinations(permissions)

        return riskScore.coerceIn(0.0, 100.0)
    }

    /**
     * Analyze dangerous permission combinations
     */
    private fun analyzePermissionCombinations(permissions: List<String>): Double {
        var combinationRisk = 0.0

        val hasLocation = permissions.contains("android.permission.ACCESS_FINE_LOCATION") ||
                         permissions.contains("android.permission.ACCESS_COARSE_LOCATION")
        val hasContacts = permissions.contains("android.permission.READ_CONTACTS")
        val hasSms = permissions.contains("android.permission.READ_SMS")
        val hasPhone = permissions.contains("android.permission.READ_PHONE_STATE")
        val hasInternet = permissions.contains("android.permission.INTERNET")

        // Location + Contacts + SMS = Spyware pattern (+25 points)
        if (hasLocation && hasContacts && hasSms) {
            combinationRisk += 25.0
            Log.w("THREAT_ANALYZER", "Spyware permission pattern detected")
        }

        // Phone + SMS + Internet = Premium rate abuse pattern (+20 points)
        if (hasPhone && hasSms && hasInternet) {
            combinationRisk += 20.0
            Log.w("THREAT_ANALYZER", "Premium rate abuse pattern detected")
        }

        // Camera + Microphone + Internet = Surveillance pattern (+15 points)
        val hasCamera = permissions.contains("android.permission.CAMERA")
        val hasMicrophone = permissions.contains("android.permission.RECORD_AUDIO")
        if (hasCamera && hasMicrophone && hasInternet) {
            combinationRisk += 15.0
            Log.w("THREAT_ANALYZER", "Surveillance permission pattern detected")
        }

        return combinationRisk
    }

    /**
     * Check for malware signatures
     */
    private fun checkMalwareSignatures(packageName: String): Double {
        var signatureRisk = 0.0

        // Known malware package patterns
        val malwarePatterns = arrayOf(
            "com.android.system",           // Fake system app
            "com.google.service",           // Fake Google service
            "com.system.update",            // Fake system update
            "com.android.root",             // Root exploit
            "com.superuser",                // Superuser exploit
            "com.frida",                    // Frida detection bypass
            "com.xposed",                   // Xposed framework
            "com.genymotion",               // Genymotion emulator
            "com.bluestacks",               // BlueStacks emulator
            "com.nox",                      // Nox emulator
            "com.memu",                     // MEmu emulator
            "com.ldplayer"                  // LDPlayer emulator
        )

        for (pattern in malwarePatterns) {
            if (packageName.contains(pattern)) {
                signatureRisk += 30.0
                Log.w("THREAT_ANALYZER", "Malware signature detected: $pattern in $packageName")
                break
            }
        }

        // Check for suspicious package naming patterns
        if (packageName.count { it == '.' } > 4) {
            signatureRisk += 10.0
            Log.w("THREAT_ANALYZER", "Suspicious package name depth: $packageName")
        }

        // Check for obfuscated package names (random characters)
        val randomCharRatio = packageName.count { it.isLetterOrDigit() && it.isLowerCase() } / packageName.length.toDouble()
        if (randomCharRatio > 0.8 && packageName.length > 20) {
            signatureRisk += 15.0
            Log.w("THREAT_ANALYZER", "Potentially obfuscated package name: $packageName")
        }

        return signatureRisk.coerceIn(0.0, 100.0)
    }

    /**
     * Analyze behavioral patterns
     */
    private fun analyzeBehavior(apiCalls: List<String>): Double {
        var behaviorRisk = 0.0

        if (apiCalls.isEmpty()) {
            return 0.0 // No behavior data available
        }

        // Suspicious API call patterns
        val suspiciousPatterns = mapOf(
            // File system operations (10 points each)
            "java.io.File" to 10.0,
            "android.os.Environment" to 8.0,
            "java.io.FileOutputStream" to 12.0,
            "java.io.FileInputStream" to 8.0,

            // Network operations (15 points each)
            "java.net.HttpURLConnection" to 15.0,
            "java.net.URL" to 10.0,
            "okhttp" to 12.0,
            "retrofit" to 10.0,

            // Cryptography operations (20 points each)
            "javax.crypto" to 20.0,
            "java.security" to 15.0,
            "android.security" to 18.0,

            // System operations (25 points each)
            "java.lang.Runtime" to 25.0,
            "android.os.Process" to 20.0,
            "java.lang.ProcessBuilder" to 25.0,

            // Reflection usage (30 points - indicates code hiding)
            "java.lang.reflect" to 30.0,
            "java.lang.Class" to 20.0,

            // Dynamic loading (35 points - indicates runtime code injection)
            "dalvik.system.DexClassLoader" to 35.0,
            "java.lang.ClassLoader" to 25.0,

            // Root detection bypass attempts (40 points)
            "libcore.io" to 40.0,
            "android.os.Build" to 15.0,

            // Anti-analysis techniques (45 points)
            "java.lang.StackTraceElement" to 45.0,
            "java.lang.Thread" to 20.0
        )

        for (apiCall in apiCalls) {
            for ((pattern, risk) in suspiciousPatterns) {
                if (apiCall.contains(pattern)) {
                    behaviorRisk += risk
                    Log.w("THREAT_ANALYZER", "Suspicious API call detected: $apiCall (+$risk points)")
                    break // Only count each API call once
                }
            }
        }

        // Analyze API call sequences
        behaviorRisk += analyzeApiSequences(apiCalls)

        return behaviorRisk.coerceIn(0.0, 100.0)
    }

    /**
     * Analyze suspicious API call sequences
     */
    private fun analyzeApiSequences(apiCalls: List<String>): Double {
        var sequenceRisk = 0.0

        // Check for root exploit sequences
        val hasRuntime = apiCalls.any { it.contains("java.lang.Runtime") }
        val hasProcess = apiCalls.any { it.contains("android.os.Process") }
        val hasFileSystem = apiCalls.any { it.contains("java.io.File") }

        if (hasRuntime && hasProcess && hasFileSystem) {
            sequenceRisk += 35.0
            Log.w("THREAT_ANALYZER", "Root exploit API sequence detected")
        }

        // Check for data exfiltration sequences
        val hasNetwork = apiCalls.any { it.contains("java.net") || it.contains("okhttp") }
        val hasContacts = apiCalls.any { it.contains("android.provider.ContactsContract") }
        val hasSms = apiCalls.any { it.contains("android.provider.Telephony") }

        if (hasNetwork && (hasContacts || hasSms)) {
            sequenceRisk += 30.0
            Log.w("THREAT_ANALYZER", "Data exfiltration API sequence detected")
        }

        // Check for surveillance sequences
        val hasCamera = apiCalls.any { it.contains("android.hardware.camera") }
        val hasMicrophone = apiCalls.any { it.contains("android.media.MediaRecorder") }
        val hasLocation = apiCalls.any { it.contains("android.location") }

        if (hasCamera && hasMicrophone && hasLocation && hasNetwork) {
            sequenceRisk += 40.0
            Log.w("THREAT_ANALYZER", "Surveillance API sequence detected")
        }

        return sequenceRisk
    }

    /**
     * Convert threat score to risk level
     */
    private fun getRiskLevel(score: Double): String = when {
        score >= 70.0 -> "CRITICAL"
        score >= 50.0 -> "HIGH"
        score >= 30.0 -> "MEDIUM"
        score >= 10.0 -> "LOW"
        else -> "VERY_LOW"
    }

    /**
     * Get detailed threat analysis report
     */
    fun getDetailedThreatReport(app: AppInfo): String {
        val analysis = calculateThreatScore(app)

        return """
            |=== THREAT ANALYSIS REPORT ===
            |App: ${app.appName} (${app.packageName})
            |Version: ${app.versionName}
            |
            |Overall Threat Score: ${analysis.threatScore}/100
            |Risk Level: ${analysis.riskLevel}
            |
            |Risk Breakdown:
            |• Permission Risk: ${analysis.permissionRisk}/100 (40% weight)
            |• Signature Risk: ${analysis.signatureRisk}/100 (30% weight)
            |• Behavior Risk: ${analysis.behaviorRisk}/100 (30% weight)
            |
            |Risk Factors:
            |${analysis.riskFactors.joinToString("\n") { "• $it" }}
            |
            |Permissions Analyzed: ${app.permissions.size}
            |API Calls Analyzed: ${app.apiCalls.size}
            |================================
        """.trimMargin()
    }

    /**
     * Analyze all installed applications
     */
    fun analyzeAllInstalledApps(): List<Pair<AppInfo, ThreatAnalysis>> {
        val results = mutableListOf<Pair<AppInfo, ThreatAnalysis>>()

        try {
            val packages = packageManager.getInstalledPackages(PackageManager.GET_PERMISSIONS)

            for (packageInfo in packages) {
                try {
                    val appInfo = createAppInfo(packageInfo)
                    val threatAnalysis = calculateThreatScore(appInfo)
                    results.add(Pair(appInfo, threatAnalysis))
                } catch (e: Exception) {
                    Log.e("THREAT_ANALYZER", "Error analyzing package: ${packageInfo.packageName}", e)
                }
            }
        } catch (e: Exception) {
            Log.e("THREAT_ANALYZER", "Error analyzing installed apps", e)
        }

        return results.sortedByDescending { it.second.threatScore }
    }

    /**
     * Create AppInfo from PackageInfo
     */
    private fun createAppInfo(packageInfo: PackageInfo): AppInfo {
        val appName = try {
            packageManager.getApplicationLabel(packageInfo.applicationInfo).toString()
        } catch (e: Exception) {
            packageInfo.packageName
        }

        val permissions = packageInfo.requestedPermissions?.toList() ?: emptyList()

        return AppInfo(
            packageName = packageInfo.packageName,
            appName = appName,
            versionName = packageInfo.versionName ?: "Unknown",
            permissions = permissions
        )
    }
}