package com.artifex.mupdfdemo;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import ak.detaysoft.galepress.GalePressApplication;
import ak.detaysoft.galepress.R;
import ak.detaysoft.galepress.util.ApplicationThemeColor;

/**
 * Created by p1025 on 04.02.2016.
 */
public class CropAndShareActivity extends Activity {

    private Bitmap bmp;
    private CropImageView cropImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop_and_share);

        ((RelativeLayout)findViewById(R.id.crop_base)).setBackgroundColor(ApplicationThemeColor.getInstance().getThemeColor());

        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            //options.inJustDecodeBounds = true;
            bmp = BitmapFactory.decodeFile(GalePressApplication.getInstance().getFilesDir().getAbsolutePath()+File.separator+"capturedImage.png", options);

            int display_mode = getIntent().getIntExtra("displayMode", Configuration.ORIENTATION_PORTRAIT);

            if(display_mode == Configuration.ORIENTATION_PORTRAIT){
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
            } else {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            finish();
        }

        cropImageView = (CropImageView) findViewById(R.id.crop_imageview);
        cropImageView.setGuidelines(1);
        cropImageView.setImageBitmap(bmp);


        Button share = (Button) findViewById(R.id.crop_submit_button);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            share.setBackground(ApplicationThemeColor.getInstance().getCropPageButtonDrawable(this, ApplicationThemeColor.CROP_PAGE_SUBMIT));
        else
            share.setBackgroundDrawable(ApplicationThemeColor.getInstance().getCropPageButtonDrawable(this, ApplicationThemeColor.CROP_PAGE_SUBMIT));
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pathofBmp = MediaStore.Images.Media.insertImage(getContentResolver(), cropImageView.getCroppedImage(),"title", null);


                if(pathofBmp != null) {
                    Uri bmpUri = Uri.parse(pathofBmp);
                    final Intent shareIntent = new Intent(     android.content.Intent.ACTION_SEND);
                    shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    shareIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
                    shareIntent.setType("image/png");
                    startActivity(shareIntent);
                } else {
                    /*
                     * https://fabric.io/galepress/android/apps/ak.detaysoft.feyz/issues/5758483affcdc0425003898f
                     * Bu hata cok nadirende olsa oluyor takip edilecek crash oldugu zaman fabric-answers uzerinden tespit edilecek
                     * */
                    Answers.getInstance().logCustom(new CustomEvent("cropandShareImageUrl").putCustomAttribute("url", ""+pathofBmp));
                    Toast.makeText(CropAndShareActivity.this, CropAndShareActivity.this.getResources().getText(R.string.WARNING_0), Toast.LENGTH_SHORT).show();
                }

            }
        });

        Button cancel = (Button) findViewById(R.id.crop_cancel_button);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            cancel.setBackground(ApplicationThemeColor.getInstance().getCropPageButtonDrawable(this, ApplicationThemeColor.CROP_PAGE_CANCEL));
        else
            cancel.setBackgroundDrawable(ApplicationThemeColor.getInstance().getCropPageButtonDrawable(this, ApplicationThemeColor.CROP_PAGE_CANCEL));
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
