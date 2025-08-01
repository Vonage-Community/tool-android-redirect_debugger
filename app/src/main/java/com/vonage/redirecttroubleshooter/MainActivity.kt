package com.vonage.redirecttroubleshooter

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen // Added import
import com.vonage.redirecttroubleshooter.ui.theme.VonageRedirectTroubleshooterTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.io.OutputStreamWriter
import androidx.compose.foundation.border


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Handle the splash screen transition.
        installSplashScreen()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VonageRedirectTroubleshooterTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(modifier: Modifier = Modifier) {
    var url by remember { mutableStateOf("") }
    var debugInfo by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    // Configure OkHttpClient to not follow redirects
    val client = remember { OkHttpClient.Builder().followRedirects(false).build() }
    val context = LocalContext.current

    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/plain"),
        onResult = { uri: Uri? ->
            uri?.let {
                try {
                    context.contentResolver.openOutputStream(it)?.use { outputStream ->
                        OutputStreamWriter(outputStream).use { writer ->
                            writer.write(debugInfo)
                        }
                    }
                    scope.launch(Dispatchers.Main) {
                        debugInfo += """

--- Saved to file ---"""
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                    scope.launch(Dispatchers.Main) {
                        debugInfo += """

--- Error saving file: ${e.message} ---"""
                    }
                }
            }
        }
    )

    Column(modifier = modifier.padding(16.dp).fillMaxSize()) {
        Text(
            text = "Enter URL:"
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = url,
            onValueChange = { url = it },
            label = { Text("URL") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                if (url.isBlank()) {
                    debugInfo = "Please enter a URL."
                    return@Button
                }
                scope.launch(Dispatchers.IO) {
                    var currentRequestUrl = url
                    val MAX_REDIRECTS = 10
                    val currentDebugInfo = StringBuilder()

                    try {
                        for (i in 0..MAX_REDIRECTS) {
                            if (i > 0) { // Add a separator for subsequent requests in a chain
                                currentDebugInfo.append("\n\n")
                            }
                            currentDebugInfo.append("--- Request (${i + 1}) ---\n")
                            val request = Request.Builder()
                                .url(currentRequestUrl)
                                .addHeader("User-Agent", "vonage-redirect-tester/1.0")
                                .build()

                            currentDebugInfo.append("${request.method} ${request.url.encodedPath}${if (request.url.encodedQuery != null) "?" + request.url.encodedQuery else ""} HTTP/1.1\n")
                            currentDebugInfo.append("Host: ${request.url.host}\n")
                            request.headers.forEach { (name, value) ->
                                currentDebugInfo.append("$name: $value\n")
                            }

                            val response: Response = client.newCall(request).execute()
                            currentDebugInfo.append("\n--- Response (${i + 1}) ---\n")
                            currentDebugInfo.append("HTTP/${response.protocol.name.substringAfter("/")} ${response.code} ${response.message}\n")
                            response.headers.forEach { (name, value) ->
                                currentDebugInfo.append("$name: $value\n")
                            }

                            withContext(Dispatchers.Main) {
                                debugInfo = currentDebugInfo.toString()
                            }

                            if (response.isRedirect) {
                                val location = response.header("Location")
                                if (location != null) {
                                    currentRequestUrl = request.url.resolve(location).toString() // Resolve relative redirects
                                    currentDebugInfo.append("\n--- Redirecting to: $currentRequestUrl ---")
                                    if (i == MAX_REDIRECTS) {
                                        currentDebugInfo.append("\n\n--- Max redirects reached ---")
                                        withContext(Dispatchers.Main) {
                                            debugInfo = currentDebugInfo.toString()
                                        }
                                        break
                                    }
                                } else {
                                    currentDebugInfo.append("\n\n--- Redirect response but no Location header ---")
                                    withContext(Dispatchers.Main) {
                                        debugInfo = currentDebugInfo.toString()
                                    }
                                    break
                                }
                            } else {
                                // Not a redirect, so we're done with this chain.
                                break
                            }
                        }
                    } catch (e: IllegalArgumentException) {
                        e.printStackTrace()
                        val errorMsg = """Error: Invalid URL. Please ensure it includes http:// or https://
${e.message}"""
                        currentDebugInfo.append("\n\n$errorMsg")
                        withContext(Dispatchers.Main) {
                            debugInfo = currentDebugInfo.toString()
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                        val errorMsg = """Error: Network request failed.
${e.message}"""
                        currentDebugInfo.append("\n\n$errorMsg")
                        withContext(Dispatchers.Main) {
                            debugInfo = currentDebugInfo.toString()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        val errorMsg = """Error: An unexpected error occurred.
${e.message}
${e.stackTraceToString()}"""
                        currentDebugInfo.append("\n\n$errorMsg")
                        withContext(Dispatchers.Main) {
                            debugInfo = currentDebugInfo.toString()
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Fetch URL")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Debug Information:"
        )
        Spacer(modifier = Modifier.height(8.dp))
        val scrollState = rememberScrollState()
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .border(
                    BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    shape = RoundedCornerShape(4.dp)
                )
                .padding(8.dp) // Padding inside the border
                .verticalScroll(scrollState)
        ) {
            Text(
                text = debugInfo,
                modifier = Modifier.fillMaxWidth() // Text fills width inside the Box
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {
                if (debugInfo.isNotBlank()) {
                    createDocumentLauncher.launch("debug_info.txt")
                } else {
                    scope.launch(Dispatchers.Main) {
                        debugInfo = "No debug information to save."
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Download Debug Info")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    VonageRedirectTroubleshooterTheme {
        Greeting()
    }
}
