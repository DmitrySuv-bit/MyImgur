package com.example.my_imgur.models

data class ImageUploadResponse(
    val data: Data,
    val status: Int,
    val success: Boolean
)