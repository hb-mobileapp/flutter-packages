package io.flutter.plugins.camera;

import android.content.Context;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;

import java.util.ArrayList;
import java.util.List;

public class SupportedSurfaceCombination {

    private final List<SurfaceCombination> mSurfaceCombinations = new ArrayList<>();

    private final CameraCharacteristicsCompat mCharacteristics;
    private final int mHardwareLevel;

    SupportedSurfaceCombination(Context context){
        CameraCharacteristics.
        CameraManager cameraManager = context.getSystemService()

        try {
            mCharacteristics = cameraManagerCompat.getCameraCharacteristicsCompat(mCameraId);
            Integer keyValue = mCharacteristics.get(
                    CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
            mHardwareLevel = keyValue != null ? keyValue
                    : CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY;
            mIsSensorLandscapeResolution = isSensorLandscapeResolution();
        } catch (CameraAccessExceptionCompat e) {
            throw CameraUnavailableExceptionHelper.createFrom(e);
        }

        generateSupportedCombinationList();
    }

    private void generateSupportedCombinationList() {
        mSurfaceCombinations.addAll(getLegacySupportedCombinationList());

        if (mHardwareLevel == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED
                || mHardwareLevel == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL
                || mHardwareLevel == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_3) {
            mSurfaceCombinations.addAll(getLimitedSupportedCombinationList());
        }

        if (mHardwareLevel == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL
                || mHardwareLevel == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_3) {
            mSurfaceCombinations.addAll(getFullSupportedCombinationList());
        }

        int[] availableCapabilities =
                mCharacteristics.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES);

        if (availableCapabilities != null) {
            for (int capability : availableCapabilities) {
                if (capability == CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_RAW) {
                    mIsRawSupported = true;
                } else if (capability
                        == CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_BURST_CAPTURE) {
                    mIsBurstCaptureSupported = true;
                }
            }
        }

        if (mIsRawSupported) {
            mSurfaceCombinations.addAll(getRAWSupportedCombinationList());
        }

        if (mIsBurstCaptureSupported
                && mHardwareLevel == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED) {
            mSurfaceCombinations.addAll(getBurstSupportedCombinationList());
        }

        if (mHardwareLevel == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_3) {
            mSurfaceCombinations.addAll(getLevel3SupportedCombinationList());
        }

        mSurfaceCombinations.addAll(
                mExtraSupportedSurfaceCombinationsContainer.get(mCameraId, mHardwareLevel));
    }
}
