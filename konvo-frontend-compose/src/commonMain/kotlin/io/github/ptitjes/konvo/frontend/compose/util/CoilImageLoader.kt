package io.github.ptitjes.konvo.frontend.compose.util

import androidx.compose.runtime.Composable
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.request.crossfade
import io.github.ptitjes.konvo.core.base.StoragePaths
import kotlinx.io.files.Path
import okio.Path.Companion.toPath
import org.kodein.di.compose.rememberInstance

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
