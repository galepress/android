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

import com.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;

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
            byte[] byteArray = getIntent().getByteArrayExtra("cropImage");
            bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);

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
                Uri bmpUri = Uri.parse(pathofBmp);
                final Intent shareIntent = new Intent(     android.content.Intent.ACTION_SEND);
                shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                shareIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
                shareIntent.setType("image/png");
                startActivity(shareIntent);
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