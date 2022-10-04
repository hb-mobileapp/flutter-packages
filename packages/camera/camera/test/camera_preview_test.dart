// Copyright 2013 The Flutter Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

import 'package:camera/camera.dart';
import 'package:camera_platform_interface/src/types/aspect_ratio.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:quiver/core.dart';

class FakeController extends ValueNotifier<CameraValue>
    implements CameraController {
  FakeController() : super(const CameraValue.uninitialized());

  @override
  Future<void> dispose() async {
    super.dispose();
  }

  @override
  Widget buildPreview() {
    return const Texture(textureId: CameraController.kUninitializedCameraId);
  }

  @override
  int get cameraId => CameraController.kUninitializedCameraId;

  @override
  void debugCheckIsDisposed() {}

  @override
  CameraDescription get description => const CameraDescription(
      name: '', lensDirection: CameraLensDirection.back, sensorOrientation: 0);

  @override
  bool get enableAudio => false;

  @override
  Future<double> getExposureOffsetStepSize() async => 1.0;

  @override
  Future<double> getMaxExposureOffset() async => 1.0;

  @override
  Future<double> getMaxZoomLevel() async => 1.0;

  @override
  Future<double> getMinExposureOffset() async => 1.0;

  @override
  Future<double> getMinZoomLevel() async => 1.0;

  @override
  Future<void> initialize() async {}

  @override
  Future<void> lockCaptureOrientation([DeviceOrientation? orientation]) async {}

  @override
  Future<void> pauseVideoRecording() async {}

  @override
  Future<void> prepareForVideoRecording() async {}

  @override
  ResolutionPreset get resolutionPreset => ResolutionPreset.low;

  @override
  Future<void> resumeVideoRecording() async {}

  @override
  Future<void> setExposureMode(ExposureMode mode) async {}

  @override
  Future<double> setExposureOffset(double offset) async => offset;

  @override
  Future<void> setExposurePoint(Offset? point) async {}

  @override
  Future<void> setFlashMode(FlashMode mode) async {}

  @override
  Future<void> setFocusMode(FocusMode mode) async {}

  @override
  Future<void> setFocusPoint(Offset? point) async {}

  @override
  Future<void> setZoomLevel(double zoom) async {}

  @override
  Future<void> startImageStream(onLatestImageAvailable onAvailable) async {}

  @override
  Future<void> startVideoRecording() async {}

  @override
  Future<void> stopImageStream() async {}

  @override
  Future<XFile> stopVideoRecording() async => XFile('');

  @override
  Future<XFile> takePicture() async => XFile('');

  @override
  Future<void> unlockCaptureOrientation() async {}

  @override
  Future<void> pausePreview() async {}

  @override
  Future<void> resumePreview() async {}

  @override
  Future<List<double>> getAvailableLensApertures() async => List<double>.empty();

  @override
  Future<double> getLensAperture() async => 1;

  @override
  Future<int> getMaxExposureTime() async => 1;

  @override
  Future<int> getMaxSensorSensitivity() async => 1;

  @override
  Future<int> getMinExposureTime() async => 1;

  @override
  Future<int> getMinSensorSensitivity() async => 1;

  @override
  Future<int> setExposureTime(int nanoseconds) async => nanoseconds;

  @override
  Future<double> setLensAperture(double? aperture) async => aperture!;

  @override
  Future<int> setSensorSensitivity(int sensorSensitivity) async => sensorSensitivity;

  @override
  CameraAspectRatio? get aspectRatio => CameraAspectRatio.RATIO_4_3;

  @override
  ImageFormatGroup get imageFormatGroup => ImageFormatGroup.yuv420;
}

void main() {
  group('RotatedBox (Android only)', () {
    testWidgets(
        'when recording rotatedBox should turn according to recording orientation',
        (
      WidgetTester tester,
    ) async {
      debugDefaultTargetPlatformOverride = TargetPlatform.android;

      final FakeController controller = FakeController();
      controller.value = controller.value.copyWith(
        isInitialized: true,
        isRecordingVideo: true,
        deviceOrientation: DeviceOrientation.portraitUp,
        lockedCaptureOrientation:
            const Optional<DeviceOrientation>.fromNullable(
                DeviceOrientation.landscapeRight),
        recordingOrientation: const Optional<DeviceOrientation>.fromNullable(
            DeviceOrientation.landscapeLeft),
        previewSize: const Size(480, 640),
      );

      await tester.pumpWidget(
        Directionality(
          textDirection: TextDirection.ltr,
          child: CameraPreview(controller),
        ),
      );
      expect(find.byType(RotatedBox), findsOneWidget);

      final RotatedBox rotatedBox =
          tester.widget<RotatedBox>(find.byType(RotatedBox));
      expect(rotatedBox.quarterTurns, 3);

      debugDefaultTargetPlatformOverride = null;
    });

    testWidgets(
        'when orientation locked rotatedBox should turn according to locked orientation',
        (
      WidgetTester tester,
    ) async {
      debugDefaultTargetPlatformOverride = TargetPlatform.android;

      final FakeController controller = FakeController();
      controller.value = controller.value.copyWith(
        isInitialized: true,
        deviceOrientation: DeviceOrientation.portraitUp,
        lockedCaptureOrientation:
            const Optional<DeviceOrientation>.fromNullable(
                DeviceOrientation.landscapeRight),
        recordingOrientation: const Optional<DeviceOrientation>.fromNullable(
            DeviceOrientation.landscapeLeft),
        previewSize: const Size(480, 640),
      );

      await tester.pumpWidget(
        Directionality(
          textDirection: TextDirection.ltr,
          child: CameraPreview(controller),
        ),
      );
      expect(find.byType(RotatedBox), findsOneWidget);

      final RotatedBox rotatedBox =
          tester.widget<RotatedBox>(find.byType(RotatedBox));
      expect(rotatedBox.quarterTurns, 1);

      debugDefaultTargetPlatformOverride = null;
    });

    testWidgets(
        'when not locked and not recording rotatedBox should turn according to device orientation',
        (
      WidgetTester tester,
    ) async {
      debugDefaultTargetPlatformOverride = TargetPlatform.android;

      final FakeController controller = FakeController();
      controller.value = controller.value.copyWith(
        isInitialized: true,
        deviceOrientation: DeviceOrientation.portraitUp,
        recordingOrientation: const Optional<DeviceOrientation>.fromNullable(
            DeviceOrientation.landscapeLeft),
        previewSize: const Size(480, 640),
      );

      await tester.pumpWidget(
        Directionality(
          textDirection: TextDirection.ltr,
          child: CameraPreview(controller),
        ),
      );
      expect(find.byType(RotatedBox), findsOneWidget);

      final RotatedBox rotatedBox =
          tester.widget<RotatedBox>(find.byType(RotatedBox));
      expect(rotatedBox.quarterTurns, 0);

      debugDefaultTargetPlatformOverride = null;
    });
  }, skip: kIsWeb);

  testWidgets('when not on Android there should not be a rotated box',
      (WidgetTester tester) async {
    debugDefaultTargetPlatformOverride = TargetPlatform.iOS;
    final FakeController controller = FakeController();
    controller.value = controller.value.copyWith(
      isInitialized: true,
      previewSize: const Size(480, 640),
    );

    await tester.pumpWidget(
      Directionality(
        textDirection: TextDirection.ltr,
        child: CameraPreview(controller),
      ),
    );
    expect(find.byType(RotatedBox), findsNothing);
    expect(find.byType(Texture), findsOneWidget);
    debugDefaultTargetPlatformOverride = null;
  });
}
