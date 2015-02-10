package com.djh.googlyeyes.models;

import com.djh.googlyeyes.widgets.GooglyEyeWidget;
import com.orm.SugarRecord;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dillonhodapp on 9/8/14.
 */
public class Eye extends SugarRecord<Eye> {
    public long id;
    public int eyeX;
    public int eyeY;
    public int eyeSize;
    public String uri;
    public long millis;

    public Eye(){}

    public void bind(GooglyEyeWidget widget, String uri, long timeStamp) {
        eyeX = widget.getBoxCornerX();
        eyeY = widget.getBoxCornerY();
        eyeSize = widget.getBoxWidth();
        this.uri = uri;
        millis = timeStamp;
    }
}
