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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.example.tallertercer20.ui.theme.TallerTercer20Theme

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
                    errorMessage = "Usuario registrado" // Simulación de registro
                    showRegister = false
                },
                onToggle = { showRegister = false },
                errorMessage = errorMessage
            )
        } else {
            LoginScreen(
                onLogin = { email, password ->
                    // Simulación de inicio de sesión
                    isAuthenticated = true
                    token = "mocked_token" // Token simulado
                    products = listOf("Producto 1", "Producto 2", "Producto 3")
                },
                onToggle = { showRegister = true },
                errorMessage = errorMessage
            )
        }
    } else {
        ProductListScreen(products)
    }
}

@Composable
fun LoginScreen(onLogin: (String, String) -> Unit, onToggle: () -> Unit, errorMessage: String?) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
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
        modifier = Modifier.fillMaxSize().padding(16.dp),
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
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
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
fun PreviewLoginScreen() {
    TallerTercer20Theme {
        LoginScreen(
            onLogin = { _, _ -> },
            onToggle = {},
            errorMessage = null
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewRegisterScreen() {
    TallerTercer20Theme {
        RegisterScreen(
            onRegister = { _, _, _ -> },
            onToggle = {},
            errorMessage = null
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewProductListScreen() {
    TallerTercer20Theme {
        ProductListScreen(
            products = listOf("Camiseta", "Zapatos", "Gorra", "Chaqueta", "Mochila")
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewApp() {
    TallerTercer20Theme {
        App() // OJO: Puede dar errores si hay lógica compleja no soportada en preview
    }
}
