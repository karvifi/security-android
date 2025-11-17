package com.androidsecurity.app

import android.app.Application
import android.util.Log
import androidx.multidex.MultiDexApplication
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.crashlytics.FirebaseCrashlytics

class AndroidSecurityApplication : MultiDexApplication() {
    
    companion object {
        private const val TAG = "AndroidSecApp"
        
        // Firebase instances
        lateinit var auth: FirebaseAuth
        lateinit var firestore: FirebaseFirestore
        lateinit var crashlytics: FirebaseCrashlytics
    }
    
    override fun onCreate() {
        super.onCreate()
        
        try {
            // Initialize Firebase
            FirebaseApp.initializeApp(this)
            
            // Get Firebase instances
            auth = FirebaseAuth.getInstance()
            firestore = FirebaseFirestore.getInstance()
            crashlytics = FirebaseCrashlytics.getInstance()
            
            // Enable offline persistence
            firestore.firestoreSettings = com.google.firebase.firestore.FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()
            
            Log.i(TAG, "Firebase initialized successfully")
            
        } catch (e: Exception) {
            Log.e(TAG, "Firebase initialization failed", e)
            crashlytics.recordException(e)
        }
    }
}