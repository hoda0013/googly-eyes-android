package com.djh.googlyeyes.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
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
import com.djh.googlyeyes.activities.MainActivity;
import com.djh.googlyeyes.models.Eye;
import com.djh.googlyeyes.widgets.GooglyEyeWidget;
import com.djh.googlyeyes.widgets.Optometrist;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dillonhodapp on 8/13/14.
 */
public class EyeFragment extends BaseFragment {

    public static final String FRAG_TAG = "com.djh.googlyeyes.fragments.EyeFragment.FRAG_TAG";
    public static final String KEY_URI = "com.djh.googleeyes.fragments.EyeFragment.KEY_URI";
    public static final String KEY_HAS_SEEN_SLIDESHOW = "com.djh.googlyeyes.fragments.EyeFragment.KEY_HAS_SEEN_SLIDESHOW";
    public static final String KEY_FILENAME = "com.djh.googleeyes.fragments.EyeFragment.KEY_FILENAME";
    public static final String KEY_MILLIS = "com.djh.googleeyes.fragments.EyeFragment.KEY_MILLIS";

    private Context mContext;
    private Uri mImageUri = null;
    private long mMillis = 0;
    private Listener mListener;
    private ImageView imageView;
    private RelativeLayout mContainer;
    private RelativeLayout mImageFrame;
    private RelativeLayout instructionSlide;
    private RelativeLayout banner;
    private boolean isEyeHighlighted = false;
    private GooglyEyeWidget currentEye = null;
    private boolean hasSeenPreviewSlide = false;
    private String filename;

    private List<GooglyEyeWidget> mEyes = new ArrayList<GooglyEyeWidget>();

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
        Log.e("ON CREATE", "");
        mContext = getActivity();
        if (getArguments() != null && getArguments().getString(KEY_URI) != null) {
            mImageUri = Uri.parse(getArguments().getString(KEY_URI));
        }
        if (savedInstanceState == null) {
            filename = Environment.getExternalStorageDirectory().toString() + "/" + getString(R.string.photo_directory) + "/"
                    + System.currentTimeMillis() + ".jpg";
            mMillis = System.currentTimeMillis();
        }

        setHasOptionsMenu(true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_URI, mImageUri.toString());
        outState.putLong(KEY_MILLIS, mMillis);
        saveEyesToDb();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.e("ON CREATE VIEW", "");
        List<Eye> savedEyes = null;

        if (savedInstanceState != null) {
            mImageUri = Uri.parse(savedInstanceState.getString(KEY_URI));
            mMillis = savedInstanceState.getLong(KEY_MILLIS);
//            savedEyes = Eye.find(Eye.class, "filename = ?", filename);
//            Optometrist.INSTANCE.removeAllEyes();
        }

        View view = inflater.inflate(R.layout.fragment_eye, container, false);
        imageView = (ImageView) view.findViewById(R.id.imageView);
        imageView.setImageURI(mImageUri);

        mContainer = (RelativeLayout) view.findViewById(R.id.container);
        mImageFrame = (RelativeLayout) view.findViewById(R.id.imageFrame);
        instructionSlide = (RelativeLayout) view.findViewById(R.id.instructionSlide);
        banner = (RelativeLayout) view.findViewById(R.id.banner);
        banner.setVisibility(View.INVISIBLE);

        getActivity().getActionBar().setTitle("ADD ZEE GOOGLYS");
        getActivity().getActionBar().setHomeButtonEnabled(true);
        getActivity().getActionBar().setLogo(R.drawable.ic_back_button);

        if (savedEyes != null) {
            for (int i = 0; i < savedEyes.size(); i++) {
                addSavedEye(savedEyes.get(i));
            }
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e("ON RESUME", "");
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
        Log.e("ON PAUSE", "");

    }

    @Override
    public void onStop() {
        super.onStop();
        Log.e("ON STOP", "");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.e("ON DESTROY VIEW", "");
        if (imageView != null) {
            if (imageView.getDrawable() != null) {
                ((BitmapDrawable) imageView.getDrawable()).getBitmap().recycle();
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.eye, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        if (isEyeHighlighted) {
            menu.getItem(1).setEnabled(true);
        } else {
            menu.getItem(1).setEnabled(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        instructionSlide.setVisibility(View.GONE);
        //Mark slide as having been seen before
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sp.edit().putBoolean(KEY_HAS_SEEN_SLIDESHOW, true).apply();

        if (item.getItemId() == R.id.add_eye) {
            addEye();
        } else if (item.getItemId() == R.id.delete_eye){
//            Optometrist.INSTANCE.removeEye(currentEye);
            mImageFrame.removeView(currentEye);
            currentEye = null;
        } else if (item.getItemId() == R.id.next) {
            ((MainActivity)getActivity()).showProgress();
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

    GooglyEyeWidget.Listener eyeListener = new GooglyEyeWidget.Listener() {
        @Override
        public void removeView(GooglyEyeWidget eye) {
            mImageFrame.removeView(eye);
        }

        @Override
        public void updateVals(double x, double z) {

        }

        @Override
        public void onFocus(GooglyEyeWidget eye) {
            for (GooglyEyeWidget i : mEyes) {
                if (eye.equals(i)) {
//                    i.setMode(GooglyEyeWidget.Mode.DRAGGING);
                } else {
                    i.setMode(GooglyEyeWidget.Mode.PLACED);
                }
            }
            currentEye = eye;
            isEyeHighlighted = true;
            getActivity().invalidateOptionsMenu();
        }

        @Override
        public void onUnfocus() {
            currentEye = null;
            isEyeHighlighted = false;
            getActivity().invalidateOptionsMenu();
        }
    };

    private void addEye() {
        //create new instance of eye
        if (mEyes != null && !mEyes.isEmpty()) {
            //get size of last eye
            int eyeSize = mEyes.get(mEyes.size() - 1).getBoxWidth();
            currentEye = new GooglyEyeWidget(mContext, eyeListener, eyeSize);
        } else {
            currentEye = new GooglyEyeWidget(mContext, eyeListener);
        }
        //add eye to list of eyes
        mEyes.add(currentEye);
        //overlay eye on image
        mImageFrame.addView(currentEye);
        //give focus to this eye, take focus away from other eyes
        for (GooglyEyeWidget eye : mEyes) {
            if (eye.equals(currentEye)) {
                eye.setMode(GooglyEyeWidget.Mode.EDITING);
            } else {
                eye.setMode(GooglyEyeWidget.Mode.PLACED);
            }
        }
        isEyeHighlighted = true;
        getActivity().invalidateOptionsMenu();
    }

    private void saveEyesToDb() {
//        List<GooglyEyeWidget> list = Optometrist.INSTANCE.getEyeList();
//        List<Eye> currentEyes = Eye.find(Eye.class, "filename = ?", filename);
//        for (Eye eye : currentEyes) {
//            eye.delete();
//        }
//        for (int i = 0; i < list.size(); i++) {
//            Eye eye = new Eye();
//            eye.eyeX = list.get(i).getBoxCornerX();
//            eye.eyeY = list.get(i).getBoxCornerY();
//            eye.eyeSize = list.get(i).getBoxWidth();
//            eye.uri = mImageUri.toString();
//            eye.millis = mMillis;
//            eye.save();
//        }
    }

    private void addSavedEye(Eye eye) {
//        mImageFrame.addView(Optometrist.INSTANCE.makeEye(getActivity(), eyeListener, eye.eyeX, eye.eyeY, eye.eyeSize));
    }

    private File saveImage() {

        //Unfocus any eyes
//        Optometrist.INSTANCE.removeFocusFromAll();

        //Create filename

        File f = new File(filename);

        //Find directory, create if doesn't exist
        if (!f.getParentFile().exists()) {
            f.getParentFile().mkdirs();
        }
        banner.setVisibility(View.VISIBLE);
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
                //write to db

            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), getString(R.string.problem_saving), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), getString(R.string.problem_saving), Toast.LENGTH_SHORT).show();
        }

       saveEyesToDb();
        return f;
    }
}
