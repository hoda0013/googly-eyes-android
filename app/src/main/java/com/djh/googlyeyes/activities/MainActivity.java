package com.djh.googlyeyes.activities;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.djh.googlyeyes.fragments.CropFragment;
import com.djh.googlyeyes.fragments.EyeFragment;
import com.djh.googlyeyes.fragments.HomeFragment;
import com.djh.googlyeyes.fragments.ShareFragment;
import com.djh.googlyeyes.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MainActivity extends BaseActivity implements View.OnClickListener{

    public static final String KEY_CAMERA_IMAGE_URI = "com.djh.googlyeyes.activities.MainActivity.KEY_CAMERA_IMAGE_URI";

    private static final int SELECT_PICTURE = 1;
    private static final int SELECT_PICURE_KITKAT = 2;
    public static final int CAMERA_REQUEST_CODE = 4;

    private Uri imageUri;
    RelativeLayout mContainer;

    private File f = null;
    private Context mContext;
    private Uri mCameraImageUri = null;

    private HomeFragment.Listener homeFragmentListener = new HomeFragment.Listener() {

        @Override
        public void onCameraClicked() {
            takePhoto();
        }

        @Override
        public void onGalleryClicked() {
            selectImageFromGallery();
        }

        @Override
        public void onViewerClicked() {

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

    private ShareFragment.Listener shareFragmentListener = new ShareFragment.Listener() {
        @Override
        public void onDoneClicked() {
            while (getFragmentManager().getBackStackEntryCount() >= 1) {
                getFragmentManager().popBackStackImmediate();
            }
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
        if (fragment instanceof HomeFragment) {
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
        } else if (fragment instanceof ShareFragment) {
            if (shareFragmentListener != null) {
                ((ShareFragment)fragment).setListener(shareFragmentListener);
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
            } else if(resultCode == RESULT_CANCELED){
                Log.i("IMAGE", "CANCELLED" );
            }

        } else if(requestCode == SELECT_PICURE_KITKAT){
            if(resultCode == RESULT_OK) {
                imageUri = data.getData();
                makeImageUri(data, imageUri);
                viewCropFragment(imageUri);
            } else if(resultCode == RESULT_CANCELED){
                Log.i("IMAGE", "CANCELLED" );
            }
        }
    }

    private void viewPreviewFragment(Uri uri) {
        ShareFragment frag = (ShareFragment) getFragmentManager().findFragmentByTag(ShareFragment.FRAG_TAG);

        if (frag == null) {
            frag = ShareFragment.newInstance(uri);
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

    private void viewHomeFragment() {
        HomeFragment frag = (HomeFragment) getFragmentManager().findFragmentByTag(HomeFragment.FRAG_TAG);

        if (frag == null) {
            frag = new HomeFragment();
        }

        replaceFragment(frag, frag.FRAG_TAG, mContainer.getId(), false);
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

    @Override
    public void onClick(View v) {

    }
}
