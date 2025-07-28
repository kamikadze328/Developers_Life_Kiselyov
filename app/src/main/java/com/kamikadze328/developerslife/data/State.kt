package com.kamikadze328.developerslife.data

import kotlinx.serialization.Serializable

@Serializable
enum class State {
    INIT,
    LOADING,
    PROBLEM_INTERNET,
    PROBLEM_NO_MEM,
    PROBLEM_IMAGE_DOWNLOAD,
    PROBLEM_SERVER_ERROR,
    OK
}