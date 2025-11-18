package com.example.mad_practical_7

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var buttonRefresh: ImageView
    private lateinit var textViewTitle: TextView

    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var personAdapter: PersonAdapter
    private var personList: ArrayList<Person> = ArrayList()

    companion object {
        private const val TAG = "MainActivity"
        private const val JSON_URL = "https://api.myjson.online/v1/records/6dc50a82-6d2d-4ddd-ac22-709d2c0cb7b5"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize views using findViewById
        recyclerView = findViewById(R.id.recyclerView)
        buttonRefresh = findViewById(R.id.buttonRefresh)
        textViewTitle = findViewById(R.id.textViewTitle)

        // Initialize database helper
        databaseHelper = DatabaseHelper(this)

        // Setup RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        personAdapter = PersonAdapter(this, personList)
        recyclerView.adapter = personAdapter

        // Load persons from database
        loadPersonsFromDatabase()

        // Refresh button click listener
        buttonRefresh.setOnClickListener {
            Log.d(TAG, "Refresh button clicked")
            fetchDataFromAPI()
        }
    }

    private fun loadPersonsFromDatabase() {
        personList.clear()
        personList.addAll(databaseHelper.allPersons)
        personAdapter.notifyDataSetChanged()

        Log.d(TAG, "Loaded ${personList.size} persons from database")

        // If database is empty, fetch from API
        if (personList.isEmpty()) {
            Log.d(TAG, "Database is empty, fetching from API")
            fetchDataFromAPI()
        }
    }

    private fun fetchDataFromAPI() {
        Toast.makeText(this, "Fetching data...", Toast.LENGTH_SHORT).show()
        Log.d(TAG, "Starting API fetch from: $JSON_URL")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "Making HTTP request...")
                // No token needed for MyJSON
                val data = HttpRequest().makeServiceCall(JSON_URL)

                Log.d(TAG, "Response received: ${data?.take(100)}...") // Log first 100 chars

                withContext(Dispatchers.Main) {
                    if (data != null && data.isNotEmpty()) {
                        try {
                            getPersonDetailsFromJson(data)
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing JSON: ${e.message}", e)
                            Toast.makeText(
                                this@MainActivity,
                                "Error parsing data: ${e.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } else {
                        Log.e(TAG, "No data received from API")
                        Toast.makeText(
                            this@MainActivity,
                            "Failed to fetch data - empty response",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Network Error: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MainActivity,
                        "Network error: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun getPersonDetailsFromJson(jsonStr: String) {
        try {
            Log.d(TAG, "Parsing JSON data...")
            Log.d(TAG, "Raw JSON: ${jsonStr.take(200)}...")

            // MyJSON wraps data in a "data" object, so we need to extract it
            val jsonArray = try {
                // First, try to parse as a JSON object
                val jsonObject = org.json.JSONObject(jsonStr)
                if (jsonObject.has("data")) {
                    jsonObject.getJSONArray("data")
                } else {
                    // If no "data" key, try parsing as array directly
                    JSONArray(jsonStr)
                }
            } catch (e: Exception) {
                // If parsing as object fails, try as array directly
                JSONArray(jsonStr)
            }

            Log.d(TAG, "JSON array length: ${jsonArray.length()}")

            // Clear existing data in database
            val existingPersons = databaseHelper.allPersons
            Log.d(TAG, "Clearing ${existingPersons.size} existing records")
            for (person in existingPersons) {
                databaseHelper.deletePerson(person)
            }

            // Parse JSON and insert into database
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)

                val id = jsonObject.getString("id")
                val email = jsonObject.getString("email")
                val phone = jsonObject.getString("phone")

                val profileObject = jsonObject.getJSONObject("profile")
                val name = profileObject.getString("name")
                val address = profileObject.getString("address")

                val locationObject = profileObject.getJSONObject("location")
                val lat = locationObject.getDouble("lat")
                val long = locationObject.getDouble("long")

                val person = Person(id, name, email, phone, address, lat, long)

                // Insert into database
                val insertId = databaseHelper.insertPerson(person)
                Log.d(TAG, "Inserted person: $name (ID: $insertId)")
            }

            // Reload data from database
            loadPersonsFromDatabase()

            Toast.makeText(
                this,
                "Data loaded successfully (${jsonArray.length()} records)",
                Toast.LENGTH_SHORT
            ).show()

        } catch (e: org.json.JSONException) {
            Log.e(TAG, "JSON Parsing Error: ${e.message}", e)
            Toast.makeText(this, "Error parsing JSON: ${e.message}", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Log.e(TAG, "Error: ${e.message}", e)
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    fun deletePerson(person: Person) {
        databaseHelper.deletePerson(person)
        loadPersonsFromDatabase()
        Toast.makeText(this, "${person.name} deleted", Toast.LENGTH_SHORT).show()
    }
}