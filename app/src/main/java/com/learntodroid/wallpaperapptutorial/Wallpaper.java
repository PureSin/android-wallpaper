package com.learntodroid.wallpaperapptutorial;

import android.net.Uri;

public class Wallpaper {
    private String title;
    private Uri imageUri;

    public Wallpaper(String title, Uri imageUri) {
        this.title = title;
        this.imageUri = imageUri;
    }

    public String getTitle() {
        return title;
    }

    public Uri getImageUri() {
        return imageUri;
    }
}
