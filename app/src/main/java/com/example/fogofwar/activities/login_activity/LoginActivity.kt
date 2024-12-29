package com.example.fogofwar.activities.login_activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fogofwar.R
import com.example.fogofwar.activities.bottom_nav_activity.BottomNavActivity
import com.example.fogofwar.backend.BackendAPI
import com.example.fogofwar.backend.remotes.login.LoginReceiveRemote
import com.example.fogofwar.backend.remotes.register.RegisterReceiveRemote
import com.example.fogofwar.databinding.ActivityLoginBinding
import com.example.fogofwar.databinding.ActivityRegistraitionBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var phoneNumberView: TextView
    private lateinit var passwordView: TextView
    private lateinit var buttonToRegister: ImageButton
    private lateinit var buttonLogin: Button

    private lateinit var backendAPI: BackendAPI
    private lateinit var userPhoneNumber: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        phoneNumberView = binding.phoneNumber
        passwordView = binding.password
        buttonToRegister = binding.buttonToRegister
        buttonLogin = binding.buttonLogin

        val retrofit = Retrofit.Builder()
            .baseUrl("http://45.91.8.232:8081/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        backendAPI = retrofit.create(BackendAPI::class.java)

        buttonLogin.setOnClickListener {
            if (phoneNumberView.text.length == 0) {
                Toast.makeText(this, "Неверный формат номера телефона", Toast.LENGTH_LONG).show()
            }
            else if (passwordView.text != "") {
                CoroutineScope(Dispatchers.IO).launch {
                    val response = backendAPI.login(LoginReceiveRemote(phoneNumberView.text.toString(), passwordView.text.toString()))
                    if (response.isSuccessful) {
                        userPhoneNumber = phoneNumberView.text.toString()
                        withContext(Dispatchers.Main) {
                            saveLogin()
                            val intent = Intent(this@LoginActivity, BottomNavActivity::class.java)
                            intent.putExtra("user_phone_number", userPhoneNumber)
                            startActivity(intent)
                        }
                    }
                    else {
                        Toast.makeText(this@LoginActivity, "Такого пользователя не существует", Toast.LENGTH_LONG).show()
                    }
                }
            }
            else {
                Toast.makeText(this, "Введите корректные данные", Toast.LENGTH_LONG).show()
            }
        }

        buttonToRegister.setOnClickListener {
            finish()
        }
    }

    private fun saveLogin() {
        val sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("user_phone_number", userPhoneNumber)
        editor.apply()
    }
}