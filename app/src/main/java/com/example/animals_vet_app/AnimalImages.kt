package com.example.animals_vet_app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.LruCache
import android.widget.ImageView
import androidx.annotation.DrawableRes
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.URL
import java.util.concurrent.Executors
import javax.net.ssl.HttpsURLConnection

object AnimalImages {
    private const val MAX_DOWNLOAD_BYTES = 10 * 1024 * 1024
    private const val MAX_BITMAP_DIMENSION = 1024

    private val imageExecutor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())
    private val bitmapCache = object : LruCache<String, Bitmap>(4 * 1024) {
        override fun sizeOf(key: String, bitmap: Bitmap): Int = bitmap.byteCount / 1024
    }

    fun names(context: Context): List<String> =
        context.resources.getStringArray(R.array.animal_image_names).toList()

    @DrawableRes
    fun resourceFor(context: Context, name: String): Int {
        if (name.isEmpty()) return R.drawable.ic_animal_placeholder
        val id = context.resources.getIdentifier(name, "drawable", context.packageName)
        return if (id != 0) id else R.drawable.ic_animal_placeholder
    }

    fun show(
        imageView: ImageView,
        uri: String?,
        fallbackName: String,
        onLoadError: (() -> Unit)? = null
    ) {
        val parsedUri = uri?.takeIf { it.isNotBlank() }?.let(Uri::parse)
        when (parsedUri?.scheme?.lowercase()) {
            "content", "file" -> {
                imageView.tag = null
                imageView.setImageURI(parsedUri)
            }
            "https" -> showRemote(imageView, parsedUri, fallbackName, onLoadError)
            else -> {
                imageView.tag = null
                imageView.setImageResource(resourceFor(imageView.context, fallbackName))
            }
        }
    }

    private fun showRemote(
        imageView: ImageView,
        uri: Uri,
        fallbackName: String,
        onLoadError: (() -> Unit)?
    ) {
        val url = uri.toString()
        imageView.tag = url
        val fallback = resourceFor(imageView.context, fallbackName)
        val cachedBitmap = bitmapCache.get(url)
        if (cachedBitmap != null) {
            imageView.setImageBitmap(cachedBitmap)
            return
        }

        imageView.setImageResource(fallback)
        imageExecutor.execute {
            val bitmap = try {
                downloadBitmap(URL(url))
            } catch (_: IOException) {
                null
            } catch (_: IllegalArgumentException) {
                null
            }

            if (bitmap != null) bitmapCache.put(url, bitmap)
            mainHandler.post {
                if (imageView.tag == url) {
                    if (bitmap != null) {
                        imageView.setImageBitmap(bitmap)
                    } else {
                        imageView.setImageResource(fallback)
                        onLoadError?.invoke()
                    }
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun downloadBitmap(url: URL): Bitmap {
        val connection = url.openConnection() as HttpsURLConnection
        connection.connectTimeout = 10_000
        connection.readTimeout = 10_000
        connection.instanceFollowRedirects = true
        try {
            val imageBytes = connection.inputStream.use { input ->
                val output = ByteArrayOutputStream()
                val buffer = ByteArray(8 * 1024)
                var totalBytes = 0
                while (true) {
                    val bytesRead = input.read(buffer)
                    if (bytesRead == -1) break
                    totalBytes += bytesRead
                    if (totalBytes > MAX_DOWNLOAD_BYTES) {
                        throw IOException("Image exceeds the download size limit.")
                    }
                    output.write(buffer, 0, bytesRead)
                }
                output.toByteArray()
            }

            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size, bounds)
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
                throw IOException("Response is not a supported image.")
            }

            val sampleSize = calculateSampleSize(bounds.outWidth, bounds.outHeight)
            return BitmapFactory.decodeByteArray(
                imageBytes,
                0,
                imageBytes.size,
                BitmapFactory.Options().apply { inSampleSize = sampleSize }
            ) ?: throw IOException("Image could not be decoded.")
        } finally {
            connection.disconnect()
        }
    }

    private fun calculateSampleSize(width: Int, height: Int): Int {
        var sampleSize = 1
        while (width / sampleSize > MAX_BITMAP_DIMENSION ||
            height / sampleSize > MAX_BITMAP_DIMENSION
        ) {
            sampleSize *= 2
        }
        return sampleSize
    }
}
