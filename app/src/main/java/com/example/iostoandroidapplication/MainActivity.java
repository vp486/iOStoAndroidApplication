package com.example.iostoandroidapplication;

import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.iostoandroidapplication.enums.ImagePickerEnum;
import com.example.iostoandroidapplication.listeners.IImagePickerLister;
import com.example.iostoandroidapplication.utils.FileUtils;
import com.example.iostoandroidapplication.utils.UiHelper;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;


public class MainActivity extends AppCompatActivity implements IImagePickerLister{

    private static final int CAMERA_ACTION_PICK_REQUEST_CODE = 610;
    private static final int PICK_IMAGE_GALLERY_REQUEST_CODE = 609;
    public static final int CAMERA_STORAGE_REQUEST_CODE = 611;
    public static final int ONLY_CAMERA_REQUEST_CODE = 612;
    public static final int ONLY_STORAGE_REQUEST_CODE = 613;

    private String currentPhotoPath = "";
    private UiHelper uiHelper = new UiHelper();
    private ImageView imageView;
    private TextView textView1;
    private SeekBar seekBar;

    Uri image_uri2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.selectPictureButton).setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                if (uiHelper.checkSelfPermissions(this))
                    uiHelper.showImagePickerDialog(this, (IImagePickerLister) this);
        });
        imageView =(ImageView) findViewById(R.id.imageView);

        final TextView iv = findViewById(R.id.textView1);

        final SeekBar sk = (SeekBar) findViewById(R.id.seekBar);

        sk.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int p=0;

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
//                if(p < 1000)
//                {
//                    p=1000;
//                    seekBar.setProgress(p);
//                }
//            }
                Toast.makeText(MainActivity.this,""+ p ,Toast.LENGTH_SHORT);
            }


            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

                // TODO Auto-generated method stub
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
                // TODO Auto-generated method stub
                p=progress;
                float scale = ((p/40.0f)+1);
                iv.setScaleX(scale);
                iv.setScaleY(scale);


// Gets the layout:
                View view_instance = (View)findViewById(R.id.textView1);
                ViewGroup.LayoutParams params=view_instance.getLayoutParams();
                params.width=p;
                params.height=p;
                view_instance.setLayoutParams(params);


            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_STORAGE_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED)
                uiHelper.showImagePickerDialog(this, (IImagePickerLister) this);
            else if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_DENIED) {
                uiHelper.toast(this, "Visual Intelligence needs Storage access in order to store your profile picture.");
                finish();
            } else if (grantResults[0] == PackageManager.PERMISSION_DENIED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                uiHelper.toast(this, "Visual Intelligence needs Camera access in order to take profile picture.");
                finish();
            } else if (grantResults[0] == PackageManager.PERMISSION_DENIED && grantResults[1] == PackageManager.PERMISSION_DENIED) {
                uiHelper.toast(this, "Visual Intelligence needs Camera and Storage access in order to take profile picture.");
                finish();
            }
        } else if (requestCode == ONLY_CAMERA_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                uiHelper.showImagePickerDialog(this, (IImagePickerLister) this);
            else {
                uiHelper.toast(this, "Visual Intelligence needs Camera access in order to take profile picture.");
                finish();
            }
        } else if (requestCode == ONLY_STORAGE_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                uiHelper.showImagePickerDialog(this, (IImagePickerLister) this);
            else {
                uiHelper.toast(this, "Visual Intelligence needs Storage access in order to store your profile picture.");
                finish();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_ACTION_PICK_REQUEST_CODE && resultCode == RESULT_OK ) {
            //Uri uri = Uri.parse(currentPhotoPath);

            try {

                File file = getImageFile();
                Uri destinationUri = Uri.fromFile(file);
                openCropActivity(image_uri2, destinationUri);
            } catch (Exception e) {
                uiHelper.toast(this, "Error inside on activity result// camera action");
            }

            //openCropActivity(uri, uri);
        } else if (requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK) {
            if (data != null) {
                Uri uri = UCrop.getOutput(data);
                showImage(uri);
            }
        } else if (requestCode == PICK_IMAGE_GALLERY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            try {
                Uri sourceUri = data.getData();
                File file = getImageFile();
                Uri destinationUri = Uri.fromFile(file);
                openCropActivity(sourceUri, destinationUri);
            } catch (Exception e) {
                uiHelper.toast(this, "Error inside on activity result// gallery action");
            }
        }
    }

    private void openImagesDocument() {
       /*Intent pictureIntent = new Intent(Intent.ACTION_GET_CONTENT);
       pictureIntent.setType("image/*");
       pictureIntent.addCategory(Intent.CATEGORY_OPENABLE);
       if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
           String[] mimeTypes = new String[]{"image/jpeg", "image/png"};
           pictureIntent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
       }
       startActivityForResult(Intent.createChooser(pictureIntent, "Select Picture"), PICK_IMAGE_GALLERY_REQUEST_CODE);*/
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        startActivityForResult(galleryIntent, PICK_IMAGE_GALLERY_REQUEST_CODE);
    }

    private void showImage(Uri imageUri) {
        try {
            File file;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                file = FileUtils.getFile(this, imageUri);
            } else {
                file = new File(currentPhotoPath);
            }
            InputStream inputStream = new FileInputStream(file);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            imageView.setImageBitmap(bitmap);
        } catch (Exception e) {
            uiHelper.toast(this, "Please select different profile picture.//inside show picture");
        }
    }

    @Override
    public void onOptionSelected(ImagePickerEnum imagePickerEnum) {
        if (imagePickerEnum == ImagePickerEnum.FROM_CAMERA)
            openCamera();
        else if (imagePickerEnum == ImagePickerEnum.FROM_GALLERY)
            openImagesDocument();
    }

    private void openCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera");
        // Uri image_uri2;
        image_uri2 = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        //Camera intent
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri2);
        startActivityForResult(cameraIntent, CAMERA_ACTION_PICK_REQUEST_CODE);
    }
    /*
          Intent pictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
           File file;
           try {
               file = getImageFile(); // 1
           } catch (Exception e) {
               e.printStackTrace();
               uiHelper.toast(this, "Please take another image -- error inside opencamera");
               return;
           }
           Uri uri;
           if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) // 2
               uri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID.concat(".provider"), file);
           else
               uri = Uri.fromFile(file); // 3
           pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri); // 4
           startActivityForResult(pictureIntent, CAMERA_ACTION_PICK_REQUEST_CODE);
       }
    */
    private File getImageFile() throws IOException {
        String imageFileName = "JPEG_" + System.currentTimeMillis() + "_";
        File storageDir = new File(
                Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DCIM
                ), "Camera"
        );
        System.out.println(storageDir.getAbsolutePath());
        if (storageDir.exists())
            System.out.println("File exists");
        else
            System.out.println("File not exists");
        File file = File.createTempFile(
                imageFileName, ".jpg", storageDir
        );
        currentPhotoPath = "file:" + file.getAbsolutePath();
        return file;
    }

    private void openCropActivity(Uri sourceUri, Uri destinationUri) {
        UCrop.Options options = new UCrop.Options();
        options.setCircleDimmedLayer(true);
        options.setCropFrameColor(ContextCompat.getColor(this, R.color.colorAccent));
        //options.setCropGridStrokeWidth((int) 10);
        options.setMaxBitmapSize(10000);
        UCrop.of(sourceUri, destinationUri)
                .withMaxResultSize(1000, 1000)
                .withAspectRatio(5f, 5f)
                .start(this);
    }
}

