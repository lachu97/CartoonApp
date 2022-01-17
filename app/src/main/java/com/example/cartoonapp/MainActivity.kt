package com.example.cartoonapp

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Camera
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.core.content.ContextCompat
import com.example.cartoonapp.ml.LiteModelCartoonganInt81
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.tensorflow.lite.support.image.TensorImage
import java.io.File
import java.util.concurrent.ExecutorService

class MainActivity : AppCompatActivity() {
    val REQUEST_IMAGE_CAPTURE = 1
    private var preview: Preview? = null
    private var imageCapture: ImageCapture? = null
    private var camera: Camera? = null
    private var bitmap: Bitmap? = null
    private var modelSpinner: Spinner? = null
    private var modelType: Int = 0
    lateinit var img: ImageView
    lateinit var carimg:ImageView
    lateinit var convert: Button
    private var lensFacing: Int = CameraSelector.LENS_FACING_FRONT
    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        fun allPermissionsGranted(context: Context) = REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        check()
        val button: Button = findViewById(R.id.takepic)
        convert = findViewById(R.id.convert)
        carimg = findViewById(R.id.cartoon)
        img = findViewById(R.id.image)
        button.setOnClickListener {
            lensFacing = if (CameraSelector.LENS_FACING_FRONT == lensFacing) {
                CameraSelector.LENS_FACING_BACK
            } else {
                CameraSelector.LENS_FACING_FRONT
            }
            // Re-bind use cases to update selected camera
            dispatchTakePictureIntent()
        }
    }

    fun check() {
        if (allPermissionsGranted(
                this
            )
        ) {
            Log.i("MainActivity", "Permission for camera Granted")
            //TODO("Perform Camera action")
        } else {
            Log.i("MainActivity", "No permission for camera")
            requestPermissions(
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted(
                    this
                )
            ) {
                Log.i("Maina", "Permission for camera Granted by user")
                //TODO("Perform camera Action")
            } else {
                Toast.makeText(
                    this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        } catch (e: ActivityNotFoundException) {
            Log.i("MainActivity", "jhdsjlghslkjh" + e.printStackTrace())
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == REQUEST_IMAGE_CAPTURE) {
            val imgbitmap = data?.extras?.get("data") as Bitmap

            img.setImageBitmap(imgbitmap)
            Log.i("MainActivity","Success")
            convert.setOnClickListener {
                Toast.makeText(this,"Hello This is on activity ",Toast.LENGTH_LONG).show()
                CoroutineScope(Dispatchers.Main

                ).launch {
                    val model = LiteModelCartoonganInt81.newInstance(this@MainActivity)

// Creates inputs for reference.
                    val sourceImage = TensorImage.fromBitmap(imgbitmap)

// Runs model inference and gets result.
                    val outputs = model.process(sourceImage)
                    val cartoonizedImage = outputs.cartoonizedImageAsTensorImage
                    val cartoonizedImageBitmap = cartoonizedImage.bitmap
                    carimg.setImageBitmap(cartoonizedImageBitmap)
// Releases model resources if no longer used.
                    model.close()
                }

            }
        }
    }
}