package com.example.my_imgur

import android.Manifest
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.my_imgur.helper.DocumentHelper
import com.example.my_imgur.models.ImageUploadResponse
import com.example.my_imgur.service.ImgurApiService
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream
import java.util.*

class MainActivity : AppCompatActivity() {
    private var imageUri: Uri? = null

    private val image: ImageView
        get() = findViewById(R.id.imageView)

    private val galleryButton: Button
        get() = findViewById(R.id.gallery_button)

    private val uploadButton: Button
        get() = findViewById(R.id.upload_button)

    private val title: TextView
        get() = findViewById(R.id.titleText)

    private val description: TextView
        get() = findViewById(R.id.descriptionText)

    private val linkText: TextView
        get() = findViewById(R.id.linkText)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        uploadButton.isEnabled = false

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            addPermissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }

        galleryButton.setOnClickListener {
            val photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type = "image/*"

            startActivityForResult(photoPickerIntent, Constants.IMAGE_REQUEST_CODE)
        }

        uploadButton.setOnClickListener {
            uploadFile()
        }
    }

    private fun uploadFile() {
        val imageFile = File(DocumentHelper.getPath(this, imageUri)!!)

        val imgurService = ImgurApiService.getRetrofit().create(ImgurApiService::class.java)

        imgurService.postImage(
            title.text.toString(),
            description.text.toString(),
            MultipartBody.Part.createFormData(
                "image",
                imageFile.name,
                RequestBody.create(MediaType.parse("image/*"), imageFile)
            )
        ).enqueue(object : Callback<ImageUploadResponse> {
            override fun onResponse(
                call: Call<ImageUploadResponse>,
                response: Response<ImageUploadResponse>
            ) {
                if (response.isSuccessful) {
                    Toast.makeText(
                        this@MainActivity,
                        "Успешно загруженно",
                        Toast.LENGTH_SHORT
                    ).show()

                    val imageLink = "http://imgur.com/${response.body()!!.data.id}"

                    uploadButton.isEnabled = false
                    linkText.text = imageLink
                    title.text = ""
                    description.text = ""
                }
            }

            override fun onFailure(call: Call<ImageUploadResponse>, t: Throwable) {
                Toast.makeText(
                    this@MainActivity,
                    "Ошибка загрузки",
                    Toast.LENGTH_SHORT
                ).show()

                t.printStackTrace()
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == Constants.IMAGE_REQUEST_CODE && resultCode == RESULT_OK) {
            try {
                imageUri = data?.data

                if (imageUri != null) {
                    val imageStream: InputStream? =
                        imageUri?.let { contentResolver.openInputStream(it) }
                    val selectedImage = BitmapFactory.decodeStream(imageStream)

                    image.setImageBitmap(selectedImage)

                    uploadButton.isEnabled = true
                    linkText.text = ""
                }
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
        }
    }

    private fun addPermissions(vararg permission: String) {
        val permissionsList: MutableList<String> = ArrayList()

        permissionsList.addAll(permission)

        if (permissionsList.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this@MainActivity,
                permissionsList.toTypedArray(),
                Constants.PERMISSION_REQUEST_CODE
            )
        }
    }
}