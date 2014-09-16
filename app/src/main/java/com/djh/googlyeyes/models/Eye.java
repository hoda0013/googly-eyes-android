package com.djh.googlyeyes.models;

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
}
