package com.samrudh.bookhub.activity

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.*
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.room.Room
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.samrudh.bookhub.R
import com.samrudh.bookhub.database.BookDatabase
import com.samrudh.bookhub.database.BookEntity
import com.samrudh.bookhub.util.ConnectionManager
import com.squareup.picasso.Picasso
import org.json.JSONObject
import java.lang.Exception

class DescriptionActivity : AppCompatActivity() {

    lateinit var txtBookName: TextView
    lateinit var txtBookAuthor: TextView
    lateinit var txtBookPrice: TextView
    lateinit var txtBookRating: TextView
    lateinit var imgBookImage: ImageView
    lateinit var txtBookDescription: TextView
    lateinit var btnAddToFavourites: Button
    lateinit var progressBar: ProgressBar
    lateinit var progressLayout: RelativeLayout

    lateinit var toolbar: Toolbar

    var bookId: String? = "100"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_description)

        txtBookName = findViewById(R.id.txtBookName)
        txtBookAuthor = findViewById(R.id.txtBookAuthor)
        txtBookPrice = findViewById(R.id.txtBookPrice)
        txtBookRating = findViewById(R.id.txtBookRating)
        txtBookDescription = findViewById(R.id.txtBookDescription)
        imgBookImage = findViewById(R.id.imgBookImage)
        btnAddToFavourites = findViewById(R.id.btnAddToFavourites)
        progressBar = findViewById(R.id.progressBar)
        progressBar.visibility = View.VISIBLE
        progressLayout = findViewById(R.id.progressLayout)
        progressLayout.visibility = View.VISIBLE

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Book Details"



        if (intent != null) {
            bookId = intent.getStringExtra("book_id")
        } else {
            finish()
            Toast.makeText(
                this@DescriptionActivity,
                "Some Unexpected Error Occured",
                Toast.LENGTH_SHORT
            ).show()
        }
        if (bookId == "100") {
            finish()
            Toast.makeText(
                this@DescriptionActivity,
                "Some Unexpected Error Occured",
                Toast.LENGTH_SHORT
            ).show()
        }

        val queue = Volley.newRequestQueue(this@DescriptionActivity)
        val url = "http://13.235.250.119/v1/book/get_book/"

        val jsonParams = JSONObject()
        jsonParams.put("book_id", bookId)

        if (ConnectionManager().checkConnectivity(this@DescriptionActivity)) {

            val jsonRequest =
                object : JsonObjectRequest(Request.Method.POST, url, jsonParams, Response.Listener {

                    try {
                        val success = it.getBoolean("success")
                        if (success) {
                            val bookJsonObject = it.getJSONObject("book_data")
                            progressLayout.visibility = View.GONE


                            val bookImgUrl = bookJsonObject.getString("image")
                            Picasso.get().load(bookJsonObject.getString("image"))
                                .error(R.drawable.default_book_cover).into(imgBookImage)
                            txtBookName.text = bookJsonObject.getString("name")
                            txtBookAuthor.text = bookJsonObject.getString("author")
                            txtBookPrice.text = bookJsonObject.getString("price")
                            txtBookRating.text = bookJsonObject.getString("rating")
                            txtBookDescription.text = bookJsonObject.getString("description")

                            val bookEntity = BookEntity(
                                bookId?.toInt() as Int,
                                txtBookName.text.toString(),
                                txtBookAuthor.text.toString(),
                                txtBookPrice.text.toString(),
                                txtBookRating.text.toString(),
                                txtBookDescription.text.toString(),
                                bookImgUrl
                            )
                            val checkFav = DBAsyncTask(applicationContext, bookEntity, 1).execute()
                            val isFav = checkFav.get()

                            if (isFav) {
                                btnAddToFavourites.text = "Remove from Favourites"
                                val favColor = ContextCompat.getColor(applicationContext, R.color.colorFavourite)
                                btnAddToFavourites.setBackgroundColor(favColor)
                            } else {
                                btnAddToFavourites.text = "Add to Favourites"
                                val noFavColor = ContextCompat.getColor(applicationContext, R.color.colorPrimary)
                                btnAddToFavourites.setBackgroundColor(noFavColor)
                            }

                            btnAddToFavourites.setOnClickListener {
                                if (!DBAsyncTask(applicationContext, bookEntity, 1).execute().get()
                                ) {
                                    val async = DBAsyncTask(applicationContext ,bookEntity, 2).execute()
                                    val result = async.get()

                                    if (result) {
                                        Toast.makeText(this@DescriptionActivity, "Book added to Favourites", Toast.LENGTH_SHORT).show()
                                        btnAddToFavourites.text = "Remove from Favourites"
                                        val favColor = ContextCompat.getColor(applicationContext, R.color.colorFavourite)
                                        btnAddToFavourites.setBackgroundColor(favColor)
                                    } else {
                                        Toast.makeText(this@DescriptionActivity, "Some error occurred!!!", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    val async = DBAsyncTask(applicationContext,bookEntity,3).execute()
                                    val result = async.get()

                                    if (result) {
                                        Toast.makeText(this@DescriptionActivity, "Book removed from Favourites", Toast.LENGTH_SHORT).show()
                                        btnAddToFavourites.text = "Add to Favourites"
                                        val noFavColor = ContextCompat.getColor(applicationContext, R.color.colorPrimary)
                                        btnAddToFavourites.setBackgroundColor(noFavColor)
                                    } else {
                                        Toast.makeText(this@DescriptionActivity, "Some error occurred!!!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }


                        } else {
                            Toast.makeText(this@DescriptionActivity, "Some unexpected error occurred!!!", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this@DescriptionActivity, "Some unexpected error occurred!!!", Toast.LENGTH_SHORT).show()
                    }

                }, Response.ErrorListener {

                    Toast.makeText(
                        this@DescriptionActivity,
                        "Volley Error $it !!!",
                        Toast.LENGTH_SHORT
                    )
                        .show()

                }) {
                    override fun getHeaders(): MutableMap<String, String> {
                        val headers = HashMap<String, String>()
                        headers["Content-type"] = "application/json"
                        headers["token"] = "62a211b615f650"
                        return headers
                    }
                }
            queue.add(jsonRequest)
        } else {
            // Internet is Not Available
            val dialog = AlertDialog.Builder(this@DescriptionActivity)
            dialog.setTitle("Error")
            dialog.setMessage("Internet Connection NOT Found")
            dialog.setPositiveButton("Open Settings") { text, listener ->
                val settingsIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                startActivity(settingsIntent)
                finish()
            }
            dialog.setNegativeButton("Exit") { text, listener ->
                ActivityCompat.finishAffinity(this@DescriptionActivity)
            }
            dialog.create()
            dialog.show()
        }


    }

    class DBAsyncTask(val context: Context, val bookEntity: BookEntity, val mode: Int) :
        AsyncTask<Void, Void, Boolean>() {

        /*
        * Mode 1 -> Check DB if the book is favourite or not
        * Mode 2 -> Save the book into DB as favourite
        * Mode 3 -> Remove the favourite book
        */

        val db = Room.databaseBuilder(context, BookDatabase::class.java, "books-db").build()

        override fun doInBackground(vararg params: Void?): Boolean {

            when (mode) {
                1 -> {
                    //Check DB if the book is favourite or not
                    val book: BookEntity? = db.bookDao().getBookById(bookEntity.book_id.toString())
                    db.close()
                    return book != null
                }
                2 -> {
                    //Save the book into DB as favourite
                    db.bookDao().insertBook(bookEntity)
                    db.close()
                    return true
                }
                3 -> {
                    //Remove the favourite book
                    db.bookDao().deleteBook(bookEntity)
                    db.close()
                    return true
                }
            }

            return false
        }

    }

}
