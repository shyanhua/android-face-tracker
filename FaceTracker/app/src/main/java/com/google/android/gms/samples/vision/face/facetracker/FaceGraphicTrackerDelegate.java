package com.google.android.gms.samples.vision.face.facetracker;

import com.google.android.gms.vision.face.Face;

public interface FaceGraphicTrackerDelegate
{
    void faceGraphicTrackerNewFaceDetected(Face face, Integer id);
}
