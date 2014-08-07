package com.djh.googlyeyes;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.widget.AbsoluteLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class MainActivity extends Activity implements View.OnClickListener{

    public static final int CAMERA_REQUEST_CODE = 4;
    public static final int CAMERA_RESULT_CODE = 100;
    private static final int SWIPE_MIN_DISTANCE = 90;
    private static final int SWIPE_MAX_OFF_PATH = 100;
    private static final int SWIPE_THRESHOLD_VELOCITY = 2000;

    RelativeLayout mContainer;
    ImageView mImageView;
    private Context mContext;
    private int eyeCounter = 0;
    private GooglyEyeWidget theEye = null;
    private Uri mCameraImageUri = null;

    private List<GooglyEyeWidget> listGooglyEyes = new ArrayList<GooglyEyeWidget>();

    private GestureDetector gestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContainer = (RelativeLayout) findViewById(R.id.container);
        mImageView = (ImageView) findViewById(R.id.imageView);
        mImageView.setImageResource(R.drawable.ben_bg);
        mContext = this;
        gestureDetector = new GestureDetector(this, new MyGestureDetector());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.plus) {
            //If there's an eye in focus, change it to PLACED
            if (theEye != null) {
                theEye.setMode(GooglyEyeWidget.Mode.PLACED);
            }
            //create new eye and set listeners
            theEye = new GooglyEyeWidget(this);
            theEye.setId(eyeCounter++);
            theEye.setOnClickListener(this);
            theEye.setOnTouchListener(gestureListener);
            listGooglyEyes.add(theEye);
            mContainer.addView(theEye);
            for (int i = 0; i < listGooglyEyes.size() - 1; i++) {
                listGooglyEyes.get(i).setMode(GooglyEyeWidget.Mode.PLACED);
            }
            theEye.setMode(GooglyEyeWidget.Mode.EDITING);
            return true;
        } else if (id == R.id.take_photo) {
            takePhoto();
        } else if (id == R.id.add_image) {

        }
        return super.onOptionsItemSelected(item);
    }

    private void takePhoto() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if(android.os.Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            //There is an external storage space

            try {
                File file = createImageFile();
                mCameraImageUri = Uri.fromFile(file);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, mCameraImageUri);
                startActivityForResult(intent, CAMERA_REQUEST_CODE);
            } catch (IOException ioe) {
                Toast.makeText(mContext, "Error creating file for camera.", Toast.LENGTH_SHORT).show();
            }

        }else{
            Toast.makeText(mContext, "External storage not detected", Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        return File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
    }

    private void rotateImage(final String path) {
        int rotation = 0;
        //Check the exif data of the image to see if it indicates that the image has been rotated
        try {
            ExifInterface exif = new ExifInterface(path);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            switch (orientation) {
                case ExifInterface.ORIENTATION_NORMAL:
                    rotation = 0;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotation = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotation = -90;
                    break;

                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotation = 90;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        //If the exif data showed the image has been rotated then rotated the image back to 'normal' and re-save it
        if (rotation != 0) {
            rotateImage(rotation, path, 1);
        }
    }

    private static void rotateImage(final int rotation, final String path, final int inSampleSize) {
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inSampleSize = inSampleSize;
        bmOptions.inPurgeable = true;

        Matrix matrix = new Matrix();
        matrix.postRotate(rotation);

        Bitmap bitmap = null;
        Bitmap newBitmap = null;
        try {
            bitmap = BitmapFactory.decodeFile(path, bmOptions);

            int w = bitmap.getWidth();
            int h = bitmap.getHeight();

            try {
                newBitmap = Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);
                FileOutputStream fOut = new FileOutputStream(path);
                newBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
            } catch (FileNotFoundException e) {

            }
        } catch(OutOfMemoryError e) {
            if (bitmap != null) {
                bitmap.recycle();
            }
            if (newBitmap != null) {
                newBitmap.recycle();
            }

            if (inSampleSize <= 8) {
                rotateImage(rotation, path, inSampleSize * 2);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == CAMERA_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                if (mCameraImageUri != null) {
                    rotateImage(mCameraImageUri.getPath());
                }
                showCameraPreviewImage();
            } else if(resultCode == RESULT_CANCELED){
                Log.i("IMAGE", "CANCELLED");
            }
        }
    }

    private static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    private void showCameraPreviewImage() {
        if (mCameraImageUri == null) {
            // we lost something, so just fail
            return;
        }

        final String path = mCameraImageUri.getPath();
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        mImageView.setImageBitmap(bitmap);
        mImageView.setScaleType(ImageView.ScaleType.FIT_XY);
    }

    float tempX = 0.0f;
    float tempY = 0.0f;

    @Override
    public void onClick(View v) {
    }

    public interface Listener {
        public void animationFinished();
    }

    View.OnTouchListener gestureListener = new View.OnTouchListener() {
        public boolean onTouch(View v, MotionEvent event) {

            float eventX = event.getX();
            float eventY = event.getY();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    tempX = eventX;
                    tempY = eventY;

                    Log.e("XTOUCH", String.valueOf(eventX));
                    Log.e("YTOUCH", String.valueOf(eventY));

                    for (GooglyEyeWidget eye : listGooglyEyes) {
                        if (eye.isTouchingUpperLeft(eventX, eventY)) {
                            theEye = eye;
                            theEye.setMode(GooglyEyeWidget.Mode.RESIZING_UPPER_LEFT);
                            clearOtherEyes(theEye.getId());
                        } else if (eye.isTouchingUpperRight(eventX, eventY)) {
                            theEye = eye;
                            theEye.setMode(GooglyEyeWidget.Mode.RESIZING_UPPER_RIGHT);
                            clearOtherEyes(theEye.getId());
                        } else if(eye.isTouchingLowerLeft(eventX, eventY)) {
                            theEye = eye;
                            theEye.setMode(GooglyEyeWidget.Mode.RESIZING_LOWER_LEFT);
                            clearOtherEyes(theEye.getId());
                        } else if(eye.isTouchingLowerRight(eventX, eventY)) {
                            theEye = eye;
                            theEye.setMode(GooglyEyeWidget.Mode.RESIZING_LOWER_RIGHT);
                            clearOtherEyes(theEye.getId());
                        } else if(eye.isTouchingBoundingBox(eventX, eventY)){
                            theEye = eye;
                            theEye.setMode(GooglyEyeWidget.Mode.DRAGGING);
                            clearOtherEyes(theEye.getId());
                        } else {
                            //touch event did not hit the eye
                            eye.setMode(GooglyEyeWidget.Mode.PLACED);
                            if (theEye != null) {
                                if (eye.getId() == theEye.getId()) {
                                    theEye = null;
                                }
                            }
                        }
                    }

                    break;

                case MotionEvent.ACTION_MOVE:
                    if (theEye != null && theEye.getMode() == GooglyEyeWidget.Mode.DRAGGING) {
                        int deltaX = (int) (eventX - tempX);
                        int deltaY = (int) (eventY - tempY);

                        theEye.setDraggingCoords(deltaX, deltaY);

                        tempX = eventX;
                        tempY = eventY;
                    } else if (theEye != null && theEye.getMode() == GooglyEyeWidget.Mode.RESIZING_UPPER_LEFT) {
                        int deltaX = (int) (eventX - tempX);
                        int deltaY = (int) (eventY - tempY);
                        int delta = 0;

                        if (Math.abs(deltaX) > Math.abs(deltaY)) {
                            delta = deltaY;
                        } else {
                            delta = deltaX;
                        }

                        theEye.resizeUpperLeft(delta);
                        tempX = eventX;
                        tempY = eventY;
                    } else if (theEye != null && theEye.getMode() == GooglyEyeWidget.Mode.RESIZING_UPPER_RIGHT) {
                        int deltaX = (int) (eventX - tempX);
                        int deltaY = (int) (eventY + tempY);
                        int delta = 0;

                        if (Math.abs(deltaX) > Math.abs(deltaY)) {
                            delta = deltaY;
                        } else {
                            delta = deltaX;
                        }

                        theEye.resizeUpperRight(delta);
                        tempX = eventX;
                        tempY = eventY;
                    } else if (theEye != null && theEye.getMode() == GooglyEyeWidget.Mode.RESIZING_LOWER_LEFT) {
                        int deltaX = (int) (eventX - tempX);
                        int deltaY = (int) (eventY + tempY);
                        int delta = 0;

                        if (Math.abs(deltaX) > Math.abs(deltaY)) {
                            delta = deltaY;
                        } else {
                            delta = deltaX;
                        }
                        theEye.resizeLowerLeft(delta);
                        tempX = eventX;
                        tempY = eventY;
                    } else if (theEye != null && theEye.getMode() == GooglyEyeWidget.Mode.RESIZING_LOWER_RIGHT) {
                        int deltaX = (int) (eventX - tempX);
                        int deltaY = (int) (eventY - tempY);
                        int delta = 0;

                        if (Math.abs(deltaX) > Math.abs(deltaY)) {
                            delta = deltaY;
                        } else {
                            delta = deltaX;
                        }
                        theEye.resizeLowerRight(delta);
                        tempX = eventX;
                        tempY = eventY;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (theEye != null) {
                        theEye.setMode(GooglyEyeWidget.Mode.EDITING);
                    }
                    break;
            }
            return gestureDetector.onTouchEvent(event);
        }
    };

    class MyGestureDetector extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            try {
                if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
                    return false;
                // right to left swipe
                if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    Toast.makeText(mContext, getString(R.string.eye_deleted), Toast.LENGTH_SHORT).show();
                    theEye.flingOffScreen(true, new Listener() {
                        @Override
                        public void animationFinished() {
                            mContainer.removeView(theEye);
                            listGooglyEyes.remove(theEye);
                        }
                    });

                } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    Toast.makeText(mContext, getString(R.string.eye_deleted), Toast.LENGTH_SHORT).show();
                    theEye.flingOffScreen(false, new Listener() {
                        @Override
                        public void animationFinished() {
                            mContainer.removeView(theEye);
                            listGooglyEyes.remove(theEye);
                        }
                    });
                }
            } catch (Exception e) {

            }
            return false;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }
    }

    private void clearOtherEyes(int id) {
        for (GooglyEyeWidget eye : listGooglyEyes) {
            if (eye.getId() != id) {
                eye.setMode(GooglyEyeWidget.Mode.PLACED);
            }
        }
    }
}
