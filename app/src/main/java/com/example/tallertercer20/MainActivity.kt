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
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

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


data class AuthResponse(val access_token: String)
data class User(val name: String, val email: String, val password: String, val avatar: String = "https://api.lorem.space/image/face?w=150&h=150")
data class Product(val id: Int, val title: String, val price: Double, val description: String, val category: Category)
data class Category(val id: Int, val name: String, val image: String)


interface ApiService {
    @POST("auth/login")
    suspend fun login(@Body credentials: Map<String, String>): AuthResponse

    @POST("users/")
    suspend fun register(@Body user: User): AuthResponse

    @GET("products")
    suspend fun getProducts(@Header("Authorization") token: String): List<Product>
}


object RetrofitClient {
    private const val BASE_URL = "https://api.escuelajs.co/api/v1/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
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
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val response = RetrofitClient.apiService.register(
                                User(name, email, password)
                            )
                            withContext(Dispatchers.Main) {
                                errorMessage = "Registro exitoso. Ahora inicia sesión."
                                showRegister = false
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                errorMessage = "Error al registrar: ${e.message}"
                            }
                        }
                    }
                },
                onToggle = { showRegister = false },
                errorMessage = errorMessage
            )
        } else {
            LoginScreen(
                onLogin = { email, password ->
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val response = RetrofitClient.apiService.login(
                                mapOf("email" to email, "password" to password)
                            )
                            withContext(Dispatchers.Main) {
                                token = response.access_token
                                isAuthenticated = true

                              
                                CoroutineScope(Dispatchers.IO).launch {
                                    try {
                                        val productsResponse = RetrofitClient.apiService.getProducts("Bearer ${response.access_token}")
                                        val productStrings = productsResponse.map { product ->
                                            "${product.title} - ${product.price} USD\nCategoría: ${product.category.name}\n${product.description}"
                                        }
                                        withContext(Dispatchers.Main) {
                                            products = productStrings
                                        }
                                    } catch (e: Exception) {
                                        withContext(Dispatchers.Main) {
                                            products = listOf("Error al cargar productos: ${e.message}")
                                        }
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                errorMessage = "Error al iniciar sesión: ${e.message}"
                            }
                        }
                    }
                },
                onToggle = { showRegister = true },
                errorMessage = errorMessage
            )
        }
    }
    else {
        ProductListScreen(
            products = products,
            onBack = { isAuthenticated = false }
        )
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
            Text(text = it, color = if (it.contains("exitoso", true)) Color.Green else Color.Red)
        }
    }
}

@Composable
fun ProductListScreen(products: List<String>, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Button(
            onClick = onBack,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Text("Regresar al login")
        }

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