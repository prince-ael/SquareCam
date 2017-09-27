package com.ael_mnisiddique.com.squarecam;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import java.io.File;

public class CameraActivity extends AppCompatActivity implements FragmentListener{

    private static final int REQUEST_STORAGE_PERMISSION = 299;
    private ActivityListener mActivityListener;
    private Button mCaptureButton;

    private ProgressBar mCaptureIndicator;
    private AlertDialog.Builder mErrorDialog;

    private static File mSquareCamDir = new File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString()+"/SquareCam");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        CameraWindowFragment mCameraWindow = new CameraWindowFragment();

        mActivityListener = mCameraWindow;

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragment_container , mCameraWindow)
                .commit();

        mCaptureButton = (Button) findViewById(R.id.btn_capture);
        mCaptureIndicator = (ProgressBar) findViewById(R.id.pb_capture_indicator);

        mCaptureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ActivityCompat.checkSelfPermission(CameraActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestFileWritePermission();
                }else{

                    mActivityListener.onCaptureButtonClicked(mSquareCamDir.toString());
                }
            }
        });
    }

    private void showErrorDialog(String msg, String title){

        mErrorDialog = new AlertDialog.Builder(this);
        mErrorDialog.setMessage(msg);
        mErrorDialog.setTitle(title);

        mErrorDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                requestFileWritePermission();
                dialogInterface.dismiss();
            }
        });

        mErrorDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                dialogInterface.dismiss();
                finish();
            }
        });

        mErrorDialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(Build.VERSION.SDK_INT < 23){
            createDirectory();
        }
        mActivityListener.startCamera();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mActivityListener.stopCamera();
    }

    @Override
    public void onSaveFinished() {
        mCaptureIndicator.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onStartSaving() {
        mCaptureIndicator.setVisibility(View.VISIBLE);
        mCaptureIndicator.getIndeterminateDrawable().setColorFilter(Color.WHITE , PorterDuff.Mode.MULTIPLY);
    }

    private void requestFileWritePermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                REQUEST_STORAGE_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(requestCode == REQUEST_STORAGE_PERMISSION && grantResults[0] == PackageManager.PERMISSION_DENIED){

            showErrorDialog("SquareCam needs storage permission. Would you accept that ?","Permission Alert");
            return;
        }
        createDirectory();
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }

    private void createDirectory(){

        if(!mSquareCamDir.exists()){
            mSquareCamDir.mkdir();
        }
    }
}
