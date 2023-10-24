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

class SavingsGoalAdapter(private val savingsGoals: MutableList<SavingsGoal>) :
    RecyclerView.Adapter<SavingsGoalAdapter.SavingsGoalViewHolder>() {

    private val progressMap = mutableMapOf<Int, Int>()

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

            }
        }

        builder.setNegativeButton("Anuluj") { dialog, _ ->
            dialog.dismiss()
        }

        builder.show()
    }
}