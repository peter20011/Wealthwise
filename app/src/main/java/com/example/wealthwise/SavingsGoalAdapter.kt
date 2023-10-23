package com.example.wealthwise

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SavingsGoalAdapter(private val savingsGoals: List<SavingsGoal>) :
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

        val progress = progressMap[position] ?: 0 // Pobieranie postępu z mapy lub ustawienie domyślnej wartości 0
        holder.savingsProgressBar.progress = progress

        if (progress >= 100) {
            savingsGoal.active = false
        }

        if (!savingsGoal.active) {
            holder.itemView.visibility = View.GONE
        } else {
            holder.itemView.visibility = View.VISIBLE
        }
    }

    override fun getItemCount(): Int {
        return savingsGoals.size
    }

    fun updateProgress(goalIndex: Int, progress: Int) {
        progressMap[goalIndex] = progress
        notifyItemChanged(goalIndex)
    }
}