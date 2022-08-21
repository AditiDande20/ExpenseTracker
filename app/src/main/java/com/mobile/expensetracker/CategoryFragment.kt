package com.mobile.expensetracker

import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.SetOptions


class CategoryFragment : Fragment() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var recycler_view_category: RecyclerView
    private lateinit var categoryList:  MutableList<Category>
    private lateinit var ed_category_name: EditText
    private lateinit var ed_budget: EditText
    private lateinit var btn_cancel: Button
    private lateinit var btn_add: Button
    private lateinit var dialog: BottomSheetDialog
    private lateinit var empty_view: ConstraintLayout
    private lateinit var constraint_layout: ConstraintLayout
    private lateinit var progressDialog: ProgressDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_category, container, false)

        setHasOptionsMenu(true)

        firestore = FirebaseFirestore.getInstance()
        firestore.firestoreSettings = FirebaseFirestoreSettings.Builder().build()

        recycler_view_category=view.findViewById<RecyclerView>(R.id.recycler_view_category)
        constraint_layout=view.findViewById<ConstraintLayout>(R.id.constraint_layout)
        empty_view=view.findViewById<ConstraintLayout>(R.id.empty_view)
        val fab_add_category=view.findViewById<FloatingActionButton>(R.id.fab_add_category)

        categoryList = ArrayList()

        readDataFromFirestore()

        fab_add_category.setOnClickListener {
            showBottomSheetDialog()
        }

        return view
    }

    private fun showBottomSheetDialog() {
        dialog = activity?.let { BottomSheetDialog(it) }!!

        val view = layoutInflater.inflate(R.layout.bottom_sheet_add_category, null)

        ed_category_name = view.findViewById<EditText>(R.id.ed_category_name)
        ed_budget = view.findViewById<EditText>(R.id.ed_budget)
        btn_cancel = view.findViewById<Button>(R.id.btn_cancel)
        btn_add = view.findViewById<Button>(R.id.btn_add)

        btn_cancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.setCancelable(false)
        dialog.setContentView(view)
        dialog.show()

        btn_add.setOnClickListener {
            addCategoryToFirestore(ed_category_name.text.toString(), ed_budget.text.toString())

        }

    }

    private fun addCategoryToFirestore(categoryName: String, categoryBudget: String) {

        progressDialog = ProgressDialog(requireActivity())
        progressDialog.setTitle("Saving Category")
        progressDialog.setMessage("Please Wait")
        progressDialog.setCancelable(false)
        progressDialog.show()

        val id: String = firestore.collection("Category").document().id

        val category = Category(id,categoryName, categoryBudget)
        firestore.collection("Category")
                 .document(id)
                 .set(category, SetOptions.merge())
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(activity, "Category Added", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                readDataFromFirestore()

            }
            .addOnFailureListener {
                progressDialog.dismiss()
                Log.e("Aditi===>","exception in categiory ::: ${it.message}")
                Toast.makeText(activity, "Something went wrong", Toast.LENGTH_SHORT).show()
            }
    }

    private fun readDataFromFirestore() {
        progressDialog = ProgressDialog(requireActivity())
        progressDialog.setTitle("Fetching Data")
        progressDialog.setMessage("Please Wait")
        progressDialog.setCancelable(false)
        progressDialog.show()

        firestore
            .collection("Category")
            .get()
            .addOnSuccessListener { document ->
                progressDialog.dismiss()
                try {
                    if (document != null) {
                        categoryList = document.toObjects(Category::class.java)
                        setData(categoryList)
                    } else {
                        Toast.makeText(activity, "No such document!", Toast.LENGTH_LONG).show()
                    }
                } catch (ex: Exception) {
                    ex.message?.let { }
                }
            }.addOnFailureListener { e ->
                progressDialog.dismiss()
            }
    }

    fun setData(categoryList: MutableList<Category>) {

        val categoryAdapter = context?.let { CategoryAdapter(it, categoryList) }
        recycler_view_category.adapter = categoryAdapter
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