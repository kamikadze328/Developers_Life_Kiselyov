package com.kamikadze328.developerslife.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject


@Parcelize
data class ImageMeta(
    val description: String,
    val gifURL: String,
    val previewURL: String,
    val id: Int,
    val votes: Int
) :
    Parcelable {
    companion object {
        fun jsonObjectToImageMeta(json: JSONObject): ImageMeta {
            val description = getString("description", json)
            val gifURL = getString("gifURL", json)
            val previewURL = getString("previewURL", json)
            val id = getInt("id", json)
            val votes = getInt("votes", json)
            /*
            val author = getString("author", json)
            val commentsCount = getString("commentsCount", json)
            val canVote = getBoolean("canVote", json)
            val date = getString("date", json)
            val type = getString("type", json)
            val height = getInt("height", json)
            val width = getString("width", json)
            val fileSize = getInt("fileSize", json)
            val gifSize = getInt("gifSize", json)
            val videoPath = getString("videoPath", json)
            val videoSize = getInt("videoSize", json)
            val videoURL = getString("videoURL", json)
            */
            return ImageMeta(description, gifURL, previewURL, id, votes)
        }

        private fun getString(name: String, json: JSONObject): String {
            return try {
                json.getString(name)
            } catch (e: JSONException) {
                ""
            }
        }

        private fun getInt(name: String, json: JSONObject): Int {
            return try {
                json.getInt(name)
            } catch (e: JSONException) {
                0
            }
        }
    }
}
