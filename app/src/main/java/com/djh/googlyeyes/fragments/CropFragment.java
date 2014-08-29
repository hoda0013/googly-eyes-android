package com.djh.googlyeyes.fragments;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.djh.googlyeyes.R;
import com.djh.googlyeyes.activities.MainActivity;
import com.djh.googlyeyes.util.Util;
import com.djh.googlyeyes.widgets.CropWidget;
import com.djh.googlyeyes.widgets.TouchImageView;
import com.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by dillonhodapp on 8/13/14.
 */
public class CropFragment extends BaseFragment{

    public static final String FRAG_TAG = "com.djh.googlyeyes.fragments.CropFragment.FRAG_TAG";
    public static final String KEY_URI = "com.djh.googlyeyes.fragments.CropFragment.KEY_URI";
    public static final String KEY_HAS_SEEN_SLIDESHOW = "com.djh.googlyeyes.fragments.CropFragment.KEY_HAS_SEEN_SLIDESHOW";

    private Uri mImageUri = null;

    private CropImageView mCropImageView;
    private RelativeLayout instructionSlide;
    private Listener mListener;
    private boolean hasSeenPreviewSlide = false;



    public interface Listener {
        public void onFinishedCropping(Uri uri);
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    public static CropFragment newInstance(Uri uri) {
        Bundle bundle = new Bundle();
        bundle.putString(KEY_URI, uri.toString());
        CropFragment frag = new CropFragment();
        frag.setArguments(bundle);
        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null && getArguments().getString(KEY_URI) != null) {
            mImageUri = Uri.parse(getArguments().getString(KEY_URI));
        }

        if (savedInstanceState != null) {
            mImageUri = Uri.parse(savedInstanceState.getString(KEY_URI));
        }
        setHasOptionsMenu(true);

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_URI, mImageUri.toString());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_crop, container, false);
        mCropImageView = (CropImageView) view.findViewById(R.id.CropImageView);
        instructionSlide = (RelativeLayout) view.findViewById(R.id.instructionSlide);

        String imagePath = Util.getPath(getActivity(), mImageUri);

        if (imagePath != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            mCropImageView.setImageBitmap(bitmap);
        }

        getActivity().getActionBar().setTitle("CROP");
        getActivity().getActionBar().setHomeButtonEnabled(true);
        getActivity().getActionBar().setLogo(R.drawable.ic_back_button);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        hasSeenPreviewSlide = sp.getBoolean(KEY_HAS_SEEN_SLIDESHOW, false);

        if (!hasSeenPreviewSlide) {
            instructionSlide.setVisibility(View.VISIBLE);
            instructionSlide.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    instructionSlide.setVisibility(View.GONE);
                    //Mark slide as having been seen before
                    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    sp.edit().putBoolean(KEY_HAS_SEEN_SLIDESHOW, true).apply();
                }
            });
        } else {
            instructionSlide.setVisibility(View.GONE);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.next, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.next) {
            ((MainActivity)getActivity()).showProgress();
            //Grag screenshot from cropping tool
            Bitmap bitmap = mCropImageView.getCroppedImage();

            //save bitmap
            saveCrop(bitmap);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveCrop(Bitmap bitmap) {
        //Create filename
        String filename = Environment.getExternalStorageDirectory().toString() + "/" + getString(R.string.photo_directory) + "/" + getString(R.string.crops) + "/"
                + System.currentTimeMillis() + ".jpg";
        File f = new File(filename);

        //Find directory, create if doesn't exist
        if (!f.getParentFile().exists()) {
            f.getParentFile().mkdirs();
        }

        //Save
        FileOutputStream fOut = null;
        boolean success = false;
        try {
            fOut = new FileOutputStream(f);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, fOut);
            fOut.flush();
            if (fOut != null) {
                fOut.close();
                success = true;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), getString(R.string.problem_saving), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), getString(R.string.problem_saving), Toast.LENGTH_SHORT).show();
        } finally {
            if (success == true) {
                mListener.onFinishedCropping(Uri.fromFile(f));
            }
        }
    }
}
