// Copyright 2013 The Flutter Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package io.flutter.plugins.camera.features.resolution;

import android.graphics.SurfaceTexture;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.CamcorderProfile;
import android.media.EncoderProfiles;
import android.os.Build;
import android.util.Log;
import android.util.Rational;
import android.util.Size;

import io.flutter.plugins.camera.CameraProperties;
import io.flutter.plugins.camera.features.CameraFeature;
import io.flutter.plugins.camera.features.aspectratio.CompareSizesByArea;
import io.flutter.plugins.camera.features.aspectratio.ImageFormatConstants;
import io.flutter.plugins.camera.types.AspectRatio;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controls the resolutions configuration on the {@link android.hardware.camera2} API.
 *
 * <p>The {@link ResolutionFeature} is responsible for converting the platform independent {@link
 * ResolutionPreset} into a {@link android.media.CamcorderProfile} which contains all the properties
 * required to configure the resolution using the {@link android.hardware.camera2} API.
 */
public class AspectRatioFeature extends CameraFeature<AspectRatio> {
    private String TAG = "AspectRatioFeature";
    private Size captureSize;
    private Size previewSize;
    private AspectRatio currentSetting;
    private static final int ALIGN16 = 16;
    private int imageFormat;
    private int cameraId;
    private boolean isSensorLandscapeResolution;

    private static final Size DEFAULT_SIZE = new Size(640, 480);
    private static final Rational ASPECT_RATIO_4_3 = new Rational(4, 3);
    private static final Rational ASPECT_RATIO_3_4 = new Rational(3, 4);
    private static final Rational ASPECT_RATIO_16_9 = new Rational(16, 9);
    private static final Rational ASPECT_RATIO_9_16 = new Rational(9, 16);

    /**
     * Creates a new instance of the {@link ResolutionFeature}.
     *
     * @param cameraProperties Collection of characteristics for the current camera device.
     * @param aspectRatio Platform agnostic enum containing resolution information.
     * @param cameraName Camera identifier of the camera for which to configure the resolution.
     */
    public AspectRatioFeature(
            CameraProperties cameraProperties, AspectRatio aspectRatio, int imageFormat, String cameraName) {
        super(cameraProperties);
        this.currentSetting = aspectRatio;
        this.imageFormat = imageFormat;
        this.isSensorLandscapeResolution = cameraProperties.isSensorLandscapeResolution();

        try {
            this.cameraId = Integer.parseInt(cameraName, 10);
        } catch (NumberFormatException e) {
            this.cameraId = -1;
            return;
        }
        configureResolution(aspectRatio, cameraId);
    }

    /**
     * Gets the optimal preview size based on the configured resolution.
     *
     * @return The optimal preview size.
     */
    public Size getPreviewSize() {
        return this.previewSize;
    }

    /**
     * Gets the optimal capture size based on the configured resolution.
     *
     * @return The optimal capture size.
     */
    public Size getCaptureSize() {
        return this.captureSize;
    }

    @Override
    public String getDebugName() {
        return "ResolutionFeature";
    }

    @Override
    public AspectRatio getValue() {
        return currentSetting;
    }

    @Override
    public void setValue(AspectRatio value) {
        this.currentSetting = value;
        configureResolution(currentSetting, cameraId);
    }

    @Override
    public boolean checkIsSupported() {
        return cameraId >= 0;
    }

    @Override
    public void updateBuilder(CaptureRequest.Builder requestBuilder) {
        // No-op: when setting a resolution there is no need to update the request builder.
    }


    private void configureResolution(AspectRatio aspectRatio, int cameraId)
            throws IndexOutOfBoundsException {
        if (!checkIsSupported()) {
            return;
        }

        List<Size> supportedOutputSizes = getSupportedOutputSizes(aspectRatio);
        if(supportedOutputSizes.isEmpty()){
            throw new IllegalArgumentException(
                    "No supported surface combination is found for camera device - Id : " + cameraId);
        }

        captureSize = supportedOutputSizes.get(0);
        previewSize = supportedOutputSizes.get(0);
    }

    private List<Size> getSupportedOutputSizes(AspectRatio targetAspectRatio){
        StreamConfigurationMap map = cameraProperties.getConfigurationMap();

        if(map == null){
            throw new IllegalStateException("Failed to get configuration map");
        }

        Size[] outputSizes = map.getOutputSizes(imageFormat);


        if (outputSizes == null) {
            outputSizes = getAllOutputSizesByFormat(imageFormat);
        }

        // Sort the output sizes. The Comparator result must be reversed to have a descending order
        // result.
        Arrays.sort(outputSizes, new CompareSizesByArea(true));
        List<Size> outputSizeCandidates = Arrays.asList(outputSizes);

        if (outputSizeCandidates.isEmpty()) {
            throw new IllegalArgumentException(
                    "Can not get supported output size for the format: "
                            + imageFormat);
        }

        Map<Rational, List<Size>> aspectRatioSizeListMap = groupSizesByAspectRatio(outputSizeCandidates);

        Rational aspectRatio = getTargetAspectRatio(targetAspectRatio);
        List<Size> supportedResolutions = new ArrayList<>();

        // Sort the aspect ratio key set by the target aspect ratio.
        List<Rational> aspectRatios = new ArrayList<>(aspectRatioSizeListMap.keySet());

        Collections.sort(aspectRatios,
                new CompareAspectRatiosByDistanceToTargetRatio(aspectRatio));

        for (Rational rational : aspectRatios) {
            for (Size size : aspectRatioSizeListMap.get(rational)) {
                // A size may exist in multiple groups in mod16 condition. Keep only one in
                // the final list.
                if (!supportedResolutions.contains(size)) {
                    supportedResolutions.add(size);
                }
            }
        }
        return supportedResolutions;
    }


    private Rational getTargetAspectRatio(AspectRatio aspectRatio){
        Rational outputRatio = null;

        switch(aspectRatio){
            case RATIO_4_3:
                outputRatio = isSensorLandscapeResolution ? ASPECT_RATIO_4_3 : ASPECT_RATIO_3_4;
                break;
            case RATIO_16_9:
                outputRatio = isSensorLandscapeResolution ? ASPECT_RATIO_16_9 : ASPECT_RATIO_9_16;
                break;
        }
        return outputRatio;
    }


    private Map<Rational, List<Size>> groupSizesByAspectRatio(List<Size> sizes) {
        Map<Rational, List<Size>> aspectRatioSizeListMap = new HashMap<>();

        // Add 4:3 and 16:9 entries first. Most devices should mainly have supported sizes of
        // these two aspect ratios. Adding them first can avoid that if the first one 4:3 or 16:9
        // size is a mod16 alignment size, the aspect ratio key may be different from the 4:3 or
        // 16:9 value.
        aspectRatioSizeListMap.put(ASPECT_RATIO_4_3, new ArrayList<>());
        aspectRatioSizeListMap.put(ASPECT_RATIO_16_9, new ArrayList<>());

        for (Size outputSize : sizes) {
            Rational matchedKey = null;

            for (Rational key : aspectRatioSizeListMap.keySet()) {
                // Put the size into all groups that is matched in mod16 condition since a size
                // may match multiple aspect ratio in mod16 algorithm.
                if (hasMatchingAspectRatio(outputSize, key)) {
                    matchedKey = key;

                    List<Size> sizeList = aspectRatioSizeListMap.get(matchedKey);
                    if (!sizeList.contains(outputSize)) {
                        sizeList.add(outputSize);
                    }
                }
            }

            // Create new item if no matching group is found.
            if (matchedKey == null) {
                aspectRatioSizeListMap.put(
                        new Rational(outputSize.getWidth(), outputSize.getHeight()),
                        new ArrayList<>(Collections.singleton(outputSize)));
            }
        }

        return aspectRatioSizeListMap;
    }

    private int getArea(Size size) {
        return size.getWidth() * size.getHeight();
    }

    private boolean hasMatchingAspectRatio(Size resolution, Rational aspectRatio) {
        boolean isMatch = false;
        if (aspectRatio == null) {
            isMatch = false;
        } else if (aspectRatio.equals(
                new Rational(resolution.getWidth(), resolution.getHeight()))) {
            isMatch = true;
        } else if (getArea(resolution) >= getArea(DEFAULT_SIZE)) {
            // Only do mod 16 calculation if the size is equal to or larger than 640x480. It is
            // because the aspect ratio will be affected critically by mod 16 calculation if the
            // size is small. It may result in unexpected outcome such like 256x144 will be
            // considered as 18.5:9.
            isMatch = isPossibleMod16FromAspectRatio(resolution, aspectRatio);
        }

        return isMatch;
    }

    private static boolean isPossibleMod16FromAspectRatio(Size resolution, Rational aspectRatio) {
        int width = resolution.getWidth();
        int height = resolution.getHeight();

        Rational invAspectRatio = new Rational(/* numerator= */aspectRatio.getDenominator(),
                /* denominator= */aspectRatio.getNumerator());
        if (width % 16 == 0 && height % 16 == 0) {
            return ratioIntersectsMod16Segment(Math.max(0, height - ALIGN16), width, aspectRatio)
                    || ratioIntersectsMod16Segment(Math.max(0, width - ALIGN16), height,
                    invAspectRatio);
        } else if (width % 16 == 0) {
            return ratioIntersectsMod16Segment(height, width, aspectRatio);
        } else if (height % 16 == 0) {
            return ratioIntersectsMod16Segment(width, height, invAspectRatio);
        }
        return false;
    }


    private static boolean ratioIntersectsMod16Segment(int height, int mod16Width,
                                                       Rational aspectRatio) {
        double aspectRatioWidth =
                height * aspectRatio.getNumerator() / (double) aspectRatio.getDenominator();
        return aspectRatioWidth > Math.max(0, mod16Width - ALIGN16) && aspectRatioWidth < (
                mod16Width + ALIGN16);
    }

    private Size[] getAllOutputSizesByFormat(int imageFormat){
        StreamConfigurationMap map = cameraProperties.getConfigurationMap();
        Size[] outputSizes;

        if (map == null) {
            throw new IllegalArgumentException("Can not retrieve SCALER_STREAM_CONFIGURATION_MAP");
        }

        if (Build.VERSION.SDK_INT < 23
                && imageFormat == ImageFormatConstants.INTERNAL_DEFINED_IMAGE_FORMAT_PRIVATE) {
            // This is a little tricky that 0x22 that is internal defined in
            // StreamConfigurationMap.java to be equal to ImageFormat.PRIVATE that is public
            // after Android level 23 but not public in Android L. Use {@link SurfaceTexture}
            // or {@link MediaCodec} will finally mapped to 0x22 in StreamConfigurationMap to
            // retrieve the output sizes information.
            outputSizes = map.getOutputSizes(SurfaceTexture.class);
        } else {
            outputSizes = map.getOutputSizes(imageFormat);
        }

        if (outputSizes == null) {
            throw new IllegalArgumentException(
                    "Can not get supported output size for the format: " + imageFormat);
        }

        // Sort the output sizes. The Comparator result must be reversed to have a descending order
        // result.
        Arrays.sort(outputSizes, new CompareSizesByArea(true));

        return outputSizes;
    }

    private class CompareAspectRatiosByDistanceToTargetRatio implements Comparator<Rational> {

        private Rational targetRatio;

        CompareAspectRatiosByDistanceToTargetRatio(Rational targetRatio){
            this.targetRatio = targetRatio;
        }

        @Override
        public int compare(Rational lhs, Rational rhs) {
            if(lhs.equals(rhs)){
                return 0;
            }

            final Float lhsRatioDelta = Math.abs(lhs.floatValue() - targetRatio.floatValue());
            final Float rhsRatioDelta = Math.abs(rhs.floatValue() - targetRatio.floatValue());

            int result = (int) Math.signum(lhsRatioDelta - rhsRatioDelta);
            return result;
        }
    }

}
