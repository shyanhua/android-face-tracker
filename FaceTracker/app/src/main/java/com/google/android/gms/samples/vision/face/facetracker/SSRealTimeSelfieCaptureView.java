package com.google.android.gms.samples.vision.face.facetracker;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.samples.vision.face.facetracker.ui.camera.CameraSourcePreview;
import com.google.android.gms.samples.vision.face.facetracker.ui.camera.GraphicOverlay;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;

public class SSRealTimeSelfieCaptureView extends FrameLayout implements FaceGraphicTrackerDelegate
{
    private Context context;
    private Activity activity;

    private CameraSource mCameraSource = null;
    private GraphicOverlay mGraphicOverlay;
    private CameraSourcePreview mPreview;

    //Static variables
    private static final int RC_HANDLE_GMS = 9001;
    private static final int RC_HANDLE_CAMERA_PERM = 2;
    public static final String TAG = "FaceTracker";

    //Scanner logic
    private Boolean flagContinue = false, flagFaceRight = false, flagFaceLeft = false, flagFaceId = false, flagSmile = false, flagCapture = false;
    private Integer faceId;
    private File imageFile, dir;
    private float rotation = 0;

    // ============================================================================================
    // SSFaceScannerViewDelegate
    // ============================================================================================
    private SSFaceScannerViewDelegate ssFaceScannerViewDelegate = null;

    public void setDelegate(SSFaceScannerViewDelegate delegate, Activity activity)
    {
        this.ssFaceScannerViewDelegate = delegate;
        this.activity = activity;
    }

    //==============================================================================================
    // Public Methods - Camera Setup
    //==============================================================================================

    /**
     * Starts or restarts the camera source, if it exists.  If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    public void startCameraSource()
    {
        // check that the device has play services available.
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context);
        if (code != ConnectionResult.SUCCESS)
        {
            Dialog dlg =
                    GoogleApiAvailability.getInstance().getErrorDialog(activity, code, RC_HANDLE_GMS);
            dlg.show();
        }

        if (mCameraSource != null)
        {
            try
            {
                mPreview.start(mCameraSource, mGraphicOverlay);
            }
            catch (IOException e)
            {
                Log.e(TAG, "Unable to start camera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }

    /**
     * Creates and starts the camera.  Note that this uses a higher resolution in comparison
     * to other detection examples to enable the barcode detector to detect small barcodes
     * at long distances.
     */
    public void createCameraSource()
    {
        FaceDetector detector = new FaceDetector.Builder(context)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .setMode(FaceDetector.ACCURATE_MODE)
                .build();

        GraphicFaceTrackerFactory graphicFaceTrackerFactory = new GraphicFaceTrackerFactory(mGraphicOverlay,this);

        detector.setProcessor(new MultiProcessor.Builder<>(graphicFaceTrackerFactory).build());

        if (!detector.isOperational())
        {
            // Note: The first time that an app using face API is installed on a device, GMS will
            // download a native library to the device in order to do detection.  Usually this
            // completes before the app is run for the first time.  But if that download has not yet
            // completed, then the above call will not detect any faces.
            //
            // isOperational() can be used to check if the required native library is currently
            // available.  The detector will automatically become operational once the library
            // download completes on device.
            Log.w(TAG, "Face detector dependencies are not yet available.");
        }

        mCameraSource = new CameraSource.Builder(context, detector)
                .setRequestedPreviewSize(640, 480)
                .setFacing(CameraSource.CAMERA_FACING_FRONT)
                .setRequestedFps(30.0f)
                .build();
    }

    public void cameraPermission()
    {
        // Check for the camera permission before accessing the camera. If the
        // permission is not granted yet, request permission.
        int requestCamera = ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA);
        if (requestCamera == PackageManager.PERMISSION_GRANTED)
        {
            createCameraSource();
        }
        else
        {
            requestCameraPermission();
        }
    }

    public void stop()
    {
        mPreview.stop();
    }

    public void release()
    {
        if (mCameraSource != null)
        {
            mCameraSource.release();
        }
    }

    //==============================================================================================
    // Private Methods
    //==============================================================================================

    private void initData()
    {
        //Load xml
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View view = layoutInflater.inflate(R.layout.viewfacescanner, this);

        mPreview = (CameraSourcePreview) view.findViewById(R.id.view_ssface_scanner_preview);
        mGraphicOverlay = (GraphicOverlay) view.findViewById(R.id.view_ssface_scanner_faceOverlay);
    }

    /**
     * Handles the requesting of the camera permission.  This includes
     * showing a "Snackbar" message of why the permission is needed then
     * sending the request.
     */
    private void requestCameraPermission()
    {
        Log.w(TAG, "Camera permission is not granted. Requesting permission");

        final String[] permissions = new String[]{Manifest.permission.CAMERA};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(activity,
                Manifest.permission.CAMERA))
        {
            ActivityCompat.requestPermissions(activity, permissions, RC_HANDLE_CAMERA_PERM);
            return;
        }

        final Activity thisActivity = activity;

        View.OnClickListener listener = new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                ActivityCompat.requestPermissions(thisActivity, permissions,
                        RC_HANDLE_CAMERA_PERM);
            }
        };

        Snackbar.make(mGraphicOverlay, R.string.permission_camera_rationale,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.ok, listener)
                .show();
    }

    private void resetFlag()
    {
        flagSmile = false;
        flagContinue = false;
        flagFaceLeft = false;
        flagFaceRight = false;
        flagFaceId = false;
    }

    //==============================================================================================
    // SSRealTimeSelfieCaptureView Constructors
    //==============================================================================================

    /** Init when enter from xml */
    public SSRealTimeSelfieCaptureView(Context context)
    {
        super(context);
        this.context = context;
        initData();
    }

    /** Init when enter from xml */
    public SSRealTimeSelfieCaptureView(Context context, AttributeSet attrs)
    {
        super(context,attrs);
        this.context = context;
        initData();
    }

    /** Init when enter from xml */
    public SSRealTimeSelfieCaptureView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initData();
    }

    //==============================================================================================
    // FaceGraphicTrackerDelegate
    //==============================================================================================
    @Override
    public void faceGraphicTrackerNewFaceDetected(Face face, Integer id)
    {
        if (face == null)
        {
            return;
        }

        float faceY = face.getEulerY();

        //Get Face ID
        if (flagFaceId == false)
        {
            faceId = id;
            flagFaceId = true;
        }

        //If face id changed then reset flag
        if (flagFaceId == true && id > faceId)
        {
            resetFlag();
        }

        //Detect front face
        if (flagContinue == false)
        {
            if (faceY > -12 && faceY < 12)
            {
                Log.d("Face","Front face detected");
                flagContinue = true;
                return;
            }
        }

        //After front face , detect left face then right face
        if (flagContinue == true)
        {
            //Left face
            if (flagFaceLeft == false)
            {
                if (faceY < -30)
                {
                    Log.d("Face","Left face detected");
                    flagFaceLeft = true;
                    return;
                }
            }
            //Right face
            if (flagFaceLeft == true)
            {
                if (flagFaceRight == false)
                {
                    if (faceY > 30)
                    {
                        Log.d("Face","Right face detected");
                        flagFaceRight = true;
                        return;
                    }
                }
            }
        }

        //Detect smile after left face and right face detected
        if (flagFaceLeft == true && flagFaceRight == true)
        {
            if (flagSmile == false)
            {
                if (face.getIsSmilingProbability() > 0.6)
                {
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                    {
                        Log.d("Face","Please enable storage");
                        return;
                    }
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                    {
                        Log.d("Face","Captured !");
                    }
                    flagSmile = true;
                    return;
                }
            }
        }

        //Capture smile face save into gallery
        if(flagSmile == true && flagCapture == false)
        {
            mCameraSource.takePicture(null, new CameraSource.PictureCallback()
            {
                @Override
                public void onPictureTaken(byte[] bytes)
                {
                    try
                    {
                        // convert byte array into bitmap
                        Bitmap loadedImage;
                        Bitmap rotatedBitmap;
                        loadedImage = BitmapFactory.decodeByteArray(bytes, 0,
                                bytes.length);

                        Matrix rotateMatrix = new Matrix();

                        rotateMatrix.postRotate(rotation);
                        rotatedBitmap = Bitmap.createBitmap(loadedImage, 0, 0,
                                loadedImage.getWidth(), loadedImage.getHeight(),
                                rotateMatrix, false);

                        dir = new File(
                                Environment.getExternalStorageDirectory(), "/MyPhotos/");

                        boolean success = true;
                        if (!dir.exists())
                        {
                            success = dir.mkdirs();
                        }
                        if (success)
                        {
                            Date date = new Date();
                            imageFile = new File(dir.getAbsolutePath()
                                    + File.separator
                                    + new Timestamp(date.getTime()).toString()
                                    + "Image.jpg");

                            imageFile.createNewFile();
                        }
                        else
                        {
                            //Image not saved.
                            return;
                        }
                        ByteArrayOutputStream ostream = new ByteArrayOutputStream();

                        // save image into gallery
                        rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, ostream);

                        FileOutputStream fout = new FileOutputStream(imageFile);
                        fout.write(ostream.toByteArray());
                        fout.close();
                        ContentValues values = new ContentValues();

                        values.put(MediaStore.Images.Media.DATE_TAKEN,
                                System.currentTimeMillis());
                        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                        values.put(MediaStore.MediaColumns.DATA,
                                imageFile.getAbsolutePath());

                        activity.getContentResolver().insert(
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

                        //Delegate image to activity
                        ssFaceScannerViewDelegate.captureCompletedForSelfie(loadedImage);

                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            });
            flagCapture = true;
            return;
        }
    }

    //==============================================================================================
    // Graphic Face Tracker
    //==============================================================================================

    /**
     * Factory for creating a face tracker to be associated with a new face.  The multiprocessor
     * uses this factory to create face trackers as needed -- one for each individual.
     */
    private class GraphicFaceTrackerFactory implements MultiProcessor.Factory<Face>
    {
        private FaceGraphicTrackerDelegate faceGraphicTrackerDelegate;

        public GraphicFaceTrackerFactory(GraphicOverlay<FaceGraphic> barcodeGraphicOverlay, FaceGraphicTrackerDelegate delegate)
        {
            mGraphicOverlay = barcodeGraphicOverlay;
            this.faceGraphicTrackerDelegate = delegate;
        }

        @Override
        public Tracker<Face> create(Face face)
        {
            FaceGraphic faceGraphic = new FaceGraphic(mGraphicOverlay);
            GraphicFaceTracker graphicFaceTracker = new GraphicFaceTracker(mGraphicOverlay, faceGraphic);

            if (faceGraphicTrackerDelegate != null)
            {
                graphicFaceTracker.setDelegate(faceGraphicTrackerDelegate);
            }
            return graphicFaceTracker;
        }
    }

    /**
     * Face tracker for each detected individual. This maintains a face graphic within the app's
     * associated face overlay.
     */
    private class GraphicFaceTracker extends Tracker<Face>
    {
        private GraphicOverlay<FaceGraphic> mOverlay;
        private FaceGraphic mFaceGraphic;
        private FaceGraphicTrackerDelegate faceGraphicTrackerDelegate;

        public void setDelegate(FaceGraphicTrackerDelegate delegate)
        {
            this.faceGraphicTrackerDelegate = delegate;
        }

        GraphicFaceTracker(GraphicOverlay overlay, FaceGraphic faceGraphic)
        {
            mOverlay = overlay;
            mFaceGraphic = faceGraphic;
        }

        /**
         * Start tracking the detected face instance within the face overlay.
         */
        @Override
        public void onNewItem(int faceId, Face item)
        {
            mFaceGraphic.setId(faceId);
        }

        /**
         * Update the position/characteristics of the face within the overlay.
         */
        @Override
        public void onUpdate(FaceDetector.Detections<Face> detectionResults, Face face)
        {
            mOverlay.add(mFaceGraphic);
            mFaceGraphic.updateFace(face);

            if(faceGraphicTrackerDelegate != null)
            {
                faceGraphicTrackerDelegate.faceGraphicTrackerNewFaceDetected(face, mFaceGraphic.getId());
            }
        }

        /**
         * Hide the graphic when the corresponding face was not detected.  This can happen for
         * intermediate frames temporarily (e.g., if the face was momentarily blocked from
         * view).
         */
        @Override
        public void onMissing(FaceDetector.Detections<Face> detectionResults)
        {
            mOverlay.remove(mFaceGraphic);
        }

        /**
         * Called when the face is assumed to be gone for good. Remove the graphic annotation from
         * the overlay.
         */
        @Override
        public void onDone()
        {
            mOverlay.remove(mFaceGraphic);
        }

    }
}
