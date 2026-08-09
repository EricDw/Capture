package com.dewildte.capture.auth

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.GetCredentialInterruptedException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.UserRecoverableAuthException
import com.dewildte.capture.events.RequestGoogleAuthResolution
import com.dewildte.capture.utils.Actor
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GoogleAuthManager(
    private val context: Context,
    private val uiActor: Actor
) {

    private val credentialManager = CredentialManager.create(context)
    
    private val _userEmail = MutableStateFlow<String?>(null)
    val userEmail: StateFlow<String?> = _userEmail.asStateFlow()

    private var _accessToken: String? = null

    suspend fun getAccessToken(): String? {
        val email = _userEmail.value ?: run {
            Log.w(TAG, "getAccessToken: User email is null, user might not be signed in")
            return null
        }
        Log.d(TAG, "getAccessToken: Fetching token for $email with scopes: $SCOPES")
        return withContext(Dispatchers.IO) {
            try {
                // Return cached token if valid, or fetch a new one
                val token = _accessToken ?: GoogleAuthUtil.getToken(context, email, SCOPES)
                Log.d(TAG, "getAccessToken: Successfully retrieved token (starts with ${token.take(10)})")
                token
            } catch (e: UserRecoverableAuthException) {
                Log.e(TAG, "User interaction required for scopes: ${e.message}")
                e.intent?.let { intent ->
                    uiActor.tell(RequestGoogleAuthResolution(intent))
                }
                null
            } catch (e: Exception) {
                Log.e(TAG, "Error getting access token for $email", e)
                null
            }
        }
    }

    suspend fun signIn(clientId: String?): Result<String> {
        Log.d(TAG, "Starting sign-in with Client ID: $clientId")
        
        if (clientId.isNullOrBlank()) {
            return Result.failure(Exception("Google Client ID is missing. Please set the Web Client ID in Settings."))
        }

        val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false) // Allow all accounts, not just those already signed in
            .setServerClientId(clientId)
            .setAutoSelectEnabled(false) // Disable auto-select to force the UI for debugging
            .build()

        val request: GetCredentialRequest = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        return try {
            val result = credentialManager.getCredential(
                context = context,
                request = request
            )
            Log.d(TAG, "getCredential successful")
            handleSignIn(result)
        } catch (e: NoCredentialException) {
            Log.e(TAG, "No credentials available. Check SHA-1 and package name (com.dewildte.capture) in Cloud Console.")
            Result.failure(Exception("No accounts found. Please ensure you have a Google account on this device and your SHA-1 fingerprint matches the Cloud Console."))
        } catch (e: GetCredentialCancellationException) {
            Log.w(TAG, "Sign-in cancelled by user")
            Result.failure(Exception("Sign-in cancelled"))
        } catch (e: GetCredentialInterruptedException) {
            Log.e(TAG, "Sign-in interrupted")
            Result.failure(Exception("Sign-in interrupted"))
        } catch (e: GetCredentialException) {
            Log.e(TAG, "Credential Manager error: [${e.type}] ${e.message}", e)
            Result.failure(Exception("Sign-in failed: ${e.message}"))
        } catch (e: Exception) {
            Log.e(TAG, "General auth error", e)
            Result.failure(e)
        }
    }

    private fun handleSignIn(result: GetCredentialResponse): Result<String> {
        val credential = result.credential
        Log.d(TAG, "Handling credential of type: ${credential.type}")
        
        return try {
            if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val userIdentifier = googleIdTokenCredential.id
                Log.i(TAG, "Successfully authenticated user with identifier: $userIdentifier")
                _userEmail.value = userIdentifier
                // We don't use the ID token for MCP, we'll fetch an access token later
                _accessToken = null 
                
                // Immediately check for access token permissions to trigger consent flow if needed
                CoroutineScope(Dispatchers.Main).launch {
                    getAccessToken()
                }

                Result.success(userIdentifier)
            } else {
                Log.e(TAG, "Received unexpected credential type: ${credential.type}")
                Result.failure(Exception("Unsupported credential type: ${credential.type}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing Google ID Token credential", e)
            Result.failure(e)
        }
    }

    fun signOut() {
        Log.d(TAG, "Signing out user: ${_userEmail.value}")
        val tokenToClear = _accessToken
        _userEmail.value = null
        _accessToken = null
        
        if (tokenToClear != null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    GoogleAuthUtil.invalidateToken(context, tokenToClear)
                    Log.d(TAG, "Successfully invalidated token on sign out")
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to invalidate token during sign out", e)
                }
            }
        }
    }

    suspend fun refreshAccessToken(): String? {
        val token = _accessToken ?: getAccessToken()
        if (token != null) {
            withContext(Dispatchers.IO) {
                try {
                    GoogleAuthUtil.invalidateToken(context, token)
                    _accessToken = null
                    Log.d(TAG, "Invalidated stale token, fetching fresh one...")
                } catch (e: Exception) {
                    Log.e(TAG, "Error invalidating token", e)
                }
            }
        }
        return getAccessToken()
    }

    suspend fun invalidateCurrentToken() {
        val token = _accessToken ?: getAccessToken()
        if (token != null) {
            withContext(Dispatchers.IO) {
                try {
                    GoogleAuthUtil.invalidateToken(context, token)
                    _accessToken = null
                    Log.d(TAG, "Manually invalidated current access token")
                } catch (e: Exception) {
                    Log.e(TAG, "Error invalidating token", e)
                }
            }
        }
    }

    companion object {
        private const val TAG = "GoogleAuthManager"
        private const val SCOPES = "oauth2:https://www.googleapis.com/auth/calendar https://www.googleapis.com/auth/drive.readonly https://www.googleapis.com/auth/gmail.readonly https://www.googleapis.com/auth/tasks"
    }
}
