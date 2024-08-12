package com.being.coder.app.credentials

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.being.coder.app.credentials.ui.theme.CredentialsTheme
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.util.UUID

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CredentialsTheme {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    GoogleSignInButton()
                }
            }
        }
    }
}

@Composable
fun GoogleSignInButton() {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val onClick = {
        val rawNonce = UUID.randomUUID().toString()
        val md = MessageDigest.getInstance("SHA-256")

        val digest = md.digest(rawNonce.toByteArray())
        val hashedNonce = digest.fold("") { str, it -> str + "%02x".format(it) }

        val credentialManager = CredentialManager.create(context)
        val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId("xxxxxxxxxxxxxxxxxxxx-xxxxxxxxxxxxxxxxxxxxxxxxxxxxx.apps.googleusercontent.com")
            .setNonce(hashedNonce)
            .setAutoSelectEnabled(false)
            .build()

        val request: GetCredentialRequest = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        scope.launch {
            try {
                val credentialResponse = credentialManager.getCredential(
                    request = request,
                    context = context
                )

                val credential = credentialResponse.credential
                val googleIdTokenCredential = GoogleIdTokenCredential
                    .createFrom(credential.data)

                val googleIdToken = googleIdTokenCredential.idToken

                Log.d("GoogleSignInButton", "Google ID Token: $googleIdToken")
                // Proceed with using the Google ID token for authentication.
                Toast.makeText(context, "Signed In!", Toast.LENGTH_SHORT).show()
            } catch (e: GetCredentialCancellationException){
                e.printStackTrace()
            } catch (e: GetCredentialException){
                e.printStackTrace()
            } catch (e: GoogleIdTokenParsingException){
                e.printStackTrace()
            }
        }

    }

    Button(onClick = {
        onClick()
    }) {
        Text(text = "SignIn with Google")
    }
}