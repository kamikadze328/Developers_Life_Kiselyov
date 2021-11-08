package com.kamikadze328.developerslife.data

import com.kamikadze328.developerslife.R

enum class Category(val urlParam: String, val resourceId: Int, val id: Int) {
    RANDOM("random", R.string.random, 0),
    LATEST("latest", R.string.latest, 1),
    HOT("hot", R.string.hot, 2),
    TOP("top", R.string.top, 3);


    companion object {
        private val mapIds = values().associateBy(Category::id)

        fun byId(id: Int) = mapIds[id] ?: RANDOM
    }
}