/*
 * Copyright 2019 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package detection;

import android.app.Activity;
import android.content.Context;
import android.graphics.*;
import android.graphics.Bitmap.Config;
import android.graphics.Paint.Style;
import android.media.ImageReader.OnImageAvailableListener;
import android.os.SystemClock;
import android.util.Size;
import android.util.TypedValue;
import android.widget.Toast;
import androidx.fragment.app.FragmentManager;
import com.example.insight.BTSerial.BLE;
import com.example.insight.BTSerial.BrailleParser;
import com.example.insight.R;
import detection.customview.OverlayView;
import detection.customview.OverlayView.DrawCallback;
import detection.env.BorderedText;
import detection.env.ImageUtils;
import org.tensorflow.lite.examples.detection.tflite.Detector;
import org.tensorflow.lite.examples.detection.tflite.TFLiteObjectDetectionAPIModel;
import detection.tracking.MultiBoxTracker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * An activity that uses a TensorFlowMultiBoxDetector and ObjectTracker to detect and then track
 * objects.
 */
public class DetectorActivity extends CameraActivity implements OnImageAvailableListener {

  // Configuration values for the prepackaged SSD model.
  private static final int TF_OD_API_INPUT_SIZE = 300;
  private static final boolean TF_OD_API_IS_QUANTIZED = true;
  private static String TF_OD_API_MODEL_FILE = "model1.tflite";
  private static String TF_OD_API_LABELS_FILE = "labelmap1.txt";
  private static final DetectorMode MODE = DetectorMode.TF_OD_API;
  // Minimum detection confidence to track a detection.
  private static final float MINIMUM_CONFIDENCE_TF_OD_API = 0.5f;
  private static final boolean MAINTAIN_ASPECT = false;
  private static final Size DESIRED_PREVIEW_SIZE = new Size(640, 480);
  private static final boolean SAVE_PREVIEW_BITMAP = false;
  private static final float TEXT_SIZE_DIP = 10;
  private final byte[] zeroArray = {127,0,0,0,0,0,0,0,0,0,0};
  OverlayView trackingOverlay;
  private Integer sensorOrientation;

  private Detector detector;

  private long lastProcessingTimeMs;
  private Bitmap rgbFrameBitmap = null;
  private Bitmap croppedBitmap = null;
  private Bitmap cropCopyBitmap = null;

  private boolean computingDetection = false;

  private long timestamp = 0;

  private Matrix frameToCropTransform;
  private Matrix cropToFrameTransform;

  private MultiBoxTracker tracker;

  private BorderedText borderedText;
  public static Detector.Recognition sharedRecognition = null;

  public DetectorActivity(Context context, Activity activity, FragmentManager fragmentManager) {
    super(context, activity, fragmentManager);
    semaphoreRelease(2000);
  }

  public DetectorActivity(Context context, Activity activity, FragmentManager fragmentManager, Boolean mode) {
    super(context, activity, fragmentManager);
    semaphoreRelease(2000);
    if (mode) {
      TF_OD_API_MODEL_FILE = "model1.tflite";
      TF_OD_API_LABELS_FILE = "labelmap1.txt";
    } else {
      TF_OD_API_MODEL_FILE = "model.tflite";
      TF_OD_API_LABELS_FILE = "labelmap.txt";
    }
  }

  @Override
  public void onPreviewSizeChosen(final Size size, final int rotation) {
    final float textSizePx =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP, context.getResources().getDisplayMetrics());
    borderedText = new BorderedText(textSizePx);
    borderedText.setTypeface(Typeface.MONOSPACE);

    tracker = new MultiBoxTracker(context);

    int cropSize = TF_OD_API_INPUT_SIZE;

    try {
      detector =
          TFLiteObjectDetectionAPIModel.create(
              context,
              TF_OD_API_MODEL_FILE,
              TF_OD_API_LABELS_FILE,
              TF_OD_API_INPUT_SIZE,
              TF_OD_API_IS_QUANTIZED);
      cropSize = TF_OD_API_INPUT_SIZE;
    } catch (final IOException e) {
      e.printStackTrace();
      Toast toast =
          Toast.makeText(
              context, "Detector could not be initialized", Toast.LENGTH_SHORT);
      toast.show();
    }

    previewWidth = size.getWidth();
    previewHeight = size.getHeight();

    sensorOrientation = rotation;// - getScreenOrientation();
    rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Config.ARGB_8888);
    croppedBitmap = Bitmap.createBitmap(cropSize, cropSize, Config.ARGB_8888);

    frameToCropTransform =
        ImageUtils.getTransformationMatrix(
            previewWidth, previewHeight,
            cropSize, cropSize,
            sensorOrientation, MAINTAIN_ASPECT);

    cropToFrameTransform = new Matrix();
    frameToCropTransform.invert(cropToFrameTransform);

    trackingOverlay = (OverlayView) activity.findViewById(R.id.tracking_overlay);
    trackingOverlay.addCallback(
        new DrawCallback() {
          @Override
          public void drawCallback(final Canvas canvas) {
            tracker.draw(canvas);
            if (isDebug()) {
              tracker.drawDebug(canvas);
            }
          }
        });

    tracker.setFrameConfiguration(previewWidth, previewHeight, sensorOrientation);
    detector.setNumThreads(1);
  }

  @Override
  protected void processImage() {
    ++timestamp;
    final long currTimestamp = timestamp;
    trackingOverlay.postInvalidate();

    // No mutex needed as this method is not reentrant.
    if (computingDetection) {
      readyForNextImage();
      return;
    }
    computingDetection = true;

    rgbFrameBitmap.setPixels(getRgbBytes(), 0, previewWidth, 0, 0, previewWidth, previewHeight);

    readyForNextImage();

    final Canvas canvas = new Canvas(croppedBitmap);
    canvas.drawBitmap(rgbFrameBitmap, frameToCropTransform, null);
    // For examining the actual TF input.
    if (SAVE_PREVIEW_BITMAP) {
      ImageUtils.saveBitmap(croppedBitmap);
    }

    try {
      TimeUnit.MILLISECONDS.sleep(100);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    runInBackground(
        new Runnable() {
          @Override
          public void run() {
            final long startTime = SystemClock.uptimeMillis();
            final List<Detector.Recognition> results = detector.recognizeImage(croppedBitmap);
            lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime;

            cropCopyBitmap = Bitmap.createBitmap(croppedBitmap);
            final Canvas canvas = new Canvas(cropCopyBitmap);
            final Paint paint = new Paint();
            paint.setColor(Color.RED);
            paint.setStyle(Style.STROKE);
            paint.setStrokeWidth(2.0f);

            float minimumConfidence = MINIMUM_CONFIDENCE_TF_OD_API;
            switch (MODE) {
              case TF_OD_API:
                minimumConfidence = MINIMUM_CONFIDENCE_TF_OD_API;
                break;
            }

            final List<Detector.Recognition> mappedRecognitions =
                new ArrayList<Detector.Recognition>();

            for (final Detector.Recognition result : results) {
              final RectF location = result.getLocation();
              if (location != null && result.getConfidence() >= minimumConfidence) {
                canvas.drawRect(location, paint);

                cropToFrameTransform.mapRect(location);

                result.setLocation(location);
                mappedRecognitions.add(result);
              }
            }
            tracker.trackResults(mappedRecognitions, currTimestamp);
            trackingOverlay.postInvalidate();

            computingDetection = false;

            if (cameraSemaphore.tryAcquire()) {
              sendToPriotittyModule(mappedRecognitions);
            }
          }
        });
  }

  @Override
  protected int getLayoutId() {
    return R.layout.tfe_od_camera_connection_fragment_tracking;
  }

  @Override
  protected Size getDesiredPreviewFrameSize() {
    return DESIRED_PREVIEW_SIZE;
  }


  // Which detection model to use: by default uses Tensorflow Object Detection API frozen
  // checkpoints.
  private enum DetectorMode {
    TF_OD_API;
  }

  @Override
  protected void setUseNNAPI(final boolean isChecked) {
    runInBackground(
        () -> {
          try {
            detector.setUseNNAPI(isChecked);
          } catch (UnsupportedOperationException e) {
            runInBackground(
                () -> {
                  Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                });
          }
        });
  }

  @Override
  protected void setNumThreads(final int numThreads) {
    runInBackground(() -> detector.setNumThreads(numThreads));
  }


  //Priority done by percentage area the object is taking
  //up of the camera view. highest priority will be sent
  //to ble
  @Override
  protected void sendToPriotittyModule(List<Detector.Recognition> recognitionList){
    runInBackground(new Runnable() {
      @Override
      public void run() {
        if (!recognitionList.isEmpty()) {
          Detector.Recognition highestPriority = null;
          float highestArea = -1;

          //get the biggest area
          for (Detector.Recognition result : recognitionList) {
            float area = result.getLocation().height() * result.getLocation().width();
            if (area > highestArea) {
              highestArea = area;
              highestPriority = result;
            }
          }
          sendCameraDetectionToBle(highestPriority.getTitle());
        }

      }
    });
  }

  protected void semaphoreRelease(long delay){
    runDelayed(new Runnable() {
      @Override
      public void run() {
        cameraSemaphore.release();
      }
    },delay);
  }

  //Split String to characters to send to ble in 500ms intervals
  private void sendCameraDetectionToBle(String detectString){
    char[] detectChars = detectString.toCharArray();
    for (int i = 0; i < detectChars.length; i++){
      final char charToPrint = detectChars[i];
      runDelayed(new Runnable() {
        @Override
        public void run() {
          int [] a = BrailleParser.parse(charToPrint);
          ble.writeToGatt(BLE.RIGHT_GATT,BrailleParser.parse(charToPrint));
        }
      },500*i);
    }

    //Sending 0 to Right BLE
    runDelayed(new Runnable() {
      @Override
      public void run() {
        ble.writeToGatt(BLE.RIGHT_GATT,zeroArray);
      }
    },(detectChars.length+1) * 500);

    //Semaphore release with delay
    semaphoreRelease(detectChars.length * 500 + 2000);
  }

  @Override
  protected Detector.Recognition recognitionClone(Detector.Recognition oldRec){
    return new Detector.Recognition(oldRec.getId(),oldRec.getTitle(),oldRec.getConfidence(),oldRec.getLocation());
  }
}
