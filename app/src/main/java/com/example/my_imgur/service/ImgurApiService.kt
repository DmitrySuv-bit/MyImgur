package com.example.my_imgur.service

import com.example.my_imgur.Constants
import com.example.my_imgur.models.ImageUploadResponse
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.io.File

interface ImgurApiService {
    @Multipart
    @Headers("Authorization: ${Constants.CLIENT_ID}")
    @POST("image")
    fun postImage(
        @Query("title") title: String?,
        @Query("description") description: String?,
        @Part file: MultipartBody.Part
    ): Call<ImageUploadResponse>

    companion object {
        fun getRetrofit(): Retrofit {
            return Retrofit.Builder()
                .baseUrl(Constants.IMGUR_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
    }
}