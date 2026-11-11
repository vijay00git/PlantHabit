package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.local.BannerStorage
import com.example.data.model.ChallengeEntity
import java.io.File

/** Where a challenge's banner comes from: a user photo or a bundled drawable. */
sealed interface BannerSource {
    data class Photo(val file: File) : BannerSource
    data class Bundled(val resId: Int) : BannerSource
}

private fun bundledBanner(name: String?): Int? = when (name) {
    "challenge_gratitude" -> R.drawable.challenge_gratitude
    "routine_morning" -> R.drawable.routine_morning
    else -> null
}

/** The challenge's banner, or null if it has none (callers then draw a gradient). */
@Composable
fun rememberBannerSource(challenge: ChallengeEntity): BannerSource? {
    val ref = challenge.bannerResName
    return remember(ref) {
        if (BannerStorage.isCustom(ref)) {
            File(ref!!).takeIf { it.exists() }?.let { BannerSource.Photo(it) }
        } else {
            bundledBanner(ref)?.let { BannerSource.Bundled(it) }
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
