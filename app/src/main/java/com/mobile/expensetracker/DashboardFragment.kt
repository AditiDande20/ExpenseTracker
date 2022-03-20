package com.mobile.expensetracker

import android.app.ProgressDialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.mikephil.charting.utils.MPPointF
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings


class DashboardFragment : Fragment() {
    private lateinit var firestore: FirebaseFirestore
    private lateinit var categoryList : List<Category>
    private lateinit var pieChart :PieChart
    private lateinit var progressDialog :ProgressDialog
    private lateinit var empty_view :ConstraintLayout
    var totalExpenses : Float = 0.0f



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_dashboard, container, false)
        firestore = FirebaseFirestore.getInstance()
        firestore.firestoreSettings = FirebaseFirestoreSettings.Builder().build()

        pieChart = view.findViewById(R.id.pieChart)
        empty_view = view.findViewById(R.id.empty_view)

        getExpenses()

        return view
    }

    fun setPieChartData(entry: ArrayList<PieEntry>) {
        progressDialog.dismiss()

        val dataSet = PieDataSet(entry, "Number Of Expenses")
        dataSet.setDrawIcons(false)
        dataSet.sliceSpace = 3f
        dataSet.iconsOffset = MPPointF(0F, 40F)
        dataSet.selectionShift = 5f
        dataSet.setColors(*ColorTemplate.COLORFUL_COLORS)

        val data = PieData(dataSet)
        data.setValueTextSize(11f)
        data.setValueTextColor(Color.WHITE)
        pieChart.data = data
        pieChart.highlightValues(null)
        pieChart.invalidate()
        pieChart.animateXY(5000, 5000)
    }


    fun getExpenses() : Float{

        progressDialog = ProgressDialog(requireActivity())
        progressDialog.setTitle("Loading chart")
        progressDialog.setMessage("Please Wait")
        progressDialog.setCancelable(false)
        progressDialog.show()

        firestore
            .collection("Expense")
            .get()
            .addOnSuccessListener { document ->
                progressDialog.dismiss()
                try {
                    if (document != null) {
                        val expenseList = document.toObjects(Expense::class.java)

                        if(expenseList.isEmpty()){
                            pieChart.visibility = View.GONE
                            empty_view.visibility = View.VISIBLE
                        }
                        else{
                            pieChart.visibility = View.VISIBLE
                            empty_view.visibility = View.GONE
                            val entry = ArrayList<PieEntry>()
                            for(i in expenseList.indices){
                                entry.add(PieEntry(expenseList[i].expenseAmount.toFloat(),expenseList[i].expenseName))
                            }
                            setPieChartData(entry)
                        }
                    }
                } catch (ex: Exception) {
                    ex.message?.let {  }
                }
            }
        return totalExpenses

    }

}