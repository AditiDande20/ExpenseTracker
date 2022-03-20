package com.mobile.expensetracker

import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.github.curioustechizen.ago.RelativeTimeTextView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.SetOptions
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap


class ExpenseAdapter(val context: Context, val expenseList: MutableList<Expense>,val expenseFragment: ExpenseFragment) : RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var dialog: BottomSheetDialog
    private lateinit var progressDialog: ProgressDialog
    private val myCalendar = Calendar.getInstance()
    val myFormat = "yyyy-MM-dd"
    val dateFormat = SimpleDateFormat(myFormat, Locale.US)


    class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        val txt_expense_name = itemView.findViewById<TextView>(R.id.txt_expense_name)
        val txt_category = itemView.findViewById<TextView>(R.id.txt_category)
        val txt_date = itemView.findViewById<RelativeTimeTextView>(R.id.txt_date)
        val chip_amt = itemView.findViewById<Chip>(R.id.chip_amt)
        val img_edit = itemView.findViewById<ImageView>(R.id.img_edit)
        val img_delete = itemView.findViewById<ImageView>(R.id.img_delete)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view = LayoutInflater.from(parent.context).
        inflate(R.layout.expense_item_layout, parent, false) as View
        return ExpenseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {

        firestore = FirebaseFirestore.getInstance()
        firestore.firestoreSettings = FirebaseFirestoreSettings.Builder().build()

        val currentExpense = expenseList[position]

        holder.txt_expense_name.text = currentExpense.expenseName
        holder.txt_category.text = "Category : ${currentExpense.expenseCategory}"

        holder.txt_date.setReferenceTime(currentExpense.expenseDate.time)
        holder.chip_amt.text = "₹ "+currentExpense.expenseAmount

        holder.img_edit.setOnClickListener {
            showBottomSheetDialog(currentExpense)
        }

        holder.img_delete.setOnClickListener {
            deleteExpense(currentExpense.id,currentExpense.expenseCategory)
        }

    }

    private fun deleteExpense(id: String,expenseCategory : String) {

        progressDialog = ProgressDialog(context)
        progressDialog.setTitle("Deleting Expense")
        progressDialog.setMessage("Please Wait")
        progressDialog.setCancelable(false)
        progressDialog.show()

        firestore.collection("Expense")
            .document(id)
            .delete()
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(context,"Expense Deleted",Toast.LENGTH_SHORT).show()
                if(expenseCategory == "All"){
                    expenseFragment.readExpenseFromFirestore("")
                }
                else
                {
                    expenseFragment.readExpenseFromFirestore(expenseCategory)
                }
            }
            .addOnFailureListener {
                progressDialog.dismiss()
                Toast.makeText(context,"Something went wrong",Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateExpenseEntry(
        expenseName: String,
        expenseAmount: String,
        expenseDate: String,
        expenseCategory: String,
        id: String
    ) {

        progressDialog = ProgressDialog(context)
        progressDialog.setTitle("Updating Expense")
        progressDialog.setMessage("Please Wait")
        progressDialog.setCancelable(false)
        progressDialog.show()


        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
        val date = simpleDateFormat.parse(expenseDate)

        val expense = HashMap<String,Any>()
        expense["expenseName"] = expenseName
        expense["expenseAmount"] = expenseAmount
        expense["expenseDate"] = date
        expense["expenseCategory"] = expenseCategory

        firestore.collection("Expense")
            .document(id)
            .update(expense)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(context, "Expense Updated", Toast.LENGTH_SHORT).show()
                dialog.dismiss()

                expenseFragment.readExpenseFromFirestore(expenseCategory)

            }
            .addOnFailureListener {
                progressDialog.dismiss()
                Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show()
            }


    }

    private fun showBottomSheetDialog(currentExpense: Expense) {
        dialog = BottomSheetDialog(context)

        val view: View = (context as FragmentActivity).layoutInflater.inflate(
            R.layout.bottom_sheet_add_expense,
            null
        )

        val txt_category = view.findViewById<TextView>(R.id.txt_category)
        val ed_expense_name = view.findViewById<EditText>(R.id.ed_expense_name)
        val ed_amount = view.findViewById<EditText>(R.id.ed_amount)
        val ed_date = view.findViewById<EditText>(R.id.ed_date)
        val spinner_category = view.findViewById<Spinner>(R.id.spinner_category)
        val btn_cancel = view.findViewById<Button>(R.id.btn_cancel)
        val btn_update = view.findViewById<Button>(R.id.btn_add)

        btn_update.text = "UPDATE"
        txt_category.visibility = View.VISIBLE
        spinner_category.visibility = View.GONE

        ed_expense_name.setText(currentExpense.expenseName)
        ed_amount.setText(currentExpense.expenseAmount)
        ed_date.setText(dateFormat.format(currentExpense.expenseDate))

        txt_category.text = currentExpense.expenseCategory

        btn_cancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.setCancelable(false)
        dialog.setContentView(view)
        dialog.show()

        btn_update.setOnClickListener {
            updateExpenseEntry(
                ed_expense_name.text.toString(),
                ed_amount.text.toString(),
                ed_date.text.toString(),
                txt_category.text.toString(),
                currentExpense.id
            )
        }

        val date = DatePickerDialog.OnDateSetListener { view, year, month, day ->
                myCalendar.set(Calendar.YEAR, year)
                myCalendar.set(Calendar.MONTH, month)
                myCalendar.set(Calendar.DAY_OF_MONTH, day)
                updateLabel(ed_date)
            }

        ed_date.setOnClickListener {
            DatePickerDialog(
                context,
                date,
                myCalendar.get(Calendar.YEAR),
                myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

    }


    override fun getItemCount(): Int {
        return expenseList.size
    }

    private fun updateLabel(ed_date: EditText) {

        ed_date.setText(dateFormat.format(myCalendar.getTime()))
    }


}