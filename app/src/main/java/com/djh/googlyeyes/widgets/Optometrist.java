package com.djh.googlyeyes.widgets;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

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

    public int eyeId = 0;

    public GooglyEyeWidget makeEye(Context context, GooglyEyeWidget.Listener listener) {
        GooglyEyeWidget eye;
        int nextSize = 50;
        if (eyeList.size() >= 1) {
            nextSize = eyeList.get(eyeList.size() - 1).getBoxWidth();
            eye = new GooglyEyeWidget(context, nextSize);
        } else {
            eye = new GooglyEyeWidget(context);
        }
        eye.setListener(listener);
        eye.setId(eyeId++);
        eyeList.add(eye);
        getFocus(eye.getId());
        return eye;
    }

    public GooglyEyeWidget makeEye(Context context, GooglyEyeWidget.Listener listener, int x, int y, int size) {
        GooglyEyeWidget eye = makeEye(context, listener);
        eye.setBoxWidth(size);
        eye.setBoxCornerX(x);
        eye.setBoxCornerY(y);
        eye.setMode(GooglyEyeWidget.Mode.PLACED);
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

    public void removeEye(GooglyEyeWidget eye) {
        eyeList.remove(eye);
        for(int i = 0; i < eyeList.size(); i++){
            Log.e("EYE " + String.valueOf(i), " = String.valueOf(e.getId()))");
        }
    }

    public void removeFocusFromAll() {
        for (GooglyEyeWidget eye : eyeList) {
            eye.setMode(GooglyEyeWidget.Mode.PLACED);
        }
    }

    public void removeAllEyes() {
        eyeList.clear();
    }

    public boolean areEyesPresent() {
        if (eyeList.size() > 0) {
            return true;
        }

        return false;
    }

    public List<GooglyEyeWidget> getEyeList() {
        return eyeList;
    }

}
