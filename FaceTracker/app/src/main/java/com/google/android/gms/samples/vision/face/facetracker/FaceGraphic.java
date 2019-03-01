/*
 * Copyright (C) The Android Open Source Project
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
package com.google.android.gms.samples.vision.face.facetracker;


import android.graphics.Canvas;

import com.google.android.gms.samples.vision.face.facetracker.ui.camera.GraphicOverlay;
import com.google.android.gms.vision.face.Face;

/**
 * Graphic instance for rendering face position, orientation, and landmarks within an associated
 * graphic overlay view.
 */
class FaceGraphic extends GraphicOverlay.Graphic
{
    private volatile Face mFace;
    private int mFaceId;

    /**
     * Detect & draws the face annotations for position on the supplied canvas.
     */
    @Override
    public void draw(Canvas canvas)
    {
        Face face = mFace;
    }

    //==============================================================================================
    // FaceGraphic Constructor
    //==============================================================================================

    /**
     * Create face annotations here
     */
    FaceGraphic(GraphicOverlay overlay)
    {
        super(overlay);
    }

    //==============================================================================================
    // Public Methods
    //==============================================================================================

    public int getId()
    {
        return mFaceId;
    }

    public void setId(int id)
    {
        mFaceId = id;
    }

    /**
     * Updates the face instance from the detection of the most recent frame.  Invalidates the
     * relevant portions of the overlay to trigger a redraw.
     */
    public void updateFace(Face face)
    {
        mFace = face;
        postInvalidate();
    }
}
