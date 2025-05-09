package com.example.tallertercer20

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.tallertercer20.ui.theme.TallerTercer20Theme
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TallerTercer20Theme {
                App()
            }
        }
    }
}

@Composable
fun App() {
    var isAuthenticated by remember { mutableStateOf(false) }
    var showRegister by remember { mutableStateOf(false) }
    var token by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var products by remember { mutableStateOf<List<String>>(emptyList()) }

    if (!isAuthenticated) {
        if (showRegister) {
            RegisterScreen(
                onRegister = { name, email, password ->
                    errorMessage = "El registro no está implementado aún"
                    showRegister = false
                },
                onToggle = { showRegister = false },
                errorMessage = errorMessage
            )
        } else {
            LoginScreen(
                onLogin = { email, password ->
                    loginUser(
                        email,
                        password,
                        onSuccess = { receivedToken ->
                            token = receivedToken
                            isAuthenticated = true
                            fetchProducts(receivedToken) { fetchedProducts ->
                                products = fetchedProducts
                            }
                        },
                        onError = { error ->
                            errorMessage = error
                        }
                    )
                },
                onToggle = { showRegister = true },
                errorMessage = errorMessage
            )
        }
    } else {
        ProductListScreen(products)
    }
}

fun loginUser(email: String, password: String, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val url = URL("https://api.escuelajs.co/api/v1/auth/login")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true

            val jsonBody = JSONObject()
            jsonBody.put("email", email)
            jsonBody.put("password", password)

            val outputWriter = OutputStreamWriter(connection.outputStream)
            outputWriter.write(jsonBody.toString())
            outputWriter.flush()

            val responseCode = connection.responseCode
            if (responseCode in 200..299) {
                val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonResponse = JSONObject(responseText)
                val accessToken = jsonResponse.getString("access_token")
                withContext(Dispatchers.Main) {
                    onSuccess(accessToken)
                }
            } else {
                val errorText = connection.errorStream?.bufferedReader()?.use { it.readText() } ?: "Error desconocido"
                withContext(Dispatchers.Main) {
                    onError("Error al iniciar sesión: $errorText")
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                onError("Error: ${e.localizedMessage}")
            }
        }
    }
}

fun fetchProducts(token: String, onResult: (List<String>) -> Unit) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val url = URL("https://api.escuelajs.co/api/v1/products")
            val connection = url.openConnection() as HttpURLConnection
            connection.setRequestProperty("Authorization", "Bearer $token")
            connection.connect()

            val response = connection.inputStream.bufferedReader().use { it.readText() }
            val jsonArray = JSONArray(response)
            val productList = mutableListOf<String>()

            for (i in 0 until jsonArray.length()) {
                val product = jsonArray.getJSONObject(i)
                val title = product.getString("title")
                val price = product.getDouble("price")
                val description = product.getString("description")
                val category = product.getJSONObject("category").getString("name")
                productList.add("$title - $price USD\nCategoría: $category\n$description")
            }

            withContext(Dispatchers.Main) {
                onResult(productList)
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                onResult(listOf("Error al cargar productos: ${e.localizedMessage}"))
            }
        }
    }
}

@Composable
fun LoginScreen(onLogin: (String, String) -> Unit, onToggle: () -> Unit, errorMessage: String?) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { onLogin(email, password) }, modifier = Modifier.fillMaxWidth()) {
            Text("Iniciar Sesión")
        }
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = onToggle) {
            Text("¿No tienes cuenta? Regístrate")
        }
        errorMessage?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = it, color = Color.Red)
        }
    }
}

@Composable
fun RegisterScreen(onRegister: (String, String, String) -> Unit, onToggle: () -> Unit, errorMessage: String?) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        TextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nombre") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { onRegister(name, email, password) }, modifier = Modifier.fillMaxWidth()) {
            Text("Registrarse")
        }
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = onToggle) {
            Text("¿Ya tienes cuenta? Inicia sesión")
        }
        errorMessage?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = it, color = Color.Red)
        }
    }
}

@Composable
fun ProductListScreen(products: List<String>) {
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        Text("Lista de Productos:", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn {
            items(products) { product ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                ) {
                    Text(product, modifier = Modifier.padding(16.dp))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewApp() {
    TallerTercer20Theme {
        App()
    }
}


