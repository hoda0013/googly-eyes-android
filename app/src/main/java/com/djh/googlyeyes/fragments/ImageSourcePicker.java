package com.djh.googlyeyes.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.djh.googlyeyes.R;

/**
 * Created by dillonhodapp on 8/8/14.
 */
public class ImageSourcePicker extends DialogFragment {

    public static final String FRAG_TAG = "com.djh.googlyeyes.fragments.ImageSourcePicker.FRAG_TAG";
    private Listener mListener;

    public interface Listener {
        public void onTakePhotoSelected();
        public void onImageGallerySelected();
    }

    public static ImageSourcePicker newInstance(){
        ImageSourcePicker frag = new ImageSourcePicker();
        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    public void setListener(Listener listener){
        mListener = listener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_attachment_picker, null);
        TextView takePhoto = (TextView) view.findViewById(R.id.takePhotoTV);
        takePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onTakePhotoSelected();
                dismiss();
            }
        });

        TextView attachPhoto = (TextView) view.findViewById(R.id.attachPhotoTV);
        attachPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onImageGallerySelected();
                dismiss();
            }
        });

        dialogBuilder.setView(view);
        dialogBuilder.setCancelable(true);

        return dialogBuilder.create();
    }
}
