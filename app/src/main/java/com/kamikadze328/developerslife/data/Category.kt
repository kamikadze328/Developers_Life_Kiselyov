package com.kamikadze328.developerslife.data

import kotlinx.serialization.Serializable

@Serializable
enum class Category(val urlParam: String, val id: Int) {
    RANDOM("random", 0),
    LATEST("latest", 1),
    HOT("hot", 2),
    TOP("top", 3);


    companion object {
        private val mapIds = entries.associateBy(Category::id)

        fun byId(id: Int) = mapIds[id] ?: RANDOM
    }
}