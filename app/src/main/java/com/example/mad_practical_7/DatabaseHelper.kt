package com.example.mad_practical_7

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import kotlin.let

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        // Database Version
        private const val DATABASE_VERSION = 1

        // Database Name
        private const val DATABASE_NAME = "persons_db"
    }

    // Creating Tables
    override fun onCreate(db: SQLiteDatabase) {
        // create persons table
        db.execSQL(PersonDbTableData.CREATE_TABLE)
    }

    // Upgrading database
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + PersonDbTableData.TABLE_NAME)

        // Create tables again
        onCreate(db)
    }

    // Insert person into database
    fun insertPerson(person: Person): Long {
        val db = this.writableDatabase

        val values = ContentValues()
        values.put(PersonDbTableData.COLUMN_ID, person.id)
        values.put(PersonDbTableData.COLUMN_PERSON_NAME, person.name)
        values.put(PersonDbTableData.COLUMN_PERSON_EMAIL_ID, person.emailId)
        values.put(PersonDbTableData.COLUMN_PERSON_PHONE_NO, person.phoneNo)
        values.put(PersonDbTableData.COLUMN_PERSON_ADDRESS, person.address)
        values.put(PersonDbTableData.COLUMN_PERSON_GPS_LAT, person.latitude)
        values.put(PersonDbTableData.COLUMN_PERSON_GPS_LONG, person.longitude)

        // Inserting Row
        val id = db.insert(PersonDbTableData.TABLE_NAME, null, values)
        db.close()

        return id
    }

    // Get values from person object
    private fun getValues(person: Person): ContentValues {
        val values = ContentValues()
        values.put(PersonDbTableData.COLUMN_ID, person.id)
        values.put(PersonDbTableData.COLUMN_PERSON_NAME, person.name)
        values.put(PersonDbTableData.COLUMN_PERSON_EMAIL_ID, person.emailId)
        values.put(PersonDbTableData.COLUMN_PERSON_PHONE_NO, person.phoneNo)
        values.put(PersonDbTableData.COLUMN_PERSON_ADDRESS, person.address)
        values.put(PersonDbTableData.COLUMN_PERSON_GPS_LAT, person.latitude)
        values.put(PersonDbTableData.COLUMN_PERSON_GPS_LONG, person.longitude)
        return values
    }

    // Get single person by id
    fun getPerson(id: String): Person? {
        val db = this.readableDatabase

        val cursor = db.query(
            PersonDbTableData.TABLE_NAME,
            arrayOf(
                PersonDbTableData.COLUMN_ID,
                PersonDbTableData.COLUMN_PERSON_NAME,
                PersonDbTableData.COLUMN_PERSON_EMAIL_ID,
                PersonDbTableData.COLUMN_PERSON_PHONE_NO,
                PersonDbTableData.COLUMN_PERSON_ADDRESS,
                PersonDbTableData.COLUMN_PERSON_GPS_LAT,
                PersonDbTableData.COLUMN_PERSON_GPS_LONG
            ),
            PersonDbTableData.COLUMN_ID + "=?",
            arrayOf(id),
            null,
            null,
            null,
            null
        )

        cursor?.moveToFirst()

        val person = cursor?.let { getPerson(it) }
        cursor?.close()

        return person
    }

    // Get person from cursor
    private fun getPerson(cursor: Cursor): Person {
        return Person(
            cursor.getString(cursor.getColumnIndexOrThrow(PersonDbTableData.COLUMN_ID)),
            cursor.getString(cursor.getColumnIndexOrThrow(PersonDbTableData.COLUMN_PERSON_NAME)),
            cursor.getString(cursor.getColumnIndexOrThrow(PersonDbTableData.COLUMN_PERSON_EMAIL_ID)),
            cursor.getString(cursor.getColumnIndexOrThrow(PersonDbTableData.COLUMN_PERSON_PHONE_NO)),
            cursor.getString(cursor.getColumnIndexOrThrow(PersonDbTableData.COLUMN_PERSON_ADDRESS)),
            cursor.getDouble(cursor.getColumnIndexOrThrow(PersonDbTableData.COLUMN_PERSON_GPS_LAT)),
            cursor.getDouble(cursor.getColumnIndexOrThrow(PersonDbTableData.COLUMN_PERSON_GPS_LONG))
        )
    }

    // Select All Query
    val allPersons: ArrayList<Person>
        get() {
            val personList = kotlin.collections.ArrayList<Person>()

            val selectQuery = "SELECT * FROM " + PersonDbTableData.TABLE_NAME

            val db = this.writableDatabase
            val cursor = db.rawQuery(selectQuery, null)

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    val person = getPerson(cursor)
                    personList.add(person)
                } while (cursor.moveToNext())
            }

            cursor.close()
            return personList
        }

    // Get persons count
    val personsCount: Int
        get() {
            val countQuery = "SELECT * FROM " + PersonDbTableData.TABLE_NAME
            val db = this.readableDatabase
            val cursor = db.rawQuery(countQuery, null)

            val count = cursor.count
            cursor.close()

            return count
        }

    // Update person
    fun updatePerson(person: Person): Int {
        val db = this.writableDatabase

        val values = getValues(person)

        // updating row
        return db.update(
            PersonDbTableData.TABLE_NAME,
            values,
            PersonDbTableData.COLUMN_ID + " = ?",
            arrayOf(person.id)
        )
    }

    // Delete person
    fun deletePerson(person: Person) {
        val db = this.writableDatabase
        db.delete(
            PersonDbTableData.TABLE_NAME,
            PersonDbTableData.COLUMN_ID + " = ?",
            arrayOf(person.id)
        )
        db.close()
    }
}