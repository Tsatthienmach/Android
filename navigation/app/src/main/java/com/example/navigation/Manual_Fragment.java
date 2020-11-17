package com.example.navigation;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.pytorch.Module;

public class Manual_Fragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_manual, container, false);
    }

    private Bitmap bitmap = null;
    private Module module = null;
    private static int IMAGE_PICK_CODE = 100;
    private static int CAMERA_PERM_CODE = 101;
    private static int CAMERA_REQUEST_CODE = 102;
    ImageView imageView;
    TextView textView;
    Button infer_button;
    Button gallery_button;
    Button camera_button;

    @Override
    public void onCreate(Bundle savedInstanceState) {

    }
}
