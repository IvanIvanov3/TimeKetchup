package com.bytebazar.timeketchup;

 interface ActivityConnection {

    boolean onTimerClick();

    void onTimerLongClick();

    void openSettings();

    boolean checkStatus();

    int getRemainingTime();

    int getDurationTime();

    int getTodaySessions();
 }
