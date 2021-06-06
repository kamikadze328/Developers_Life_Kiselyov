package com.kamikadze328.developerslife.additional

enum class CATEGORY(val category: String) {
    RANDOM("Random"),
    LATEST("Latest"),
    HOT("Hot"),
    TOP("Top");

    companion object {
        private val map = values().associateBy(CATEGORY::category)
        fun fromString(category: String) = map[category]
    }
}