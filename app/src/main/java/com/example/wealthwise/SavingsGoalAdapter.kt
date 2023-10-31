package com.example.wealthwise

import android.app.AlertDialog
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.wealthwise.DataClass.AddCashSavingsGoalRequest
import com.example.wealthwise.DataClass.SavingsGoal
import com.example.wealthwise.DataClass.SavingsGoalRequest
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SavingsGoalAdapter(private val savingsGoals: MutableList<SavingsGoal>) :
    RecyclerView.Adapter<SavingsGoalAdapter.SavingsGoalViewHolder>() {

    private val progressMap = mutableMapOf<Int, Int>()
    private val BASE_URL = "http://10.0.2.2:8080"

    inner class SavingsGoalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.goalTitleTextView)
        val currentAmountTextView: TextView = itemView.findViewById(R.id.currentAmountTextView)
        val targetAmountTextView: TextView = itemView.findViewById(R.id.targetAmountTextView)
        val savingsProgressBar: ProgressBar = itemView.findViewById(R.id.savingsProgressBar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SavingsGoalViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.savings_goal_item, parent, false)
        return SavingsGoalViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: SavingsGoalViewHolder, position: Int) {
        val savingsGoal = savingsGoals[position]
        holder.titleTextView.text = savingsGoal.title
        holder.currentAmountTextView.text = "Kwota zaoszczędzona: ${savingsGoal.currentAmount} PLN"
        holder.targetAmountTextView.text = "Cel oszczędzania: ${savingsGoal.targetAmount} PLN"

        val progress = progressMap[position] ?: 0
        holder.savingsProgressBar.progress = progress

        if (progress >= 100) {
            savingsGoal.active = false
        }

        if (!savingsGoal.active) {
            holder.itemView.visibility = View.GONE
            Toast.makeText(holder.itemView.context, "Oszczędzanie na ten cel zostało zakończone", Toast.LENGTH_SHORT).show()
        } else {
            holder.itemView.visibility = View.VISIBLE
        }

        holder.itemView.setOnClickListener {
            showEditSavingsGoalDialog(holder.adapterPosition, holder)
        }
    }

    override fun getItemCount(): Int {
        return savingsGoals.size
    }

    fun updateProgress(goalIndex: Int, progress: Int) {
        progressMap[goalIndex] = progress
        notifyItemChanged(goalIndex)
    }

    private fun showEditSavingsGoalDialog(position: Int, holder: SavingsGoalViewHolder) {
        val tokenManager = TokenManager(holder.itemView.context)
        val savingsGoal = savingsGoals[position]

        val builder = AlertDialog.Builder(holder.itemView.context, R.style.AlertDialogTheme)
        builder.setTitle("Edytuj cel oszczędzania")

        val savingsInput = EditText(holder.itemView.context)
        savingsInput.hint = "Podaj kwotę oszczędności"
        savingsInput.inputType = InputType.TYPE_CLASS_NUMBER
        savingsInput.setBackgroundResource(R.drawable.blue_border)
        savingsInput.setTextColor(holder.itemView.context.resources.getColor(android.R.color.black))
        builder.setView(savingsInput)

        builder.setPositiveButton("Zapisz") { _, _ ->
            val savingsAmount = savingsInput.text.toString()
            if (savingsAmount.isNotEmpty()) {
                val newSavings = savingsAmount.toDouble()
                val updatedCurrentAmount = savingsGoal.currentAmount + newSavings
                val updatedSavingsGoal = savingsGoal.copy(currentAmount = updatedCurrentAmount)
                savingsGoals[position] = updatedSavingsGoal
                val percentProgress = (updatedCurrentAmount / updatedSavingsGoal.targetAmount * 100).toInt()
                updateProgress(position, percentProgress)
                holder.savingsProgressBar.progress = percentProgress

                val interceptor = HttpLoggingInterceptor()
                interceptor.level = HttpLoggingInterceptor.Level.BODY

                val client = OkHttpClient.Builder()
                    .addInterceptor(interceptor)
                    .build()

                val retrofit = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build()
                val authHeader = "Bearer " + tokenManager.getTokenAccess().toString()
                val apiService = retrofit.create(ApiService::class.java)

                val addCashSavingsGoalRequest = AddCashSavingsGoalRequest(tokenManager.getTokenAccess().toString(), updatedSavingsGoal.title, updatedSavingsGoal.currentAmount)

                val call = apiService.addCashSavingsGoal(authHeader, addCashSavingsGoalRequest)

                call.enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(call: Call<ResponseBody>,
                                            response: Response<ResponseBody>
                    ) {
                        if (response.isSuccessful) {
                            Toast.makeText(holder.itemView.context, "Pomyślnie zwiększsono oszczędzone środki na dany cel", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(holder.itemView.context, "Nie udało się dodać oszczędności", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        Toast.makeText(holder.itemView.context, "Nie udało się dodać oszczędności", Toast.LENGTH_SHORT).show()
                    }
                    })


            }
        }

        builder.setNegativeButton("Anuluj") { dialog, _ ->
            dialog.dismiss()
        }

        builder.show()
    }
}