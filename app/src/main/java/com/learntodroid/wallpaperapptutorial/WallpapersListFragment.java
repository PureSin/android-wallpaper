package com.learntodroid.wallpaperapptutorial;

import android.Manifest;
import android.app.WallpaperManager;
import android.content.ContentResolver;
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
import android.widget.Button;

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
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Button queryButton;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!checkPermissions()) {
            return;
        }

        initUI();
    }

    private void initUI() {
        wallpaperGalleryRecyclerAdapter = new WallpaperGalleryRecyclerAdapter(this);
        query();
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
        queryButton = view.findViewById(R.id.query);
        queryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("KELVIN querying");
                Uri uri = Uri.parse("content://com.google.android.apps.photos.partnercontentprovider/getVersion");
                Bundle b = new Bundle();
                b.putInt("filter_id", 2);
                b.putString("partner_authority", "KELVIN");
                b.putParcelable("output_uri", Uri.parse("/storage/emulated/0/Download/tmp.jpg"));
                b.putString("file_name", "/storage/emulated/0/Download/IMG_5789.jpg");
                Bundle bundle = getContext().getContentResolver().call(uri, "applyPreviewFilter", null, b);
                if (bundle != null) {
                    System.out.println("KELVIN " + bundle);
                }
            }
        });
        wallpaperRecyclerView = view.findViewById(R.id.fragment_listwallpapers_recyclerView);
        wallpaperRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        wallpaperRecyclerView.setAdapter(wallpaperGalleryRecyclerAdapter);

        return view;
    }

    private void query() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Uri collection =  MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                            String[] projection = new String[] {
                                    MediaStore.Images.Media._ID,
                                    MediaStore.Images.Media.DISPLAY_NAME,
                                    MediaStore.Images.Media.TITLE,
                            };
                            ContentResolver contentResolver = getActivity().getApplicationContext().getContentResolver();

                            try (Cursor cursor = contentResolver.query(
                                    collection,
                                    projection,
                                    null,
                                    null,
                                    MediaStore.Images.Media.DATE_TAKEN + " DESC LIMIT 20"
                            )) {
                                // Cache column indices.
                                int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
                                int nameColumn =
                                        cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);
                                int titleColumn =
                                        cursor.getColumnIndexOrThrow(MediaStore.Images.Media.TITLE);

                                wallpapers = new ArrayList<>();
                                while (cursor.moveToNext()) {
                                    long id = cursor.getLong(idColumn);
                                    String name = cursor.getString(nameColumn);
                                    String title = cursor.getString(titleColumn);

                                    Uri contentUri = ContentUris.withAppendedId(
                                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
                                    System.out.println("URI: " +contentUri);
                                    wallpapers.add(new Wallpaper(title, contentUri));
                                }
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        wallpaperGalleryRecyclerAdapter.setWallpapers(wallpapers);
                                    }
                                });
                            }
                        }
                    });
                }
            });
        }
    }

    @Override
    public void onWallpaperSelect(Wallpaper wallpaper) {
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(getContext());
        startActivity(wallpaperManager.getCropAndSetWallpaperIntent(wallpaper.getImageUri()));
    }
}
