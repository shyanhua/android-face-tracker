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

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.Image;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.BitSet;


/**
 * Activity for the face tracker app.  This app detects faces with the rear facing camera, and draws
 * overlay graphics to indicate the position, size, and ID of each face.
 */
public final class RealTimeSelfieCaptureActivity extends AppCompatActivity implements SSFaceScannerViewDelegate
{
    private SSRealTimeSelfieCaptureView ssRealTimeSelfieCaptureView;

    //==============================================================================================
    // Activity Cycles
    //==============================================================================================

    /**
     * Initializes the UI and initiates the creation of a face detector.
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activityfacescan);

        initFaceScanner();
        ssRealTimeSelfieCaptureView.cameraPermission();

    }

    /**
     * Restarts the camera.
     */
    @Override
    protected void onResume()
    {
        super.onResume();
        ssRealTimeSelfieCaptureView.startCameraSource();
    }

    /**
     * Stops the camera.
     */
    @Override
    protected void onPause()
    {
        super.onPause();
        ssRealTimeSelfieCaptureView.stop();
    }

    /**
     * Releases the resources associated with the camera source, the associated detector, and the
     * rest of the processing pipeline.
     */
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        ssRealTimeSelfieCaptureView.release();
    }

    /**
     * Callback for the result from requesting permissions. This method
     * is invoked for every call on {@link #requestPermissions(String[], int)}.
     * <p>
     * <strong>Note:</strong> It is possible that the permissions request interaction
     * with the user is interrupted. In this case you will receive empty permissions
     * and results arrays which should be treated as a cancellation.
     * </p>
     *
     * @param requestCode  The request code passed in {@link #requestPermissions(String[], int)}.
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either {@link PackageManager#PERMISSION_GRANTED}
     *                     or {@link PackageManager#PERMISSION_DENIED}. Never null.
     * @see #requestPermissions(String[], int)
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        //Permission granted
        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
        {
            Log.d(ssRealTimeSelfieCaptureView.TAG, "Camera permission granted - initialize the camera source");

            // we have permission, so create the cameraSource
                ssRealTimeSelfieCaptureView.createCameraSource();
                return;
        }

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id)
            {
                finish();
            }
        };

        //Exit application if deny camera permission
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Face Tracker sample")
                .setMessage(R.string.no_camera_permission)
                .setPositiveButton(R.string.ok, listener)
                .show();
    }

    //==============================================================================================
    // Button Action
    //==============================================================================================


    //==============================================================================================
    // Private Methods
    //==============================================================================================

    private void initFaceScanner()
    {
        ssRealTimeSelfieCaptureView = (SSRealTimeSelfieCaptureView) findViewById(R.id.face_scanner);
        ssRealTimeSelfieCaptureView.setDelegate(this, this);
    }

    //==============================================================================================
    // SSFaceScannerViewDelegate
    //==============================================================================================

    @Override
    public void captureCompletedForSelfie(Bitmap bitmap)
    {
        //Retrieve selfie image
        Log.d("Test","Successful");
    }
}
