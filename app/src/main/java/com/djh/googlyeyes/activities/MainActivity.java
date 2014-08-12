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
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
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

import com.djh.googlyeyes.fragments.ImageSourcePicker;
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


public class MainActivity extends Activity implements View.OnClickListener{

    private static final int SELECT_PICTURE = 1;
    private static final int SELECT_PICURE_KITKAT = 2;
    public static final int CAMERA_REQUEST_CODE = 4;


    private Uri imageUri;
    private String selectedImagePath;
    RelativeLayout mContainer;
    TouchImageView mImageView;
    RelativeLayout mImageFrame;
    TextView mAddImageLabel;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContainer = (RelativeLayout) findViewById(R.id.container);
        mImageView = (TouchImageView) findViewById(R.id.imageView);
        mImageView.setFocusableInTouchMode(true);
        mImageFrame = (RelativeLayout) findViewById(R.id.imageFrame);
        mAddImageLabel = (TextView) findViewById(R.id.addImageLabel);
        mAddImageLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewImageSourcePicker();
            }
        });
        getActionBar().setTitle("GOOGLY EYES");
        getActionBar().setIcon(R.drawable.ic_actionbar_icon);
        mContext = this;

        if (savedInstanceState != null) {
            //redraw eyes here if device was rotated / backgrounded
        }
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
        if (fragment instanceof ImageSourcePicker) {
            if (imageSourcePickerListener != null) {
                ((ImageSourcePicker)fragment).setListener(imageSourcePickerListener);
            }
        }
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

        if (item.getItemId() == R.id.plus) {
            addEye();
        } else if (item.getItemId() == R.id.add_image) {
            viewImageSourcePicker();
        } else if (item.getItemId() == R.id.save) {
            saveImage();
        }

        return super.onOptionsItemSelected(item);
    }

    private void saveImage() {

        long startMillis = System.currentTimeMillis();
        //Unfocus any eyes
        Optometrist.INSTANCE.removeFocusFromAll();

        //Create filename
        String filename = Environment.getExternalStorageDirectory().toString() + "/" + getString(R.string.photo_directory) + "/"
                + System.currentTimeMillis() + ".jpg";
        f = new File(filename);
        long endMillis = System.currentTimeMillis();

        Log.e("FILE CREATION = ", Long.toString(startMillis - endMillis));
        startMillis = endMillis;

        //Find directory, create if doesn't exist
        if (!f.getParentFile().exists()) {
            f.getParentFile().mkdirs();
        }
        endMillis = System.currentTimeMillis();
        Log.e("DIRECTORY CREATION = ", Long.toString(startMillis - endMillis));
        startMillis = endMillis;

        //Grab bitmap of image
        mImageFrame.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(mImageFrame.getDrawingCache());
//        mImageView.setImageBitmap(bitmap);
        mImageFrame.destroyDrawingCache();

        endMillis = System.currentTimeMillis();
        Log.e("BITMAP CREATION = ", Long.toString(startMillis - endMillis));
        startMillis = endMillis;

        //Save
        FileOutputStream fOut = null;

        try {
            fOut = new FileOutputStream(f);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fOut);
            fOut.flush();
            if (fOut != null) {
                fOut.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(this, getString(R.string.problem_saving), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, getString(R.string.problem_saving), Toast.LENGTH_SHORT).show();
        }

        endMillis = System.currentTimeMillis();
        Log.e("FOUT CREATION = ", Long.toString(startMillis - endMillis));

        startPreviewActivity(f);
    }

    private void startPreviewActivity(File f) {
        Uri uri = Uri.fromFile(f);
        Intent intent = new Intent(this, PreviewActivity.class);
        if (uri != null) {
            intent.setData(uri);
        } else {
            Toast.makeText(this, getString(R.string.problem_saving), Toast.LENGTH_SHORT).show();
        }

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
        } else if (requestCode == SELECT_PICTURE){
            if (resultCode == RESULT_OK) {
                imageUri = data.getData();
                showPreviewImage(imageUri);
            } else if(resultCode == RESULT_CANCELED){
                Log.i("IMAGE", "CANCELLED" );
            }

        } else if(requestCode == SELECT_PICURE_KITKAT){
            if(resultCode == RESULT_OK) {
                imageUri = data.getData();
                makeImageUri(data, imageUri);
                showPreviewImage(imageUri);
            } else if(resultCode == RESULT_CANCELED){
                Log.i("IMAGE", "CANCELLED" );
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void makeImageUri(Intent data, Uri imageUri){
        final int takeFlags = data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION) | Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
        getContentResolver().takePersistableUriPermission(imageUri, takeFlags);
    }

    private void showPreviewImage(Uri imageUri){

        selectedImagePath = Util.getPath(this, imageUri);

        if (selectedImagePath!=null) {
            Uri externalImages = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            String[] projection = {MediaStore.Images.Media._ID, MediaStore.Images.Media.ORIENTATION};
            Cursor c = getContentResolver().query(externalImages, projection, MediaStore.Images.Media.DATA + " = ? ", new String[]{selectedImagePath}, null);

            Bitmap bitmap = null;
            if (c != null && c.moveToFirst()) {
                int rotation = c.getInt(c.getColumnIndex(MediaStore.Images.Media.ORIENTATION));
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (bitmap != null && rotation != 0) {
                    Matrix matrix = new Matrix();
                    matrix.postRotate(rotation);

                    int w = bitmap.getWidth();
                    int h = bitmap.getHeight();

                    Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);
                    bitmap.recycle();
                    bitmap = newBitmap;
                }
            }

            if (bitmap != null) {
                mImageView.resetZoom();
                mImageView.setImageBitmap(bitmap);
                mAddImageLabel.setVisibility(View.GONE);
//                removeAllEyes();
            }
        }
    }

    private void showCameraPreviewImage() {
        if (mCameraImageUri == null) {
            // we lost something, so just fail
            return;
        }

        final String path = mCameraImageUri.getPath();
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        mImageView.setImageBitmap(bitmap);
        mAddImageLabel.setVisibility(View.GONE);
//        removeAllEyes();
    }

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
                mImageFrame.removeView(eye);
            }
        });
        mImageFrame.addView(eye);
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
