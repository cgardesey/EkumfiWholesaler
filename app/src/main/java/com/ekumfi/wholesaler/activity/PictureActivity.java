package com.ekumfi.wholesaler.activity;

import static com.ekumfi.wholesaler.receiver.NetworkReceiver.activeActivity;

import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.ekumfi.wholesaler.R;
import com.ekumfi.wholesaler.receiver.NetworkReceiver;
import com.greysonparrelli.permiso.Permiso;

import uk.co.senab.photoview.PhotoViewAttacher;


public class PictureActivity extends AppCompatActivity {

    NetworkReceiver networkReceiver;
    public static Bitmap idPicBitmap;

    private ImageView photoImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        photoImageView = findViewById(R.id.photoImageView);
        photoImageView.getLayoutParams().width = getWindowManager().getDefaultDisplay().getWidth();
        photoImageView.getLayoutParams().height = getWindowManager().getDefaultDisplay().getHeight();
        photoImageView.setAdjustViewBounds(true);
        //photoImageView.setScaleType(ImageView.ScaleType.FIT_XY);
        PhotoViewAttacher photoViewAttacher = new PhotoViewAttacher(photoImageView);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        photoImageView.setImageBitmap(idPicBitmap);

        networkReceiver = new NetworkReceiver();
    }


    @Override
    protected void onResume() {
        super.onResume();
        Permiso.getInstance().setActivity(this);
        activeActivity = this;
        registerReceiver(networkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(networkReceiver);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
