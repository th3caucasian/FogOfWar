package com.example.fogofwar.activities.registration_activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fogofwar.R
import com.example.fogofwar.activities.bottom_nav_activity.BottomNavActivity
import com.example.fogofwar.activities.login_activity.LoginActivity
import com.example.fogofwar.backend.BackendAPI
import com.example.fogofwar.backend.remotes.register.RegisterReceiveRemote
import com.example.fogofwar.databinding.ActivityRegistraitionBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.HTTP

class RegistraitionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegistraitionBinding
    private lateinit var phoneNumberView: TextView
    private lateinit var loginView: TextView
    private lateinit var passwordView: TextView
    private lateinit var toLoginView: TextView
    private lateinit var buttonRegister: Button

    private lateinit var backendAPI: BackendAPI
    private lateinit var userPhoneNumber: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE)
        userPhoneNumber = sharedPreferences.getString("user_phone_number", "null")!!
        if (userPhoneNumber != "null") {
            val intent = Intent(this@RegistraitionActivity, BottomNavActivity::class.java)
            intent.putExtra("user_phone_number", userPhoneNumber)
            startActivity(intent)
            finish()
        }

        binding = ActivityRegistraitionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        phoneNumberView = binding.phoneNumber
        loginView = binding.login
        passwordView = binding.password
        toLoginView = binding.toLogin
        buttonRegister = binding.buttonRegister

        val retrofit = Retrofit.Builder()
            .baseUrl("http://45.91.8.232:8081/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        backendAPI = retrofit.create(BackendAPI::class.java)

        buttonRegister.setOnClickListener {
            if (phoneNumberView.text.length != 11) {
                Toast.makeText(this, "Неверный формат номера телефона", Toast.LENGTH_LONG).show()
            }
            else if (loginView.text.length < 2) {
                Toast.makeText(this, "Имя пользователя должно состоять минимум из двух символов", Toast.LENGTH_LONG).show()
            }
            else if (passwordView.text != "") {
                CoroutineScope(Dispatchers.IO).launch {
                    val response = backendAPI.register(RegisterReceiveRemote(loginView.text.toString(), phoneNumberView.text.toString(), passwordView.text.toString()))
                    if (response.isSuccessful) {
                        userPhoneNumber = phoneNumberView.text.toString()
                        withContext(Dispatchers.Main) {
                            saveLogin()
                            val intent = Intent(this@RegistraitionActivity, BottomNavActivity::class.java)
                            intent.putExtra("user_phone_number", userPhoneNumber)
                            startActivity(intent)
                        }
                    }
                    else {
                        Toast.makeText(this@RegistraitionActivity, "Такой пользователь уже существует", Toast.LENGTH_LONG).show()
                    }
                }
            }
            else {
                Toast.makeText(this, "Введите корректный пароль", Toast.LENGTH_LONG).show()
            }
        }

        toLoginView.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    private fun saveLogin() {
        val sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("user_phone_number", userPhoneNumber)
        editor.apply()
    }
}