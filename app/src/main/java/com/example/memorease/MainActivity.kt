package com.example.memorease

import QuestionApi
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.memorease.data.MemoryRequest
import com.example.memorease.data.QuestionResponse
import com.example.memorease.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomnavigation.LabelVisibilityMode
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var api: QuestionApi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val retrofit = Retrofit.Builder()
            .baseUrl("https://web-production-2ba0.up.railway.app/") // senin Railway URL’in
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        api = retrofit.create(QuestionApi::class.java)

        // Örnek çağrı
        sendMemoryToApi("This is the beach where I built my first sandcastle.")

        replaceFragment(HomeFragment())

        binding.bottomNavigationView.setOnItemSelectedListener {
            when(it.itemId){
                R.id.home_icon -> replaceFragment(HomeFragment())
                R.id.leaderboard_icon -> replaceFragment(LeaderboardFragment())
                R.id.quiz_icon -> replaceFragment(QuizFragment())
                R.id.report_icon -> replaceFragment(ReportFragment())
                else ->{

                }
            }
            true
        }

    }

    private fun sendMemoryToApi(memoryText: String) {
        val request = MemoryRequest(memoryText)
        val call = api.generateQuestion(request)

        call.enqueue(object : Callback<QuestionResponse> {
            override fun onResponse(
                call: Call<QuestionResponse>,
                response: Response<QuestionResponse>
            ) {
                if (response.isSuccessful) {
                    val question = response.body()?.question
                    Log.d("LLM", "Question: $question")
                } else {
                    Log.e("LLM", "Error: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<QuestionResponse>, t: Throwable) {
                Log.e("LLM", "Network error: ${t.message}")
            }
        })
    }


    //method that will replace the fragments
    private fun replaceFragment(fragment: Fragment){

        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout,fragment)
        fragmentTransaction.commit()

    }
}