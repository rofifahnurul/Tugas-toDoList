package id.ac.unhas.todolist.main

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import id.ac.unhas.todolist.R
import id.ac.unhas.todolist.database.ToDoList
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.text.format.DateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: ViewModel
    private lateinit var adapter: Adapter
    private lateinit var filterBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Menghubungkan tampilan
        listRecycler.layoutManager = LinearLayoutManager(this)
        adapter = Adapter(this) { toDoList, i ->
            showAlertMenu(toDoList)
        }
        listRecycler.adapter = adapter

        viewModel = ViewModelProvider(this).get(ViewModel::class.java)
        viewModel.getList()?.observe(this, Observer {
            adapter.setList(it)
        })

        //button untuk sort data
        filterBtn = findViewById(R.id.filterBtn)
        filterBtn.setOnClickListener {
            val items = arrayOf("Sort By Date Created", "Sort By Deadline")
            val builder = AlertDialog.Builder(this)
            val alert = AlertDialog.Builder(this)

            //Menampilkan pilihan akan sort by apa
            builder.setItems(items) { dialog, which ->
                when (which) {
                    //Sort By Date created
                    0 -> {
                        alert.setTitle(items[which])
                                //akan mengurutkan dari tanggal pembuatan yang terbaru
                            .setPositiveButton("Dari terbaru") { dialog, which ->
                                viewModel.dateDesc()?.observe(this, Observer {
                                    adapter.setList(it)
                                })
                            }
                                //akan mengurutkan dari tanggal pembuatan yang terlama
                            .setNegativeButton("Dari terlama") { dialog, which ->
                                viewModel.dateAsc()?.observe(this, Observer {
                                    adapter.setList(it)
                                })
                            }
                        alert.show()

                    }
                    //Jika memilih sort by deadline
                    1 -> {
                        alert.setTitle(items[which])
                                //mengurutkan dari deadline terbaru
                            .setPositiveButton("Dari terbaru") { dialog, which ->
                                viewModel.deadlineDesc()?.observe(this, Observer {
                                    adapter.setList(it)
                                })
                            }
                            //mengurutkan dari deadline terlama
                            .setNegativeButton("Dari terlama") { dialog, which ->
                                viewModel.deadlineAsc()?.observe(this, Observer {
                                    adapter.setList(it)
                                })
                            }
                        alert.show()
                    } }
            }
            builder.show()
        } }

    override fun onCreateOptionsMenu (menu: Menu?): Boolean {
        //menghubungkan dengan menu
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_main, menu)
        if (menu != null) {
            search(menu)
        }
        return super.onCreateOptionsMenu(menu) }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
                //apabila item add menu dipilih
                R.id.addMenu ->
                    showAlertDialogAdd()
            }
        return super.onOptionsItemSelected(item)
    }

    //untuk melakukan search
    private fun search(menu:Menu){
        val item = menu?.findItem(R.id.search)

        val searchView = item?.actionView as androidx.appcompat.widget.SearchView?
        searchView?.isSubmitButtonEnabled = true


        searchView?.setOnQueryTextListener(
            //apabila text query dipanggil
            object: androidx.appcompat.widget.SearchView.OnQueryTextListener{
                //apabila kita mensearch
                override fun onQueryTextSubmit(query: String?): Boolean {
                    if(query != null){
                        getItems(query)
                    }
                    return true }

                //apabila kita mengubah kata yang ditulis pada search
                override fun onQueryTextChange(newText: String?): Boolean {
                    if(newText != null){
                        getItems(newText)
                    }
                    return true }
            })
    }
    //mengambil nilai untuk search
    private fun getItems(searchText: String){
        var searchText = searchText
        searchText = "%$searchText%"
        viewModel.search(searchText)?.observe(this, Observer {
            adapter.setList(it)
        })
    }

    //menampilkan dialog alert add agar dapat ditambahkan
    private fun showAlertDialogAdd() {
        //menghubungkan tampilannya
        val inflater = LayoutInflater.from(this)
        val view = inflater.inflate(R.layout.add_layout,null)
        val alert = AlertDialog.Builder(this).setView(view).show()

        val note = view.findViewById<TextView>(R.id.addNote)
        val title = view.findViewById<TextView>(R.id.addTittle)
        val dateCreate = view.findViewById<TextView>(R.id.addDate)
        val deadline = view.findViewById<TextView>(R.id.addDateDeadline)
        val timeDeadline = view.findViewById<TextView>(R.id.addTimeDeadline)
        val saveBtn = view.findViewById<Button>(R.id.saveBtn)
        val cancelBtn = view.findViewById<Button>(R.id.cancelBtn)
        val deadlineBtn = view.findViewById<Button>(R.id.dateDeadline)
        val timeDeadlineBtn = view.findViewById<Button>(R.id.timeDeadline)


        //variabel untuk mengambil tanggal dan waktu
        val calendar = Calendar.getInstance()
        val simpleFormat = SimpleDateFormat("dd-MM-yyyy hh:mm a")
        val date = simpleFormat.format(calendar.time)
        dateCreate.setText("Created at : $date")


        //Date Picker akan dijalankan jika tombol ditekan
        deadlineBtn.setOnClickListener {
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            var day = calendar.get(Calendar.DAY_OF_MONTH)

            val dpd = DatePickerDialog(this, DatePickerDialog.OnDateSetListener {
                    view, year, month, day ->
                val monthFix = month + 1
                deadline.setText("Deadline at : "+day+","+monthFix+" "+year)
            }, year, month, day)
            dpd.show() }

        //Time Picker akan dijalankan jika tombol ditekan
        timeDeadlineBtn.setOnClickListener {
            val hour = calendar.get(Calendar.HOUR)
            val minute = calendar.get(Calendar.MINUTE)
            val tpd = TimePickerDialog(this, TimePickerDialog.OnTimeSetListener{
                    timepicker,hour, minute ->

                timeDeadline.setText("Time : "+hour+":"+minute+" ")
            }, hour, minute,false)
            DateFormat.is24HourFormat(this)
            tpd.show() }

        //Menyimpan penambahan yang dilakukan
        saveBtn.setOnClickListener {
            //Menambahkan data ke database sesuai nama variabel yang dibuat di entity
            viewModel.insertList(ToDoList(
                toDoList =title.text.toString(),
                note = note.text.toString(),
                date=dateCreate.text.toString(),
                deadline = deadline.text.toString(),
                timeDeadline = timeDeadline.text.toString()))
            alert.dismiss() }

        //Jika tombol cancel ditekan
        cancelBtn.setOnClickListener { alert.dismiss() }

        alert.create()
    }

    //Menampilkan menu pilihan untuk mengedit
    private fun showAlertMenu(toDoList: ToDoList) {

        val messageDelete="To Do List telah dihapus"
        val items = arrayOf("Edit", "Delete")
        val builder = AlertDialog.Builder(this)
        val alertCheck = AlertDialog.Builder(this)

        //Jika item di alert dialog ditekan
        builder.setItems(items) { dialog, which ->
            // the user clicked on colors[which]
            when (which) {
                //Jika menekan edit
                0 -> {
                    showAlertDialogEdit(toDoList) }
                //Jika menekan delete
                1 -> {
                    viewModel.deleteList(toDoList)
                    //pesan yang muncul
                    val toast = Toast.makeText(this, messageDelete, Toast.LENGTH_SHORT)
                    toast.setGravity(Gravity.TOP, 0,-20)
                    toast.show()
                }
            }
        }
        builder.show() }

    //Fungsi untuk menampilkan alert dialog edit
    private fun showAlertDialogEdit(toDoList: ToDoList) {
        //Menghubungkan tampilan dengan layout
        val inflater = LayoutInflater.from(this)
        val view = inflater.inflate(R.layout.edit_layout, null)
        val alert = AlertDialog.Builder(this).setView(view).show()

        val editTitle = view.findViewById<TextView>(R.id.editTittle)
        val editNote = view.findViewById<TextView>(R.id.editNote)
        val editDate = view.findViewById<TextView>(R.id.editDate)
        val editDateDeadline = view.findViewById<TextView>(R.id.editDateDeadline)
        val editTimeDeadline = view.findViewById<TextView>(R.id.editTimeDeadline)
        val editDDBtn = view.findViewById<Button>(R.id.dateDeadlineBtn)
        val editTDBtn = view.findViewById<Button>(R.id.timeDeadlineBtn)
        val editBtn = view.findViewById<Button>(R.id.editBtn)
        val cancelBtn = view.findViewById<Button>(R.id.cancelBtn)

        //Untuk dapat mengambil tanggal dan waktu
        val calendar = Calendar.getInstance()
        val simpleFormat = SimpleDateFormat("dd-MM-yyyy hh:mm a")
        val date = simpleFormat.format(calendar.time)

        editDate.setText("Updated at : $date")
        editTitle.setText(toDoList.toDoList)
        editNote.setText(toDoList.note)
        editDateDeadline.setText(toDoList.deadline)
        editTimeDeadline.setText(toDoList.timeDeadline)

        //Date Picker
        editDDBtn.setOnClickListener {
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            var day = calendar.get(Calendar.DAY_OF_MONTH)

            val dpd = DatePickerDialog(
                this,
                DatePickerDialog.OnDateSetListener { view, year, month, day ->
                    val monthFix = month + 1
                    editDateDeadline.setText("Deadline at : " + day + "," + monthFix + " " + year)
                },
                year,
                month,
                day
            )
            dpd.show()
        }

        //Time Picker
        editTDBtn.setOnClickListener {
            val hour = calendar.get(Calendar.HOUR)
            val minute = calendar.get(Calendar.MINUTE)
            val tpd = TimePickerDialog(
                this,
                TimePickerDialog.OnTimeSetListener { timepicker, hour, minute ->
                    DateFormat.is24HourFormat(this)
                    editTimeDeadline.setText("" + hour + ":" + minute + " ")
                },
                hour,
                minute,
                false
            )

            tpd.show()
        }

        val messageUpdate = "To Do List telah diupdate"
        //Jika tombol edit diklik
        editBtn.setOnClickListener {
            toDoList.note = editNote.text.toString()
            toDoList.toDoList = editTitle.text.toString()
            toDoList.date = editDate.text.toString()

            toDoList.deadline = editDateDeadline.text.toString()
            toDoList.timeDeadline = editTimeDeadline.text.toString()

            viewModel.updateList(toDoList)
            val toast = Toast.makeText(this, messageUpdate, Toast.LENGTH_SHORT)
            toast.setGravity(Gravity.TOP, 0, -20)
            toast.show()
            alert.dismiss() }

        //Jika tombol cancel ditekan
        cancelBtn.setOnClickListener {
            alert.dismiss() }
        alert.create()
    }
}

