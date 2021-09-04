package com.kamikadze328.developerslife.additional

import com.kamikadze328.developerslife.R

enum class Category(val urlParam: String, val resourceId: Int) {
    RANDOM("random", R.string.random),
    LATEST("latest", R.string.latest),
    HOT("hot", R.string.hot),
    TOP("top", R.string.top);

    companion object {
        private val map = values().associateBy(Category::urlParam)
        fun fromString(category: String) = map[category]
    }
}