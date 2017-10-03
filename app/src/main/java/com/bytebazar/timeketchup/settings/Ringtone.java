package com.bytebazar.timeketchup.settings;

public final class Ringtone {
    private final String uri;
    private final String title;

    Ringtone(String uri, String title) {
        this.uri = uri;
        this.title = title;
    }

    public String getUri() {
        return uri;
    }

    public String getTitle() {
        return title;
    }
}
