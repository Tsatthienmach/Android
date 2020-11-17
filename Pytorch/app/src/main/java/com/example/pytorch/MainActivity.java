package com.example.pytorch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.pytorch.IValue;
import org.pytorch.Module;
import org.pytorch.Tensor;
import org.pytorch.torchvision.TensorImageUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;


public class MainActivity extends AppCompatActivity {

    private Bitmap bitmap = null;
    private Module module = null;
    private static int IMAGE_PICK_CODE = 100;
    private static int CAMERA_PERM_CODE = 101;
    private static int CAMERA_REQUEST_CODE = 102;
    ImageView imageView;
    TextView textView;
    Button infer_button;
    Button pic_load_button;
    Button live_button;
    Button capture_button;

    public static String assetFilePath(Context context, String assetName) throws IOException{
        File file = new File(context.getFilesDir(), assetName);
        if (file.exists() && file.length() > 0) {
            return file.getAbsolutePath();
        }

        try (InputStream is = context.getAssets().open(assetName)) {
            try (OutputStream os = new FileOutputStream(file)) {
                byte[] buffer = new byte[4 * 1024];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
                os.flush();
            }
            return file.getAbsolutePath();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            bitmap = BitmapFactory.decodeStream(getAssets().open("cat.jpg"));
            module = Module.load(assetFilePath(this, "static_quantized_model.pt"));
        }
        catch (IOException e) {
            Log.e("PTRDryRun", "Error reading assets", e);
            finish();
        }
        // Showing  image on UI ,Initialization
        imageView = findViewById(R.id.imageView2);
        textView = findViewById(R.id.textView3);
        infer_button = findViewById(R.id.infer_button);
        pic_load_button = findViewById(R.id.pic_load_button);
        capture_button = findViewById(R.id.capture_button);
        live_button = findViewById(R.id.live_button);

        // Views
        imageView.setImageBitmap(bitmap);

        // Function
            // Infer Button
        infer_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Clicked INFER Button", Toast.LENGTH_SHORT).show();
                final Tensor inputTensor = TensorImageUtils.bitmapToFloat32Tensor(bitmap,
                        TensorImageUtils.TORCHVISION_NORM_MEAN_RGB, TensorImageUtils.TORCHVISION_NORM_STD_RGB);

                // Running the model
                final Tensor outputTensor = module.forward(IValue.from(inputTensor)).toTensor();

                // getting tensor content as java array of floats
                final float[] scores = outputTensor.getDataAsFloatArray();

                // Searching for the index with maximum score
                float maxScore = -Float.MAX_VALUE;
                int maxScoreIndex = -1;
                for (int i = 0; i < scores.length; i++) {
                    if (scores[i] > maxScore) {
                        maxScore = scores[i];
                        maxScoreIndex = i;
                    }
                }

                String className = ImageNetClasses.CLASSES[maxScoreIndex];
                //showing className on UI
                textView.setText((className));
            }
        });

            // Image browsing button
        pic_load_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Clicked LOAD Button", Toast.LENGTH_SHORT).show();
                pickImageFromGallery();
            }
        });

        capture_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                askCameraPermissions();
                Toast.makeText(MainActivity.this, "Clicked Capture Button", Toast.LENGTH_SHORT).show();

            }
        });

        live_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Clicked LIVE Button", Toast.LENGTH_SHORT).show();

            }
        });


    }

    private void askCameraPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, CAMERA_PERM_CODE);
        }
        else {
            openCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==CAMERA_PERM_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                 openCamera();
            }
            else {
                Toast.makeText(this, "Camera Permission is required to Use Camera", Toast.LENGTH_SHORT).show();
            }
        }

    }

    private void openCamera() {
        Toast.makeText(this, "Camera Open Request", Toast.LENGTH_SHORT).show();
        Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(camera, CAMERA_REQUEST_CODE);
    }


    // pickImageFromGallery Function
    private void pickImageFromGallery() {
        // intent to pick image
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent.createChooser(intent,"Pick an image"), IMAGE_PICK_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Gallery picking
        if (resultCode==RESULT_OK && requestCode==IMAGE_PICK_CODE) {
            try {
                InputStream inputStream = getContentResolver().openInputStream(data.getData());
                bitmap = BitmapFactory.decodeStream(inputStream);
                imageView.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        // Camera capture
        if (requestCode==CAMERA_REQUEST_CODE ) {
            bitmap = (Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(bitmap);
        }
    }
}