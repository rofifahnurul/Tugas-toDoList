package id.ac.unhas.todolist.main

import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import id.ac.unhas.todolist.R
import id.ac.unhas.todolist.database.ToDoList
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: ViewModel
    private lateinit var adapter: Adapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        listRecycler.layoutManager = LinearLayoutManager(this)
        adapter = Adapter(this) { toDoList, i ->
            showAlertMenu(toDoList)
        }
        listRecycler.adapter = adapter

        viewModel = ViewModelProvider(this).get(ViewModel::class.java)
        viewModel.getList()?.observe(this, Observer {
            adapter.setList(it)
        })

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.addMenu -> showAlertDialogAdd()
        }

        return super.onOptionsItemSelected(item)
    }

    private fun showAlertDialogAdd() {
        val alert = AlertDialog.Builder(this)

        val editText = EditText(applicationContext)
        editText.hint = "Enter your to do list"

        alert.setTitle("To Do List")
        alert.setView(editText)

        alert.setPositiveButton("Save") { dialog, _ ->
            viewModel.insertList(
                ToDoList(toDoList = editText.text.toString())
            )
            dialog.dismiss()
        }

        alert.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        alert.show()
    }

    private fun showAlertMenu(toDoList: ToDoList) {

        val messageDelete="To Do List telah dihapus"
        val items = arrayOf("Edit", "Delete")
        val builder =
            AlertDialog.Builder(this)
        builder.setItems(items) { dialog, which ->
            // the user clicked on colors[which]
            when (which) {
                0 -> {
                    showAlertDialogEdit(toDoList)
                }
                1 -> {
                    viewModel.deleteList(toDoList)
                    val toast = Toast.makeText(this, messageDelete, Toast.LENGTH_SHORT)
                    toast.setGravity(Gravity.TOP, 0,-20)
                    toast.show()
                }
            }
        }
        builder.show()
    }

    private fun showAlertDialogEdit(toDoList: ToDoList) {
        val messageUpdate="To Do List telah diupdate"
        val alert = AlertDialog.Builder(this)

        val editText = EditText(applicationContext)
        editText.setText(toDoList.toDoList)

        alert.setTitle("Edit Your To Do List")
        alert.setView(editText)

        alert.setPositiveButton("Update") { dialog, _ ->
            toDoList.toDoList = editText.text.toString()
            viewModel.updateList(toDoList)
            val toast = Toast.makeText(this, messageUpdate, Toast.LENGTH_SHORT)
            toast.setGravity(Gravity.TOP, 0,-20)
            toast.show()
            dialog.dismiss()
        }

        alert.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        alert.show()
    }
}
