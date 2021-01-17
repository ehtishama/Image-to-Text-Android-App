package com.eadevelopers.easy_image_to_text;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.eadevelopers.easy_image_to_text.databinding.ActivityMainBinding;
import com.google.android.gms.common.internal.ResourceUtils;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.Permission;

import javax.xml.transform.Result;

public class MainActivity extends AppCompatActivity {

    private static final int IMAGE_REQ_CODE = 123;
    private static final int CAMERA_REQ_CODE = 124;

    private static final String TAG = "MainActivity";
    private static final int STORAGE_PERMISSION_REQ_CODE = 456;

    private ActivityMainBinding binding;

    private Uri imageFromCameraUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.binding = DataBindingUtil.setContentView(this, R.layout.activity_main);


        binding.pickImageButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                // check for permissions
                if (!haveStoragePermission()) {
                    askStoragePermission();
                    return;
                }

                Intent requestIntent = new Intent(Intent.ACTION_PICK);
                requestIntent.setType("image/jpg");
                startActivityForResult(requestIntent, IMAGE_REQ_CODE);


                askStoragePermission();
                

            }
        });

        binding.openCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!haveStoragePermission()) {
                    askStoragePermission();
                    return;
                }

                File photoFile = null;
                photoFile = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "image.jpeg");

                Uri photoUri = FileProvider.getUriForFile(MainActivity.this,
                        "com.eadevelopers.file_provider",
                        photoFile);


                imageFromCameraUri = photoUri;

                Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                i.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);

                startActivityForResult(i, CAMERA_REQ_CODE);

            }
        });


    }

    private boolean haveStoragePermission() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }


    private void askStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_REQ_CODE);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);


        if (requestCode == STORAGE_PERMISSION_REQ_CODE
                && grantResults.length > 1
                && grantResults[1] == PackageManager.PERMISSION_GRANTED
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            Toast.makeText(this, "Permissions granted.", Toast.LENGTH_SHORT).show();
            
        } else {
            Toast.makeText(this, "Permissions required.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == IMAGE_REQ_CODE) {
            Uri uri = data.getData();
            Log.d(TAG, "onActivityResult: " + uri);


            Bitmap imBitmap = null;
            try {
                InputStream is = getContentResolver().openInputStream(uri);
                imBitmap = BitmapFactory.decodeStream(is);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            File f = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "image.jpeg");
            startCropActivity(uri, Uri.fromFile(f));

        } else if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {

            Uri resultUri = UCrop.getOutput(data);

            InputImage inputImage;
            try {
                inputImage = InputImage.fromFilePath(MainActivity.this, resultUri);

                TextRecognizer recognizer = TextRecognition.getClient();
                recognizer.process(inputImage)
                        .addOnSuccessListener(new OnSuccessListener<Text>() {
                            @Override
                            public void onSuccess(Text text) {
                                Log.d(TAG, "onSuccess: " + text.getText());

                                Intent i = new Intent(MainActivity.this, ResultActivity.class);
                                i.putExtra("text", text.getText());

                                startActivity(i);

                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e(TAG, "onFailure: Text Recognition error", e);
                            }
                        });


            } catch (IOException e) {
                e.printStackTrace();
            }


        } else if (resultCode == UCrop.RESULT_ERROR) {
        } else if (resultCode == RESULT_OK && requestCode == CAMERA_REQ_CODE) {
            if (data == null)
                Log.d(TAG, "onActivityResult: Data is null, do you see that");
            File f = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "image1.jpeg");
            startCropActivity(imageFromCameraUri, Uri.fromFile(f));

        }


    }

    private void startCropActivity(Uri source, Uri destination) {
        UCrop.of(source, destination)
                .start(MainActivity.this);
    }
}