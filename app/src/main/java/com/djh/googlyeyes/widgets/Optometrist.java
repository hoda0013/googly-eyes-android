package com.djh.googlyeyes.widgets;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dillonhodapp on 8/11/14.
 */
public class Optometrist {
    public final static Optometrist INSTANCE = new Optometrist();
    private List<GooglyEyeWidget> eyeList = new ArrayList<GooglyEyeWidget>();

    private Optometrist() {

    }

    public GooglyEyeWidget makeEye(Context context, GooglyEyeWidget.Listener listener) {
        GooglyEyeWidget eye = new GooglyEyeWidget(context);
        eye.setListener(listener);
        eye.setId(eyeList.size());
        eyeList.add(eye);
        getFocus(eye.getId());
        return eye;
    }

    public void getFocus(int id) {
        for (GooglyEyeWidget eye : eyeList) {
            if (eye.getId() == id) {
                eye.setMode(GooglyEyeWidget.Mode.DRAGGING);
            } else {
                eye.setMode(GooglyEyeWidget.Mode.PLACED);
            }
        }
    }

}
