package com.example.mad_practical_7

import android.util.Log
import java.io.BufferedInputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.ProtocolException
import java.net.SocketTimeoutException
import java.net.URL
import kotlin.io.bufferedReader
import kotlin.io.readText
import kotlin.io.use
import kotlin.text.isNullOrEmpty


class HttpRequest {

    companion object {
        private const val TAG = "HttpRequest"
    }

    fun makeServiceCall(reqUrl: String?, token: String? = null): String? {
        var response: String? = null
        var conn: HttpURLConnection? = null

        try {
            Log.d(TAG, "Making request to: $reqUrl")
            val url = URL(reqUrl)
            conn = url.openConnection() as HttpURLConnection

            // Set connection properties
            conn.connectTimeout = 15000 // 15 seconds
            conn.readTimeout = 15000
            conn.requestMethod = "GET"

            // Only add token if it's provided and not empty
            if (!token.isNullOrEmpty()) {
                conn.setRequestProperty("Authorization", "Bearer $token")
                Log.d(TAG, "Authorization token added")
            }

            conn.setRequestProperty("Content-Type", "application/json")
            conn.setRequestProperty("Accept", "application/json")

            // Connect and get response code
            val responseCode = conn.responseCode
            Log.d(TAG, "Response Code: $responseCode")

            if (responseCode == HttpURLConnection.HTTP_OK) {
                response = convertStreamToString(BufferedInputStream(conn.inputStream))
                Log.d(TAG, "Response received successfully, length: ${response?.length}")
            } else {
                // Try to read error stream
                val errorStream = conn.errorStream
                if (errorStream != null) {
                    val errorResponse = convertStreamToString(BufferedInputStream(errorStream))
                    Log.e(TAG, "Error response: $errorResponse")
                } else {
                    Log.e(TAG, "HTTP Error: $responseCode - ${conn.responseMessage}")
                }
            }

        } catch (e: MalformedURLException) {
            Log.e(TAG, "MalformedURLException: ${e.message}", e)
        } catch (e: ProtocolException) {
            Log.e(TAG, "ProtocolException: ${e.message}", e)
        } catch (e: SocketTimeoutException) {
            Log.e(TAG, "SocketTimeoutException: ${e.message}", e)
        } catch (e: IOException) {
            Log.e(TAG, "IOException: ${e.message}", e)
        } catch (e: Exception) {
            Log.e(TAG, "Exception: ${e.message}", e)
        } finally {
            conn?.disconnect()
        }

        return response
    }

    private fun convertStreamToString(inputStream: BufferedInputStream): String {
        return try {
            val bufferedReader = inputStream.bufferedReader()
            bufferedReader.use { it.readText() }
        } catch (e: Exception) {
            Log.e(TAG, "Error converting stream to string: ${e.message}", e)
            ""
        }
    }
}