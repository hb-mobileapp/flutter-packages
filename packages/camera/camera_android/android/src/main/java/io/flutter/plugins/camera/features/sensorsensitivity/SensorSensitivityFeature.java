// Copyright 2013 The Flutter Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package io.flutter.plugins.camera.features.sensorsensitivity;

import android.hardware.camera2.CaptureRequest;
import android.util.Range;

import androidx.annotation.NonNull;

import io.flutter.plugins.camera.CameraProperties;
import io.flutter.plugins.camera.features.CameraFeature;

/**
 * Controls the exposure offset making the resulting image brighter or darker.
 */
public class SensorSensitivityFeature extends CameraFeature<Integer> {
    private Integer currentSetting = 0;

    /**
     * Creates a new instance of the {@link SensorSensitivityFeature}.
     *
     * @param cameraProperties Collection of the characteristics for the current camera device.
     */
    public SensorSensitivityFeature(CameraProperties cameraProperties) {
        super(cameraProperties);
    }

    @Override
    public String getDebugName() {
        return "SensorSensitivityFeature";
    }

    @Override
    public Integer getValue() {
        return currentSetting;
    }

    @Override
    public void setValue(@NonNull Integer value) {
        this.currentSetting = value;
    }

    // Available on all devices.
    @Override
    public boolean checkIsSupported() {
        return true;
    }

    @Override
    public void updateBuilder(CaptureRequest.Builder requestBuilder) {
        if (!checkIsSupported()) {
            return;
        }
        requestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_OFF);
        requestBuilder.set(CaptureRequest.SENSOR_SENSITIVITY, currentSetting);
    }


    /**
     * Returns the minimum sensor sensitivity.
     *
     * @return Interger minimum sensor sensitivity.
     */
    public Integer getMinSensorSensitivity() {
        Range<Integer> range = cameraProperties.getSensorSensitivityRange();
        return range.getLower();
    }

    /**
     * Returns the maximum sensor sensitivity.
     *
     * @return Interger maximum sensor sensitivity.
     */
    public Integer getMaxSensorSensitivity() {
        Range<Integer> range = cameraProperties.getSensorSensitivityRange();
        return range.getUpper();
    }
}
