package com.djh.googlyeyes.fragments;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.djh.googlyeyes.R;
import com.djh.googlyeyes.widgets.GooglyEyeWidget;
import com.djh.googlyeyes.widgets.Optometrist;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by dillonhodapp on 8/13/14.
 */
public class EyeFragment extends BaseFragment {

    public static final String FRAG_TAG = "com.djh.googlyeyes.fragments.EyeFragment.FRAG_TAG";
    public static final String KEY_URI = "com.djh.googleeyes.fragments.EyeFragment.KEY_URI";
    public static final String KEY_HAS_SEEN_SLIDESHOW = "com.djh.googlyeyes.fragments.EyeFragment.KEY_HAS_SEEN_SLIDESHOW";


    private Uri mImageUri = null;
    private Listener mListener;
    private ImageView imageView;
    private RelativeLayout mContainer;
    private RelativeLayout mImageFrame;
    private RelativeLayout instructionSlide;

    private boolean hasSeenPreviewSlide = false;

    public interface Listener {
        public void onNextClicked(Uri uri);
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    public static EyeFragment newInstance(Uri uri) {
        Bundle bundle = new Bundle();
        bundle.putString(KEY_URI, uri.toString());
        EyeFragment frag = new EyeFragment();
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
        View view = inflater.inflate(R.layout.fragment_eye, container, false);
        imageView = (ImageView) view.findViewById(R.id.imageView);
        imageView.setImageURI(mImageUri);
        mContainer = (RelativeLayout) view.findViewById(R.id.container);
        mImageFrame = (RelativeLayout) view.findViewById(R.id.imageFrame);
        instructionSlide = (RelativeLayout) view.findViewById(R.id.instructionSlide);

        getActivity().getActionBar().setTitle("ADD ZEE GOOGLYS");
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
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.eye, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.add_eye) {
            addEye();
        } else if (item.getItemId() == R.id.next) {
            //Save image with eyes to GooglyEye dir, pass forward to preview fragment
            File file = saveImage();
            if (file == null) {
                //TODO: Throw error
                Toast.makeText(getActivity(), getString(R.string.problem_saving), Toast.LENGTH_SHORT).show();
            } else {
                //Get uri and pass on to preview fragment
                Uri uri = Uri.fromFile(file);
                mListener.onNextClicked(uri);
            }

            return true;
        }

        return false;
    }

    private void addEye() {
        //create new eye and set listeners
        GooglyEyeWidget eye = Optometrist.INSTANCE.makeEye(getActivity(), new GooglyEyeWidget.Listener() {
            @Override
            public void removeView(GooglyEyeWidget eye) {
                mImageFrame.removeView(eye);
            }

            @Override
            public void updateVals(double x, double z) {

            }

        });
        mImageFrame.addView(eye);
    }

        private File saveImage() {

            //Unfocus any eyes
            Optometrist.INSTANCE.removeFocusFromAll();

            //Create filename
            String filename = Environment.getExternalStorageDirectory().toString() + "/" + getString(R.string.photo_directory) + "/"
                    + System.currentTimeMillis() + ".jpg";
            File f = new File(filename);

            //Find directory, create if doesn't exist
            if (!f.getParentFile().exists()) {
                f.getParentFile().mkdirs();
            }

            //Grab bitmap of image
            mImageFrame.setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(mImageFrame.getDrawingCache());
            mImageFrame.destroyDrawingCache();

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
                Toast.makeText(getActivity(), getString(R.string.problem_saving), Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getActivity(), getString(R.string.problem_saving), Toast.LENGTH_SHORT).show();
            }

        return f;
    }
}
