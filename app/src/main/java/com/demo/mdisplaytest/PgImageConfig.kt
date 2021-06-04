package com.demo.mdisplaytest
import de.proglove.sdk.scanner.PgImageConfig.Companion.JPEG_MAXIMUM_QUALITY
import de.proglove.sdk.scanner.PgImageConfig.Companion.JPEG_MINIMUM_QUALITY
import de.proglove.sdk.commands.IPgCommandData

/**
 * Contains configuration parameters for an image
 *
 * @property jpegQuality desired JPEG quality. Should be a value from [JPEG_MINIMUM_QUALITY] to [JPEG_MAXIMUM_QUALITY]
 * @property resolution desired Image resolution. Select From predefined values
 */
data class PgImageConfig @JvmOverloads constructor(
        val jpegQuality: Int = 20,
        val resolution: ImageResolution = ImageResolution.RESOLUTION_640_480,
        val timeoutMs: Int = DEFAULT_IMAGE_TIMEOUT) : IPgCommandData<PgImageConfig> {

    companion object {

        const val JPEG_MINIMUM_QUALITY = 5
        const val JPEG_MAXIMUM_QUALITY = 100
        const val DEFAULT_IMAGE_TIMEOUT = 10000
    }
}

enum class ImageResolution { RESOLUTION_1280_960, RESOLUTION_640_480, RESOLUTION_320_240 }





