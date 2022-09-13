// Copyright 2013 The Flutter Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package io.flutter.plugins.camera.features.lensaperture;

import android.annotation.SuppressLint;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CaptureRequest;
import android.util.Range;

import androidx.annotation.NonNull;

import java.util.Arrays;

import io.flutter.plugins.camera.CameraProperties;
import io.flutter.plugins.camera.features.CameraFeature;

/** Controls the exposure offset making the resulting image brighter or darker. */
public class LensApertureFeature extends CameraFeature<Float> {

  private Float currentSetting = 0f;

  /**
   * Creates a new instance of the {@link LensApertureFeature}.
   *
   * @param cameraProperties Collection of the characteristics for the current camera device.
   */
  public LensApertureFeature(CameraProperties cameraProperties) {
    super(cameraProperties);
  }

  @Override
  public String getDebugName() {
    return "LensApertureFeature";
  }

  @Override
  public Float getValue() {
    return currentSetting;
  }

  @Override
  public void setValue(@NonNull Float value) {
      this.currentSetting = value;
  }


  @Override
  public boolean checkIsSupported() {
    float[] modes = cameraProperties.getAvailableLensApertures();

    /// If there's at least one lens aperture is available then we are supported.
    return modes != null && modes.length > 0;
  }


  /**
   * Returns all available lens apertures
   */
  public float[] getAvailableLensApertures() {
    return cameraProperties.getAvailableLensApertures();
  }

  @SuppressLint("NewApi")
  @Override
  public void updateBuilder(CaptureRequest.Builder requestBuilder) {
    if (!checkIsSupported()) {
      return;
    }

    requestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_OFF);
    requestBuilder.set(CaptureRequest.LENS_APERTURE, currentSetting);
  }
}
