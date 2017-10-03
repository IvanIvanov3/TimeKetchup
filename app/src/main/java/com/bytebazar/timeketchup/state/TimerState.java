package com.bytebazar.timeketchup.state;

import android.support.annotation.IntDef;

@IntDef({TimerState.INACTIVE, TimerState.WORK,TimerState.BREAK_SHORT,TimerState.BREAK_LONG})
public @interface TimerState {
    int INACTIVE = 0;
    int WORK = 1;
    int BREAK_SHORT = 2;
    int BREAK_LONG = 3;
}

