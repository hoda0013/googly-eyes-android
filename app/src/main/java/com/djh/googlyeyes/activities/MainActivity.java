package com.djh.googlyeyes.activities;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.djh.googlyeyes.fragments.CropFragment;
import com.djh.googlyeyes.fragments.EyeFragment;
import com.djh.googlyeyes.fragments.HomeFragment;
import com.djh.googlyeyes.fragments.ImageSourcePicker;
import com.djh.googlyeyes.fragments.PreviewFragment;
import com.djh.googlyeyes.widgets.GooglyEyeWidget;
import com.djh.googlyeyes.R;
import com.djh.googlyeyes.util.Util;
import com.djh.googlyeyes.widgets.Optometrist;
import com.djh.googlyeyes.widgets.TouchImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class MainActivity extends BaseActivity implements View.OnClickListener{

    public static final String KEY_CAMERA_IMAGE_URI = "com.djh.googlyeyes.activities.MainActivity.KEY_CAMERA_IMAGE_URI";

    private static final int SELECT_PICTURE = 1;
    private static final int SELECT_PICURE_KITKAT = 2;
    public static final int CAMERA_REQUEST_CODE = 4;
    public static final int CAMERA_RESULT_CODE = 100;



    private Uri imageUri;
    private String selectedImagePath;
    RelativeLayout mContainer;
//    TouchImageView mImageView;
//    RelativeLayout mImageFrame;
    TextView xVal;
    TextView zVal;

    private File f = null;
    private Context mContext;
    private Uri mCameraImageUri = null;

    private ImageSourcePicker.Listener imageSourcePickerListener = new ImageSourcePicker.Listener() {
        @Override
        public void onTakePhotoSelected() {
            takePhoto();
        }

        @Override
        public void onImageGallerySelected() {
            selectImageFromGallery();
        }
    };

    private HomeFragment.Listener homeFragmentListener = new HomeFragment.Listener() {
        @Override
        public void onAddImageClick() {
            //show ImageSourcePicker
            viewImageSourcePicker();
        }
    };

    private CropFragment.Listener cropFragmentListener = new CropFragment.Listener() {
        @Override
        public void onFinishedCropping(Uri uri) {
            viewEyeFragment(uri);
        }
    };

    private EyeFragment.Listener eyeFragmentListener = new EyeFragment.Listener() {
        @Override
        public void onNextClicked(Uri uri) {
            //show share fragment
            viewPreviewFragment(uri);
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        switch (item.getItemId()) {
            case android.R.id.home:

                if (getFragmentManager().getBackStackEntryCount() > 0) {
                    getFragmentManager().popBackStack();
                }
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContainer = (RelativeLayout) findViewById(R.id.container);
//        mImageView = (TouchImageView) findViewById(R.id.imageView);
//        mImageView.setImageResource(R.drawable.ben_bg);
//        mImageView.setFocusableInTouchMode(true);
//        mImageFrame = (RelativeLayout) findViewById(R.id.imageFrame);
//        xVal = (TextView) findViewById(R.id.xVal);
//        zVal = (TextView) findViewById(R.id.zVal);

        getActionBar().setTitle("GOOGLY EYES");
        getActionBar().setIcon(R.drawable.ic_actionbar_icon);
        mContext = this;

        if (savedInstanceState == null) {
            //The Activity is not being re-created
            viewHomeFragment();
        } else {
            if (savedInstanceState.containsKey(KEY_CAMERA_IMAGE_URI)) {
                String imageUri = savedInstanceState.getString(KEY_CAMERA_IMAGE_URI);
                if (TextUtils.isEmpty(imageUri)) {
                    mCameraImageUri = null;
                } else {
                    mCameraImageUri = Uri.parse(savedInstanceState.getString(KEY_CAMERA_IMAGE_URI));
                }
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mCameraImageUri != null) {
            outState.putString(KEY_CAMERA_IMAGE_URI, mCameraImageUri.toString());
        } else {
            outState.putString(KEY_CAMERA_IMAGE_URI, "");
        }
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
        if (fragment instanceof ImageSourcePicker) {
            if (imageSourcePickerListener != null) {
                ((ImageSourcePicker)fragment).setListener(imageSourcePickerListener);
            }
        } else if (fragment instanceof HomeFragment) {
            if (homeFragmentListener != null) {
                ((HomeFragment)fragment).setListener(homeFragmentListener);
            }
        } else if (fragment instanceof CropFragment) {
            if (cropFragmentListener != null) {
                ((CropFragment)fragment).setListener(cropFragmentListener);
            }
        } else if (fragment instanceof EyeFragment) {
            if (eyeFragmentListener != null) {
                ((EyeFragment)fragment).setListener(eyeFragmentListener);
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == CAMERA_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                if (mCameraImageUri != null) {
                    rotateImage(mCameraImageUri.getPath());
                    viewCropFragment(mCameraImageUri);
                }
            } else if(resultCode == RESULT_CANCELED){
                Log.i("IMAGE", "CANCELLED");
            }
        } else if (requestCode == SELECT_PICTURE){
            if (resultCode == RESULT_OK) {
                imageUri = data.getData();
                viewCropFragment(imageUri);

//                showPreviewImage(imageUri);
            } else if(resultCode == RESULT_CANCELED){
                Log.i("IMAGE", "CANCELLED" );
            }

        } else if(requestCode == SELECT_PICURE_KITKAT){
            if(resultCode == RESULT_OK) {
                imageUri = data.getData();
                makeImageUri(data, imageUri);

                viewCropFragment(imageUri);
//                showPreviewImage(imageUri);
            } else if(resultCode == RESULT_CANCELED){
                Log.i("IMAGE", "CANCELLED" );
            }
        }
    }

    private void viewPreviewFragment(Uri uri) {
        PreviewFragment frag = (PreviewFragment) getFragmentManager().findFragmentByTag(PreviewFragment.FRAG_TAG);

        if (frag == null) {
            frag = PreviewFragment.newInstance(uri);
        }

        replaceFragment(frag, frag.FRAG_TAG, mContainer.getId(), true);
    }

    private void viewCropFragment(Uri uri) {
        CropFragment frag = (CropFragment) getFragmentManager().findFragmentByTag(CropFragment.FRAG_TAG);

        if (frag == null) {
            frag = CropFragment.newInstance(uri);
        }

        replaceFragment(frag, frag.FRAG_TAG, mContainer.getId(), true);
    }

    private void viewEyeFragment(Uri uri) {
        EyeFragment frag = (EyeFragment) getFragmentManager().findFragmentByTag(EyeFragment.FRAG_TAG);

        if (frag == null) {
            frag = EyeFragment.newInstance(uri);
        }

        replaceFragment(frag, frag.FRAG_TAG, mContainer.getId(), true);
    }
//    private void saveImage() {
//
//        long startMillis = System.currentTimeMillis();
//        //Unfocus any eyes
//        Optometrist.INSTANCE.removeFocusFromAll();
//
//        //Create filename
//        String filename = Environment.getExternalStorageDirectory().toString() + "/" + getString(R.string.photo_directory) + "/"
//                + System.currentTimeMillis() + ".jpg";
//        f = new File(filename);
//        long endMillis = System.currentTimeMillis();
//
//        Log.e("FILE CREATION = ", Long.toString(startMillis - endMillis));
//        startMillis = endMillis;
//
//        //Find directory, create if doesn't exist
//        if (!f.getParentFile().exists()) {
//            f.getParentFile().mkdirs();
//        }
//        endMillis = System.currentTimeMillis();
//        Log.e("DIRECTORY CREATION = ", Long.toString(startMillis - endMillis));
//        startMillis = endMillis;
//
//        //Grab bitmap of image
//        mImageFrame.setDrawingCacheEnabled(true);
//        Bitmap bitmap = Bitmap.createBitmap(mImageFrame.getDrawingCache());
////        mImageView.setImageBitmap(bitmap);
//        mImageFrame.destroyDrawingCache();
//
//        endMillis = System.currentTimeMillis();
//        Log.e("BITMAP CREATION = ", Long.toString(startMillis - endMillis));
//        startMillis = endMillis;
//
//        //Save
//        FileOutputStream fOut = null;
//
//        try {
//            fOut = new FileOutputStream(f);
//            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fOut);
//            fOut.flush();
//            if (fOut != null) {
//                fOut.close();
//            }
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//            hideProgress();
//            Toast.makeText(this, getString(R.string.problem_saving), Toast.LENGTH_SHORT).show();
//        } catch (IOException e) {
//            e.printStackTrace();
//            hideProgress();
//            Toast.makeText(this, getString(R.string.problem_saving), Toast.LENGTH_SHORT).show();
//        }
//
//        endMillis = System.currentTimeMillis();
//        Log.e("FOUT CREATION = ", Long.toString(startMillis - endMillis));
//
//        startPreviewActivity(f);
//    }

    private void viewHomeFragment() {
        HomeFragment frag = (HomeFragment) getFragmentManager().findFragmentByTag(HomeFragment.FRAG_TAG);

        if (frag == null) {
            frag = new HomeFragment();
        }

        replaceFragment(frag, frag.FRAG_TAG, mContainer.getId(), false);
    }

    private void startPreviewActivity(File f) {
        Uri uri = Uri.fromFile(f);
        Intent intent = new Intent(this, PreviewActivity.class);
        if (uri != null) {
            intent.setData(uri);
        } else {
            Toast.makeText(this, getString(R.string.problem_saving), Toast.LENGTH_SHORT).show();
        }

        hideProgress();
        startActivity(intent);
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

    private void selectImageFromGallery() {
        if(Build.VERSION.SDK_INT < 19){
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);
        }else {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            startActivityForResult(intent, SELECT_PICURE_KITKAT);
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



    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void makeImageUri(Intent data, Uri imageUri){
        final int takeFlags = data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION) | Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
        getContentResolver().takePersistableUriPermission(imageUri, takeFlags);
    }

//    private void showPreviewImage(Uri imageUri){
//
//        selectedImagePath = Util.getPath(this, imageUri);
//
//        if (selectedImagePath!=null) {
//            Uri externalImages = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
//            String[] projection = {MediaStore.Images.Media._ID, MediaStore.Images.Media.ORIENTATION};
//            Cursor c = getContentResolver().query(externalImages, projection, MediaStore.Images.Media.DATA + " = ? ", new String[]{selectedImagePath}, null);
//
//            Bitmap bitmap = null;
//            if (c != null && c.moveToFirst()) {
//                int rotation = c.getInt(c.getColumnIndex(MediaStore.Images.Media.ORIENTATION));
//                try {
//                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                if (bitmap != null && rotation != 0) {
//                    Matrix matrix = new Matrix();
//                    matrix.postRotate(rotation);
//
//                    int w = bitmap.getWidth();
//                    int h = bitmap.getHeight();
//
//                    Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);
//                    bitmap.recycle();
//                    bitmap = newBitmap;
//                }
//            }
//
//            if (bitmap != null) {
//                mImageView.setImageBitmap(bitmap);
////                removeAllEyes();
//            }
//        }
//    }

//    private void showCameraPreviewImage() {
//        if (mCameraImageUri == null) {
//            // we lost something, so just fail
//            return;
//        }
//
//        final String path = mCameraImageUri.getPath();
//        Bitmap bitmap = BitmapFactory.decodeFile(path);
//        mImageView.setImageBitmap(bitmap);
////        removeAllEyes();
//    }

//    private void removeAllEyes() {
//        for (GooglyEyeWidget eye: listGooglyEyes) {
//            mImageFrame.removeView(eye);
//        }
//        listGooglyEyes.clear();
//    }

    float tempX = 0.0f;
    float tempY = 0.0f;

    @Override
    public void onClick(View v) {

    }

    private void viewImageSourcePicker() {
        ImageSourcePicker frag = (ImageSourcePicker)getFragmentManager().findFragmentByTag(ImageSourcePicker.FRAG_TAG);
        if (frag == null) {
            frag = ImageSourcePicker.newInstance();
            frag.show(getFragmentManager(), ImageSourcePicker.FRAG_TAG);
        }
    }

    private void addEye() {
        //create new eye and set listeners
        GooglyEyeWidget eye = Optometrist.INSTANCE.makeEye(this, new GooglyEyeWidget.Listener() {
            @Override
            public void removeView(GooglyEyeWidget eye) {
//                mImageFrame.removeView(eye);
            }

            @Override
            public void updateVals(double x, double z) {
                xVal.setText("X = " + Double.toString(x));
                zVal.setText("Z = " + Double.toString(z));

            }
        });
//        mImageFrame.addView(eye);
    }

    ProgressDialog pd;

    public void showProgress(String message) {
        if (pd == null) {
            pd = new ProgressDialog(this);
            pd.setMessage(message);
            pd.setCancelable(false);
            pd.setIndeterminate(true);
            pd.show();
        }
    }

    public void hideProgress() {
        if (pd != null) {
            try {
                pd.dismiss();
            } catch (IllegalArgumentException e) {
                //FIXME: I think this works but is bad practice
                //Got the idea here: http://stackoverflow.com/questions/2745061/java-lang-illegalargumentexception-view-not-attached-to-window-manager
                //After getting an error from this line
            } finally {
                pd = null;
            }
        }
    }
}
