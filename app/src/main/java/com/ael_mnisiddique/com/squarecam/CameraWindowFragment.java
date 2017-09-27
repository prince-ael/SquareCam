package com.ael_mnisiddique.com.squarecam;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.flurgle.camerakit.CameraListener;
import com.flurgle.camerakit.CameraView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;


/**
 * A simple {@link Fragment} subclass.
 */
public class CameraWindowFragment extends Fragment
        implements ActivityListener{

    private CameraView mCameraView;

    private static final int FINAL_OUTPUT_WIDTH = 200;
    private static final int FINAL_OUTPUT_HEIGHT = 200;

    private FragmentListener mFgListener;

    private File mSquareCamDir;


    public CameraWindowFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mFgListener = (FragmentListener) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_camera_window, container, false);
        mCameraView = view.findViewById(R.id.cv_camWindow);
        mCameraView.setCameraListener(new CameraListener() {
            @Override
            public void onPictureTaken(byte[] jpeg) {
                super.onPictureTaken(jpeg);

                Bitmap image = BitmapFactory.decodeByteArray(jpeg, 0 , jpeg.length);
                new ProcessImage().execute(image);
            }
        });
        return view;
    }


    private String getImageTitle(String fileExtension){

        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int hrs = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        int second = cal.get(Calendar.SECOND);

        String date = String.valueOf(year)+String.valueOf(month+1)+String.valueOf(day);
        String time = String.valueOf(hrs)+String.valueOf(minute)+String.valueOf(second);

        return String.format("SCIMG_%s_%s.%s",date,time,fileExtension);
    }

    @Override
    public void onCaptureButtonClicked(String directoryPath) {
        mSquareCamDir = new File(directoryPath);
        mCameraView.captureImage();
    }

    @Override
    public void startCamera() {
        mCameraView.start();
    }

    @Override
    public void stopCamera() {
        mCameraView.stop();
    }

    private class ProcessImage extends AsyncTask<Bitmap , Void, Void>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mFgListener.onStartSaving();
        }

        @Override
        protected Void doInBackground(Bitmap... bitmaps) {
            //saveOriginal(bitmaps[0]);
            Bitmap resizedImage = getResizedBitmap(bitmaps[0], FINAL_OUTPUT_WIDTH, FINAL_OUTPUT_HEIGHT);
            saveAsJpeg(resizedImage);
            saveAsWebp(resizedImage);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mFgListener.onSaveFinished();
            Toast.makeText(getActivity(),"Photo Saved On : "+mSquareCamDir.toString(),Toast.LENGTH_SHORT).show();
        }

        private void saveOriginal(Bitmap image) {

            try {

                //File webp = new File(mSquareCamDir, "Original-"+getImageTitle("jpeg"));

                File jpeg = new File(mSquareCamDir, "Original-img.jpg");
                FileOutputStream out = new FileOutputStream(jpeg);
                image.compress(Bitmap.CompressFormat.JPEG, 100, out);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void saveAsJpeg(Bitmap image){

            try {
                File jpeg = new File(mSquareCamDir, getImageTitle("jpeg"));
                FileOutputStream out = new FileOutputStream(jpeg);
                image.compress(Bitmap.CompressFormat.JPEG , 100 , out);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void saveAsWebp(Bitmap image){

            try {

                File webp = new File(mSquareCamDir, getImageTitle("webp"));
                FileOutputStream out = new FileOutputStream(webp);
                image.compress(Bitmap.CompressFormat.WEBP , 100 , out);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public Bitmap getResizedBitmap(Bitmap bitmap, int newWidth, int newHeight) {
            Bitmap resizedBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888);

            float scaleX = newWidth / (float) bitmap.getWidth();
            float scaleY = newHeight / (float) bitmap.getHeight();
            float pivotX = 0;
            float pivotY = 0;

            Matrix scaleMatrix = new Matrix();
            scaleMatrix.setScale(scaleX, scaleY, pivotX, pivotY);

            Canvas canvas = new Canvas(resizedBitmap);
            canvas.setMatrix(scaleMatrix);
            canvas.drawBitmap(bitmap, 0, 0, new Paint(Paint.FILTER_BITMAP_FLAG));

            return resizedBitmap;
        }
    }
}
