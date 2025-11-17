package com.androidsecurity.app.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.androidsecurity.app.AndroidSecurityApplication
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            AndroidSecurityTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SecurityApp()
                }
            }
        }
    }
}

@Composable
fun SecurityApp() {
    var isLoggedIn by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    
    val coroutineScope = rememberCoroutineScope()
    val auth = FirebaseAuth.getInstance()
    
    // Check if already logged in
    LaunchedEffect(Unit) {
        isLoggedIn = auth.currentUser != null
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Android Security",
            style = MaterialTheme.typography.headlineLarge
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        if (!isLoggedIn) {
            // Login Screen
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(0.8f)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(0.8f)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(0.8f),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = {
                        isLoading = true
                        coroutineScope.launch {
                            try {
                                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                                    isLoading = false
                                    if (task.isSuccessful) {
                                        isLoggedIn = true
                                        message = "✅ Login successful"
                                    } else {
                                        message = "❌ ${task.exception?.message}"
                                    }
                                }
                            } catch (e: Exception) {
                                isLoading = false
                                message = "❌ ${e.message}"
                            }
                        }
                    },
                    enabled = !isLoading
                ) {
                    Text("Sign In")
                }
                
                Button(
                    onClick = {
                        isLoading = true
                        coroutineScope.launch {
                            try {
                                auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                                    isLoading = false
                                    if (task.isSuccessful) {
                                        isLoggedIn = true
                                        message = "✅ Account created"
                                    } else {
                                        message = "❌ ${task.exception?.message}"
                                    }
                                }
                            } catch (e: Exception) {
                                isLoading = false
                                message = "❌ ${e.message}"
                            }
                        }
                    },
                    enabled = !isLoading
                ) {
                    Text("Sign Up")
                }
            }
            
        } else {
            // Home Screen
            Card(
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "✅ Connected to Firebase",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Green
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        "Email: ${auth.currentUser?.email}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(onClick = {
                        auth.signOut()
                        isLoggedIn = false
                        message = "Signed out"
                    }) {
                        Text("Sign Out")
                    }
                }
            }
        }
        
        if (message.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                message,
                color = if (message.startsWith("✅")) Color.Green else Color.Red
            )
        }
        
        if (isLoading) {
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator()
        }
    }
}

@Composable
fun AndroidSecurityTheme(content: @Composable () -> Unit) {
    MaterialTheme {
        content()
    }
}