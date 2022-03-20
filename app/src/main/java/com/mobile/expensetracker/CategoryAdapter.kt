package com.mobile.expensetracker

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

class CategoryAdapter(val context : Context,val categoryList :  MutableList<Category>) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    private lateinit var firestore: FirebaseFirestore
    var totalSpent : Int = 0
    var totalExpenses : Int = 0

    class CategoryViewHolder (itemView : View) : RecyclerView.ViewHolder(itemView){

        val txt_category_name = itemView.findViewById<TextView>(R.id.txt_category_name)
        val txt_budget = itemView.findViewById<TextView>(R.id.txt_budget)
        val txt_spent = itemView.findViewById<TextView>(R.id.txt_spent)
        val txt_items = itemView.findViewById<TextView>(R.id.txt_items)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context).
        inflate(R.layout.category_item_layout, parent, false) as View
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {

        val currentCategory = categoryList[position]
        firestore = FirebaseFirestore.getInstance()
        firestore.firestoreSettings = FirebaseFirestoreSettings.Builder().build()

        getExpenses(currentCategory.categoryName,holder,currentCategory.categoryBudget)

        holder.txt_category_name.text = currentCategory.categoryName
        holder.txt_budget.text = currentCategory.categoryBudget


    }

    override fun getItemCount(): Int {
        return categoryList.size
    }

    fun getExpenses(expenseCategory : String, holder : CategoryViewHolder, expenseBudget : String) : Int{
        firestore
            .collection("Expense")
            .whereEqualTo("expenseCategory", expenseCategory)
            .get()
            .addOnSuccessListener { document ->
                try {
                    if (document != null) {
                        val expenseList = document.toObjects(Expense::class.java)
                        totalExpenses=expenseList.size
                        holder.txt_items.text = "$totalExpenses Expenses"

                        totalSpent = 0
                        for(i in 0 until expenseList.size){
                            totalSpent += (expenseList[i].expenseAmount).toInt()
                        }
                        holder.txt_spent.text = totalSpent.toString()

                        val budget : Int = expenseBudget.toInt()
                        if(totalSpent < budget){
                            holder.txt_spent.setTextColor(ContextCompat.getColor(context,
                                R.color.colorGreen
                            ))
                        }
                        else if(totalSpent>budget){
                            holder.txt_spent.setTextColor(ContextCompat.getColor(context,
                                R.color.colorRed
                            ))
                        }
                        else if (totalSpent==budget){
                            holder.txt_spent.setTextColor(ContextCompat.getColor(context,
                                R.color.colorYellow
                            ))
                        }
                        else
                        {
                            holder.txt_spent.setTextColor(ContextCompat.getColor(context,
                                R.color.colorPrimary
                            ))

                        }

                    }
                } catch (ex: Exception) {
                    ex.message?.let {  }
                }
            }

        return totalExpenses
    }

}