import com.example.memorease.data.MemoryRequest
import com.example.memorease.data.QuestionResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.Call

interface QuestionApi {
    @POST("generate-question")
    fun generateQuestion(@Body request: MemoryRequest): Call<QuestionResponse>
}
