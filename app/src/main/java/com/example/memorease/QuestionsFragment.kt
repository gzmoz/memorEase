package com.example.memorease

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.memorease.data.MemoryRequest
import com.example.memorease.data.QuestionResponse
import com.example.memorease.databinding.FragmentQuestionsBinding
import com.example.memorease.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class QuestionsFragment : Fragment() {

    private var _binding: FragmentQuestionsBinding? = null
    // This property is only valid between onCreateView and
// onDestroyView.
    private val binding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentQuestionsBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val request = MemoryRequest("This is my favorite memory.")
        RetrofitClient.api.generateQuestion(request).enqueue(object : Callback<QuestionResponse> {
            override fun onResponse(call: Call<QuestionResponse>, response: Response<QuestionResponse>) {
                if (response.isSuccessful) {
                    val question = response.body()?.question
                    Log.d("QUIZ", "Soru geldi: $question")
                    // 🟢 Soruyu UI'da göster:
                    binding.tvQuestionText.text = question
                }else {
                    Log.e("QUIZ", "Cevap başarısız: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<QuestionResponse>, t: Throwable) {
                Log.e("QUIZ", "API Hatası: ${t.message}")
            }
        })
    }
}