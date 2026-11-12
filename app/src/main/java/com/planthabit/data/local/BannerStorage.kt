package com.planthabit.data.local

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max

/**
 * Stores user-picked challenge banner photos in app-private storage.
 *
 * The picked image is copied (not referenced by URI) so it survives the user deleting or
 * moving the original, and is downscaled so large camera photos don't bloat storage or memory.
 * The stored absolute path is kept in [com.planthabit.data.model.ChallengeEntity.bannerResName];
 * bundled banners keep using a drawable name there instead.
 */
object BannerStorage {
    private const val DIR = "challenge_banners"
    private const val MAX_EDGE = 1600

    /** True when [bannerRef] points to a user photo rather than a bundled drawable name. */
    fun isCustom(bannerRef: String?): Boolean = bannerRef != null && bannerRef.startsWith("/")

    /** Copies [source] into private storage for [challengeId] and returns the new file path. */
    fun save(context: Context, challengeId: String, source: Uri): String {
        val bitmap = decodeScaled(context, source)
            ?: throw IllegalArgumentException("Couldn't read that image")
        val dir = File(context.filesDir, DIR).apply { mkdirs() }
        val safeId = challengeId.replace(Regex("[^A-Za-z0-9_-]"), "_")
        // New name every time so image caches never show a stale banner.
        val out = File(dir, "${safeId}_${System.currentTimeMillis()}.jpg")
        FileOutputStream(out).use { bitmap.compress(Bitmap.CompressFormat.JPEG, 85, it) }
        bitmap.recycle()
        return out.absolutePath
    }

    /** Deletes a previously saved banner file (no-op for bundled drawable names). */
    fun delete(context: Context, bannerRef: String?) {
        if (!isCustom(bannerRef)) return
        val file = File(bannerRef!!)
        // Only ever delete inside our own folder.
        if (file.parentFile?.canonicalPath == File(context.filesDir, DIR).canonicalPath) file.delete()
    }

    private fun decodeScaled(context: Context, uri: Uri): Bitmap? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // ImageDecoder applies EXIF rotation automatically.
            val src = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(src) { decoder, info, _ ->
                val longest = max(info.size.width, info.size.height)
                if (longest > MAX_EDGE) {
                    val scale = MAX_EDGE.toFloat() / longest
                    decoder.setTargetSize(
                        (info.size.width * scale).toInt().coerceAtLeast(1),
                        (info.size.height * scale).toInt().coerceAtLeast(1)
                    )
                }
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            }
        } else {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
            var sample = 1
            while (max(bounds.outWidth, bounds.outHeight) / (sample * 2) >= MAX_EDGE) sample *= 2
            val opts = BitmapFactory.Options().apply { inSampleSize = sample }
            context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opts) }
        }
    }
}
