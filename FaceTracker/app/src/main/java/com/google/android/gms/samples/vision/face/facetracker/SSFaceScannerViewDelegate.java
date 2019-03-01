package com.google.android.gms.samples.vision.face.facetracker;

import android.graphics.Bitmap;

public interface SSFaceScannerViewDelegate
{
    void captureCompletedForSelfie(Bitmap bitmap);
}
