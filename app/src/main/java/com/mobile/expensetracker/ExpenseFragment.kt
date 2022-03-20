package com.mobile.expensetracker

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import java.text.SimpleDateFormat
import java.util.*


class ExpenseFragment : Fragment() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var recycler_view_category: RecyclerView
    private lateinit var expenseList: MutableList<Expense>
    private lateinit var dialog: BottomSheetDialog
    private lateinit var ed_expense_name: EditText
    private lateinit var ed_amount: EditText
    private lateinit var ed_date: EditText
    private lateinit var spinner_category: Spinner
    private lateinit var sort_category: Spinner
    private lateinit var btn_cancel: Button
    private lateinit var btn_add: Button
    private lateinit var categories: MutableList<String>
    private lateinit var categoriesAll: MutableList<String>
    private lateinit var empty_view: ConstraintLayout
    private lateinit var constraint_layout: ConstraintLayout
    private lateinit var progressDialog: ProgressDialog

    private val myCalendar = Calendar.getInstance()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_category, container, false)

        firestore = FirebaseFirestore.getInstance()
        firestore.firestoreSettings = FirebaseFirestoreSettings.Builder().build()

        setHasOptionsMenu(true)

        recycler_view_category = view.findViewById(R.id.recycler_view_category)
        constraint_layout = view.findViewById<ConstraintLayout>(R.id.constraint_layout)
        empty_view = view.findViewById<ConstraintLayout>(R.id.empty_view)
        val fab_add_category = view.findViewById<FloatingActionButton>(R.id.fab_add_category)
        val linear_sorting = view.findViewById<LinearLayout>(R.id.linear_sorting)
        sort_category = view.findViewById(R.id.sort_category)

        linear_sorting.visibility = View.VISIBLE

        readCategoryFromFirestore(sort_category)

        fab_add_category.setOnClickListener {
            showBottomSheetDialog()
        }

        return view
    }

    private fun showBottomSheetDialog() {
        dialog = activity?.let { BottomSheetDialog(it) }!!

        val view = layoutInflater.inflate(R.layout.bottom_sheet_add_expense, null)

        ed_expense_name = view.findViewById(R.id.ed_expense_name)
        ed_amount = view.findViewById(R.id.ed_amount)
        ed_date = view.findViewById(R.id.ed_date)
        spinner_category = view.findViewById(R.id.spinner_category)
        btn_cancel = view.findViewById(R.id.btn_cancel)
        btn_add = view.findViewById(R.id.btn_add)

        setSpinnerAdapter(spinner_category, categories, "")

        btn_cancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.setCancelable(false)
        dialog.setContentView(view)
        dialog.show()

        btn_add.setOnClickListener {
            addExpenseToFirestore(
                ed_expense_name.text.toString(),
                ed_amount.text.toString(),
                ed_date.text.toString(),
                spinner_category.selectedItem.toString()
            )
        }

        val date =
            OnDateSetListener { view, year, month, day ->
                myCalendar.set(Calendar.YEAR, year)
                myCalendar.set(Calendar.MONTH, month)
                myCalendar.set(Calendar.DAY_OF_MONTH, day)
                updateLabel()
            }
        ed_date.setOnClickListener {
            DatePickerDialog(
                requireActivity(),
                date,
                myCalendar.get(Calendar.YEAR),
                myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

    }

    private fun updateLabel() {
        val myFormat = "yyyy-MM-dd"
        val dateFormat = SimpleDateFormat(myFormat, Locale.US)
        ed_date.setText(dateFormat.format(myCalendar.getTime()))
    }

    private fun addExpenseToFirestore(
        expenseName: String,
        expenseAmount: String,
        expenseDate: String,
        expenseCategory: String
    ) {

        progressDialog = ProgressDialog(requireActivity())
        progressDialog.setTitle("Saving Expense")
        progressDialog.setMessage("Please Wait")
        progressDialog.setCancelable(false)
        progressDialog.show()

        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
        val date = simpleDateFormat.parse(expenseDate)
        val id: String = firestore.collection("Expense").document().id

        val expense = Expense(id, expenseName, expenseCategory, expenseAmount, date!!)
        firestore.collection("Expense")
            .document(id)
            .set(expense, SetOptions.merge())
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(activity, "Expense Added", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                if (sort_category.selectedItem.toString().equals("All")) {
                    readExpenseFromFirestore("")
                } else {
                    readExpenseFromFirestore(sort_category.selectedItem.toString())
                }

            }
            .addOnFailureListener {
                progressDialog.dismiss()
                Toast.makeText(activity, "Something went wrong", Toast.LENGTH_SHORT).show()
            }

    }

    fun readExpenseFromFirestore(condition: String) {

        if (condition.isNotEmpty()) {

            firestore
                .collection("Expense")
                .whereEqualTo("expenseCategory", condition)
                .orderBy("expenseDate", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener { document ->
                    progressDialog.dismiss()
                    try {
                        if (document != null) {
                            expenseList = document.toObjects(Expense::class.java)
                            setData(expenseList)

                        } else {
                            Toast.makeText(activity, "No such document!", Toast.LENGTH_LONG).show()
                        }
                    } catch (ex: Exception) {
                        ex.message?.let {  }
                    }
                }.addOnFailureListener { e ->
                    progressDialog.dismiss()
                }
        } else {
            firestore
                .collection("Expense")
                .orderBy("expenseDate", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener { document ->
                    progressDialog.dismiss()
                    try {
                        if (document != null) {
                            expenseList = document.toObjects(Expense::class.java)
                            setData(expenseList)

                        } else {
                            Toast.makeText(activity, "No such document!", Toast.LENGTH_LONG).show()
                        }
                    } catch (ex: Exception) {
                        ex.message?.let {  }
                    }
                }.addOnFailureListener { e ->
                    progressDialog.dismiss()
                }
        }


    }

    private fun setSpinnerAdapter(
        spinnerCategory: Spinner?,
        categories: MutableList<String>,
        flag: String
    ) {


        val adapter = ArrayAdapter(
            requireActivity(),
            android.R.layout.simple_spinner_dropdown_item, categories.reversed()
        )
        spinnerCategory?.adapter = adapter

        spinnerCategory?.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View, position: Int, id: Long
            ) {
                spinnerCategory?.setSelection(position)
                if (flag.isNotEmpty() && flag == "Category") {

                    if (spinnerCategory?.selectedItem.toString().equals("All")) {
                        readExpenseFromFirestore("")

                    } else {
                        readExpenseFromFirestore(spinnerCategory?.selectedItem.toString())

                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // write code to perform some action
            }
        }
    }

    private fun readCategoryFromFirestore(spinner_category: Spinner) {

        progressDialog = ProgressDialog(requireActivity())
        progressDialog.setTitle("Fetching Data")
        progressDialog.setMessage("Please Wait")
        progressDialog.setCancelable(false)
        progressDialog.show()

        firestore
            .collection("Category")
            .get()
            .addOnSuccessListener { document ->
                try {
                    if (document != null) {
                        val categoryList = document.toObjects(Category::class.java)
                        /* categories = Array(categoryList.size) { i -> "" }
                         categoriesAll = Array(4) { i -> "All" }*/
                        categories = mutableListOf()
                        categoriesAll = mutableListOf()
                        for (i in 0 until categoryList.size) {
                            categories.add(i, categoryList[i].categoryName)
                        }
                        categoriesAll.addAll(categories)
                        categoriesAll.add("All")

                        setSpinnerAdapter(spinner_category, categoriesAll, "Category")

                    } else {
                        Toast.makeText(activity, "No such document!", Toast.LENGTH_LONG).show()
                    }
                } catch (ex: Exception) {
                    progressDialog.dismiss()
                    ex.message?.let {  }
                }
            }.addOnFailureListener { e ->
                progressDialog.dismiss()
            }
    }

    private fun setData(expenseList: MutableList<Expense>) {

        val expenseAdapter = context?.let {
            ExpenseAdapter(
                it,
                expenseList,
                this
            )
        }
        recycler_view_category.adapter = expenseAdapter
        recycler_view_category.layoutManager = LinearLayoutManager(activity)

        if (recycler_view_category.adapter?.itemCount!! > 0) {
            constraint_layout.visibility = View.VISIBLE
            empty_view.visibility = View.GONE
        } else {
            constraint_layout.visibility = View.GONE
            empty_view.visibility = View.VISIBLE
        }
    }


}