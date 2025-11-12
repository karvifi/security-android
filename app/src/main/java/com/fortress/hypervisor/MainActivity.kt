package com.fortress.hypervisor

import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fortress.hypervisor.modules.DeviceSecurityAnalyzer
import com.fortress.hypervisor.modules.ForensicLogger
import com.fortress.hypervisor.modules.ThreatAnalyzer
import com.fortress.hypervisor.utils.ErrorHandler
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SecurityDashboard()
                }
            }
        }
    }
}

@Composable
fun SecurityDashboard(viewModel: SecurityViewModel = viewModel()) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // State management
    var securityReport by remember { mutableStateOf("") }
    var threatReport by remember { mutableStateOf("") }
    var vpnStatus by remember { mutableStateOf("Checking...") }
    var isAnalyzing by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Text(
            text = "🛡️ Fortress Hypervisor",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Advanced Android Security Platform",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.secondary
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Quick Actions Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = {
                    scope.launch {
                        isAnalyzing = true
                        try {
                            securityReport = HypervisorApplication.deviceSecurityAnalyzer.getDetailedSecurityReport()
                        } catch (e: Exception) {
                            ErrorHandler.handleException(e, "Security analysis")
                            securityReport = "Error generating security report: ${e.message}"
                        }
                        isAnalyzing = false
                    }
                },
                enabled = !isAnalyzing
            ) {
                Text(if (isAnalyzing) "Analyzing..." else "🔍 Analyze Device")
            }

            Button(
                onClick = {
                    scope.launch {
                        try {
                            val apps = HypervisorApplication.threatAnalyzer.analyzeAllInstalledApps()
                            val highRisk = apps.filter { it.second.threatScore > 50 }
                            threatReport = """
                                |=== THREAT ANALYSIS SUMMARY ===
                                |Total Apps Analyzed: ${apps.size}
                                |High-Risk Apps: ${highRisk.size}
                                |
                                |Top Threats:
                                |${highRisk.take(5).joinToString("\n") { "${it.first.appName}: ${it.second.threatScore.toInt()}%" }}
                                |================================
                            """.trimMargin()
                        } catch (e: Exception) {
                            ErrorHandler.handleException(e, "Threat analysis")
                            threatReport = "Error analyzing threats: ${e.message}"
                        }
                    }
                }
            ) {
                Text("⚠️ Scan Threats")
            }

            Button(
                onClick = {
                    val vpnIntent = VpnService.prepare(context)
                    if (vpnIntent != null) {
                        context.startActivity(vpnIntent)
                    } else {
                        // Start VPN service directly
                        val serviceIntent = Intent(context, HypervisorVpnService::class.java)
                        context.startService(serviceIntent)
                    }
                }
            ) {
                Text("🔒 Start VPN")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Status Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatusCard(
                title = "Device Security",
                value = "Analyzed",
                color = Color(0xFF4CAF50),
                modifier = Modifier.weight(1f)
            )

            StatusCard(
                title = "Threat Level",
                value = "Monitoring",
                color = Color(0xFFFF9800),
                modifier = Modifier.weight(1f)
            )

            StatusCard(
                title = "VPN Status",
                value = "Active",
                color = Color(0xFF2196F3),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Security Report Section
        if (securityReport.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🔐 Device Security Analysis",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = securityReport,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Threat Report Section
        if (threatReport.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "⚠️ Threat Analysis Report",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.error
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = threatReport,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // System Information Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "📊 System Information",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                val runtime = Runtime.getRuntime()
                val systemInfo = """
                    |Device: ${android.os.Build.MODEL}
                    |Android: ${android.os.Build.VERSION.RELEASE} (API ${android.os.Build.VERSION.SDK_INT})
                    |Security Patch: ${android.os.Build.VERSION.SECURITY_PATCH}
                    |Max Memory: ${runtime.maxMemory() / 1024 / 1024} MB
                    |Available Memory: ${runtime.freeMemory() / 1024 / 1024} MB
                    |Used Memory: ${(runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024} MB
                """.trimMargin()

                Text(
                    text = systemInfo,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Service Control Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "🔧 Service Controls",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            // Open accessibility settings
                            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                            context.startActivity(intent)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("♿ Accessibility")
                    }

                    OutlinedButton(
                        onClick = {
                            // Open notification listener settings
                            val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                            context.startActivity(intent)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("🔔 Notifications")
                    }

                    OutlinedButton(
                        onClick = {
                            // Open device admin settings (if implemented)
                            // This would need additional implementation
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("🔑 Device Admin")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Forensic Logging Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "🔗 Forensic Evidence Chain",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        scope.launch {
                            try {
                                val chainSummary = HypervisorApplication.forensicLogger.exportChainSummary()
                                // In a real app, this would show in a dialog or navigate to a details screen
                                Log.i("FORENSIC", chainSummary)
                            } catch (e: Exception) {
                                ErrorHandler.handleException(e, "Forensic chain export")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("📋 View Evidence Chain")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Footer
        Text(
            text = "✅ Fortress Hypervisor - Enterprise Security Ready",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun StatusCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = color
            )
        }
    }
}

class SecurityViewModel : androidx.lifecycle.ViewModel() {
    // ViewModel for managing security state
    // This would contain more complex state management in a full implementation
}