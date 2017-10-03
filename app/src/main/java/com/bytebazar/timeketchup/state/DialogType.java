package com.bytebazar.timeketchup.state;


import android.support.annotation.IntDef;

@IntDef({DialogType.SESSION_DURATION, DialogType.SHORT_BREAK_DURATION, DialogType.LONG_BREAK_DURATION
        , DialogType.SESSION_STREAK})
public @interface DialogType {
    int Empty = -1;
    int SESSION_DURATION = 0;
    int SHORT_BREAK_DURATION = 1;
    int LONG_BREAK_DURATION = 2;
    int SESSION_STREAK = 3;
}

