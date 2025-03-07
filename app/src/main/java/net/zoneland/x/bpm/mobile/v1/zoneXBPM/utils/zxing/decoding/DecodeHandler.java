/*
 * Copyright (C) 2010 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.zxing.decoding;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

import net.zoneland.x.bpm.mobile.v1.zoneXBPM.R;
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.zxing.AutoFocusUtils;
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.zxing.activity.CaptureActivity;
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.zxing.camera.CameraManager;
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.zxing.camera.PlanarYUVLuminanceSource;

import java.util.Hashtable;
import java.util.Map;


final class DecodeHandler extends Handler {

  private static final String TAG = DecodeHandler.class.getSimpleName();

  private final CaptureActivity activity;
//  private final MultiFormatReader multiFormatReader;
  private final QRCodeReader mQrCodeReader;
  private final Map<DecodeHintType, Object> mHints;

  DecodeHandler(CaptureActivity activity, Hashtable<DecodeHintType, Object> hints) {
//    multiFormatReader = new MultiFormatReader();
//    multiFormatReader.setHints(hints);
    this.activity = activity;
    mQrCodeReader = new QRCodeReader();
    mHints = new Hashtable<>();
    // 使用一种编码 二维码
    mHints.put(DecodeHintType.CHARACTER_SET, "utf-8");
    mHints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
    mHints.put(DecodeHintType.POSSIBLE_FORMATS, BarcodeFormat.QR_CODE);
  }

  @Override
  public void handleMessage(Message message) {
    switch (message.what) {
      case R.id.decode:
        //Log.d(TAG, "Got decode message");
        decode((byte[]) message.obj, message.arg1, message.arg2);
        break;
      case R.id.quit:
        Looper.myLooper().quit();
        break;
    }
  }

  /**
   * Decode the data within the viewfinder rectangle, and time how long it took. For efficiency,
   * reuse the same reader objects from one decode to the next.
   *
   * @param data   The YUV preview frame.
   * @param width  The width of the preview frame.
   * @param height The height of the preview frame.
   */
  private void decode(byte[] data, int width, int height) {
    long start = System.currentTimeMillis();
    Result rawResult = null;
    
    //modify here
    byte[] rotatedData = new byte[data.length];
    for (int y = 0; y < height; y++) {
        for (int x = 0; x < width; x++)
            rotatedData[x * height + height - y - 1] = data[x + y * width];
    }
    int tmp = width; // Here we are swapping, that's the difference to #11
    width = height;
    height = tmp;
    
    PlanarYUVLuminanceSource source = CameraManager
            .get().buildLuminanceSource(rotatedData, width, height);
    BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
//    BinaryBitmap bitmap = new BinaryBitmap(new GlobalHistogramBinarizer(source));
//    AutoFocusUtils.autoFocus(bitmap,mHints);
    try {
//      rawResult = multiFormatReader.decodeWithState(bitmap);
      rawResult = mQrCodeReader.decode(bitmap);
    } catch (ReaderException re) {
      // continue
    } finally {
//      multiFormatReader.reset();
      mQrCodeReader.reset();
    }

    if (rawResult != null) {
      long end = System.currentTimeMillis();
      Log.d(TAG, "Found barcode (" + (end - start) + " ms):\n" + rawResult.toString());
      Message message = Message.obtain(activity.getHandler(), R.id.decode_succeeded, rawResult);
      Bundle bundle = new Bundle();
      bundle.putParcelable(DecodeThread.BARCODE_BITMAP, source.renderCroppedGreyscaleBitmap());
      message.setData(bundle);
      //Log.d(TAG, "Sending decode succeeded message...");
      message.sendToTarget();
    } else {
      Message message = Message.obtain(activity.getHandler(), R.id.decode_failed);
      message.sendToTarget();
    }
  }

}
