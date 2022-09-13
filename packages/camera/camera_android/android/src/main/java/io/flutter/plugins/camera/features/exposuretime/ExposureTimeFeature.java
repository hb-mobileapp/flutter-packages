// Copyright 2013 The Flutter Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package io.flutter.plugins.camera.features.exposuretime;

import android.hardware.camera2.CaptureRequest;
import android.util.Range;
import androidx.annotation.NonNull;
import io.flutter.plugins.camera.CameraProperties;
import io.flutter.plugins.camera.features.CameraFeature;

/** Controls the exposure offset making the resulting image brighter or darker. */
public class ExposureTimeFeature extends CameraFeature<Long> {
  private long currentSetting = 0;

  /**
   * Creates a new instance of the {@link ExposureTimeFeature}.
   *
   * @param cameraProperties Collection of the characteristics for the current camera device.
   */
  public ExposureTimeFeature(CameraProperties cameraProperties) {
    super(cameraProperties);
  }

  @Override
  public String getDebugName() {
    return "ExposureTimeFeature";
  }

  @Override
  public Long getValue() {
    return currentSetting;
  }

  @Override
  public void setValue(@NonNull Long value) {
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
    requestBuilder.set(CaptureRequest.SENSOR_EXPOSURE_TIME, currentSetting);
  }


  /**
   * Returns the minimum exposure time.
   *
   * @return double minimum exposure time.
   */
  public Long getMinExposureTime() {
    Range<Long> range = cameraProperties.getExposureTimeRange();
    return range.getLower();
  }

  /**
   * Returns the maximum exposure time.
   *
   * @return double maximum exposure time.
   */
  public Long getMaxExposureTime() {
    Range<Long> range = cameraProperties.getExposureTimeRange();
    return range.getUpper();
  }
}
