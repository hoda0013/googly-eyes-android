package com.djh.googlyeyes.activities;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.djh.googlyeyes.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by dillonhodapp on 8/11/14.
 */
public class PreviewActivity extends Activity{

    public static final String KEY_URI = "com.djh.googlyeyes.activities.PreviewActivity.KEY_URI";

    private ImageView mImageView;
    private Button shareButton;
    private Uri imageUri = null;
    private Bitmap bitmap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);
        mImageView = (ImageView) findViewById(R.id.image);
        shareButton = (Button) findViewById(R.id.shareButton);
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bitmap != null) {
                    //Create intent for sharing image
                    Intent shareIntent = new Intent();
                    shareIntent.setAction(Intent.ACTION_SEND);
                    shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
                    shareIntent.setType("image/jpeg");
                    startActivity(Intent.createChooser(shareIntent, "Share image..."));
                }
            }
        });
        getActionBar().setTitle("GOOGLY EYES");
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setIcon(R.drawable.ic_actionbar_icon);
        Intent inputIntent = getIntent();

        if (inputIntent != null) {
            if (inputIntent.getData() != null && inputIntent.getData() != null) {
                imageUri = inputIntent.getData();
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        
        bitmap = makeImageFromUri();
        if (bitmap != null) {
            mImageView.setImageBitmap(makeImageFromUri());
            bitmap.recycle();
        } else {
            finish();
            Toast.makeText(this, "File not found.", Toast.LENGTH_SHORT).show();

        }
    }

    private Bitmap makeImageFromUri() {
        Bitmap bitmap = null;

        try {

            bitmap = BitmapFactory.decodeStream(
                    getContentResolver().openInputStream(imageUri));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return bitmap;
    }
}
