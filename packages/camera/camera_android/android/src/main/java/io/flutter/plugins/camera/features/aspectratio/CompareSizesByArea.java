package io.flutter.plugins.camera.features.aspectratio;

import android.util.Size;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.util.Comparator;

@RequiresApi(21) // TODO(b/200306659): Remove and replace with annotation on package-info.java
public final class CompareSizesByArea implements Comparator<Size> {
    private boolean mReverse = false;

    /** Creates a comparator with standard total ordering. */
    public CompareSizesByArea() {
        this(false);
    }

    /** Creates a comparator which can reverse the total ordering. */
    public CompareSizesByArea(boolean reverse) {
        mReverse = reverse;
    }

    @Override
    public int compare(@NonNull Size lhs, @NonNull Size rhs) {
        // We cast here to ensure the multiplications won't overflow
        int result =
                Long.signum(
                        (long) lhs.getWidth() * lhs.getHeight()
                                - (long) rhs.getWidth() * rhs.getHeight());

        if (mReverse) {
            result *= -1;
        }

        return result;
    }
}
