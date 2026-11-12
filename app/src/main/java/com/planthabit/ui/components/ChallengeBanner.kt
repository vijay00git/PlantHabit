package com.planthabit.ui.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.planthabit.data.local.BannerStorage
import com.planthabit.data.model.ChallengeEntity
import java.io.File

/** Where a challenge's banner comes from: a user photo or a bundled drawable. */
sealed interface BannerSource {
    data class Photo(val file: File) : BannerSource
    data class Bundled(val resId: Int) : BannerSource
}

/** Looks up a bundled drawable by name (e.g. "banner_read_daily"); 0/unknown → null. */
@android.annotation.SuppressLint("DiscouragedApi")
private fun bundledBanner(context: android.content.Context, name: String?): Int? {
    if (name.isNullOrBlank()) return null
    return context.resources.getIdentifier(name, "drawable", context.packageName).takeIf { it != 0 }
}

/** The challenge's banner, or null if it has none (callers then draw a gradient). */
@Composable
fun rememberBannerSource(challenge: ChallengeEntity): BannerSource? {
    val ref = challenge.bannerResName
    val context = LocalContext.current
    return remember(ref) {
        if (BannerStorage.isCustom(ref)) {
            File(ref!!).takeIf { it.exists() }?.let { BannerSource.Photo(it) }
        } else {
            bundledBanner(context, ref)?.let { BannerSource.Bundled(it) }
        }
    }
}

/** Draws a banner source, cropped to fill [modifier]'s bounds. */
@Composable
fun ChallengeBannerImage(
    source: BannerSource,
    contentDescription: String?,
    modifier: Modifier = Modifier,
) {
    when (source) {
        is BannerSource.Photo -> AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(source.file)
                .crossfade(true)
                .build(),
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
        is BannerSource.Bundled -> Image(
            painter = painterResource(source.resId),
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    }
}
