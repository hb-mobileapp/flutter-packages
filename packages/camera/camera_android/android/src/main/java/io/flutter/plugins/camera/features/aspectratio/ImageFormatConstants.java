package io.flutter.plugins.camera.features.aspectratio;

import androidx.annotation.RequiresApi;

@RequiresApi(21)
public final class ImageFormatConstants {
    // Internal format in StreamConfigurationMap.java that will be mapped to public ImageFormat.JPEG
    public static final int INTERNAL_DEFINED_IMAGE_FORMAT_JPEG = 0x21;

    // Internal format HAL_PIXEL_FORMAT_IMPLEMENTATION_DEFINED (0x22) in StreamConfigurationMap.java
    // that will be mapped to public ImageFormat.PRIVATE after android level 23.
    public static final int INTERNAL_DEFINED_IMAGE_FORMAT_PRIVATE = 0x22;

    private ImageFormatConstants() {
    }
}

