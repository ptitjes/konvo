package io.github.ptitjes.konvo.frontend.compose.toolkit.images

import androidx.compose.runtime.*
import coil3.*
import coil3.compose.*
import coil3.disk.*
import coil3.memory.*
import coil3.request.*
import io.github.ptitjes.konvo.core.platform.*
import kotlinx.io.files.*
import okio.Path.Companion.toPath
import org.kodein.di.compose.*

@Composable
fun CoilImageLoader() {
    val storagePaths by rememberInstance<StoragePaths>()
    setSingletonImageLoaderFactory { context ->
        val cachePath = Path(storagePaths.cacheDirectory, "images")

        ImageLoader.Builder(context)
            .diskCache {
                DiskCache.Builder()
                    .maxSizePercent(0.02)
                    .directory(cachePath.toString().toPath())
                    .build()
            }
            .memoryCache {
                MemoryCache.Builder()
                    .maxSizePercent(context, 0.25)
                    .build()
            }
            .crossfade(true)
            .build()
    }
}
