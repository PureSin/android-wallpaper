package com.learntodroid.wallpaperapptutorial;

import android.Manifest;
import android.app.WallpaperManager;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WallpapersListFragment extends Fragment implements WallpaperSelectListener {
    private static  final int READ_EXTERNAL_STORAGE_PERMISSION_REQUEST = 1;

    private RecyclerView wallpaperRecyclerView;
    private WallpaperGalleryRecyclerAdapter wallpaperGalleryRecyclerAdapter;
    private List<Wallpaper> wallpapers;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!checkPermissions()) {
            return;
        }

        initUI();
    }

    private void initUI() {
        wallpapers = new ArrayList<>();
        wallpapers.add(new Wallpaper("Beach", "https://images.pexels.com/photos/853199/pexels-photo-853199.jpeg?auto=compress&cs=tinysrgb&dpr=2&h=750&w=1260"));
        wallpapers.add(new Wallpaper("Mountain", "https://images.pexels.com/photos/1261728/pexels-photo-1261728.jpeg?auto=compress&cs=tinysrgb&dpr=2&h=750&w=1260"));
        wallpapers.add(new Wallpaper("Field", "https://images.pexels.com/photos/35857/amazing-beautiful-breathtaking-clouds.jpg?auto=compress&cs=tinysrgb&dpr=2&h=750&w=1260"));
        wallpapers.add(new Wallpaper("Clouds", "https://images.pexels.com/photos/2088205/pexels-photo-2088205.jpeg?auto=compress&cs=tinysrgb&dpr=2&h=750&w=1260"));
        wallpapers.add(new Wallpaper("Condensation", "https://images.pexels.com/photos/891030/pexels-photo-891030.jpeg?auto=compress&cs=tinysrgb&dpr=2&h=750&w=1260"));

        wallpaperGalleryRecyclerAdapter = new WallpaperGalleryRecyclerAdapter(this);
        wallpaperGalleryRecyclerAdapter.setWallpapers(wallpapers);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == READ_EXTERNAL_STORAGE_PERMISSION_REQUEST) {
            initUI();
        }
    }

    private boolean checkPermissions() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            if (!(ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
                requestPermissions((new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}), READ_EXTERNAL_STORAGE_PERMISSION_REQUEST);
                return false;
            }
            return true;
        }
        return true;
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_listwallpapers, container, false);

        wallpaperRecyclerView = view.findViewById(R.id.fragment_listwallpapers_recyclerView);
        wallpaperRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        wallpaperRecyclerView.setAdapter(wallpaperGalleryRecyclerAdapter);

        return view;
    }

    private void setHomeScreenWallpaper(Bitmap bitmap) {
        try {
            WallpaperManager.getInstance(getContext()).setBitmap(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setCroppedHomeScreenWallpaper(Bitmap bitmap) {
        try {
            WallpaperManager wallpaperManager = WallpaperManager.getInstance(getContext());

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                int wallpaperHeight = Resources.getSystem().getDisplayMetrics().heightPixels;
                int wallpaperWidth = Resources.getSystem().getDisplayMetrics().widthPixels;

                Point start = new Point(0, 0);
                Point end = new Point(bitmap.getWidth(), bitmap.getHeight());

                if (bitmap.getWidth() > wallpaperWidth) {
                    start.x = (bitmap.getWidth() - wallpaperWidth) / 2;
                    end.x = start.x + wallpaperWidth;
                }

                if (bitmap.getHeight() > wallpaperHeight) {
                    start.y = (bitmap.getHeight() - wallpaperHeight) / 2;
                    end.y = start.y + wallpaperHeight;
                }

                wallpaperManager.setBitmap(bitmap, new Rect(start.x, start.y, end.x, end.y), false);
            } else {
                wallpaperManager.setBitmap(bitmap);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setLockScreenWallpaper(Bitmap bitmap) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                WallpaperManager.getInstance(getContext()).setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onWallpaperSelect(final Wallpaper wallpaper) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    final WallpaperManager wallpaperManager = WallpaperManager.getInstance(getContext());
                        query();
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
//                                Intent intent = wallpaperManager.getCropAndSetWallpaperIntent(Uri.parse("content://media/external/images/media/1"));
//                                startActivity(intent);
                            }
                        });
                }
            });
        }
    }

    private void query() {
        Uri collection =  MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = new String[] {
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.TITLE,
        };
        try (Cursor cursor = getActivity().getApplicationContext().getContentResolver().query(
                collection,
                projection,
                null,
                null,
                 null
        )) {
            // Cache column indices.
            int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
            int nameColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);
            int titleColumnn =
                    cursor.getColumnIndexOrThrow(MediaStore.Images.Media.TITLE);
            System.out.println("KELVIN cursor=" + cursor.getCount());
            while (cursor.moveToNext()) {
                // Get values of columns for a given video.
                long id = cursor.getLong(idColumn);
                String name = cursor.getString(nameColumn);
                String title = cursor.getString(titleColumnn);

                Uri contentUri = ContentUris.withAppendedId(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id);
                System.out.println("KELVIN uri=" + contentUri + " title=" + title + " name = " + name);
            }
        }
    }
}
