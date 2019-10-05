package com.example.qrcodescanner;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    // 右上選單實作

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        // 按下「刪除記錄」後的動作
        if (id == R.id.record_delete) {
            // 清空 textView 上的文字
            textView.setText("");
            // 清空 share_result 變數
            share_result = "";
            // 清空記錄值
            SharedPreferences record = getSharedPreferences("record", MODE_PRIVATE);
            record.edit()
                    .putString("QR Code Result", "")
                    .apply();
            // 建立提示訊息
            Toast.makeText(this, "記錄已刪除", Toast.LENGTH_SHORT).show();
        }
        else if (id == R.id.author) {
            Uri uri = Uri.parse("https://mnya.tw");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    SurfaceView surfaceView;
    TextView textView;
    CameraSource cameraSource;
    BarcodeDetector barcodeDetector;
    public static String share_result = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        textView = (TextView) findViewById(R.id.textView);
        textView.setMovementMethod(new ScrollingMovementMethod());
        textView.setTextIsSelectable(true);

        // 讀取前一次的掃描結果
        String getRecord = getSharedPreferences("record", MODE_PRIVATE)
                .getString("QR Code Result", "");
        textView.setText(getRecord);
        share_result = getRecord;

        barcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.QR_CODE).build();

        // 取得手機螢幕大小
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;

        cameraSource = new CameraSource.Builder(this, barcodeDetector)
                .setRequestedPreviewSize(width, height).setAutoFocusEnabled(true).build();

        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED)
                    return;
                try {
                    cameraSource.start(holder);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }

        });

        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {

            @Override
            public void release() {

            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> qrCodes = detections.getDetectedItems();
                if (qrCodes.size() != 0) {
                    textView.post(new Runnable() {
                        @Override
                        public void run() {
                            // 將掃描結果顯示於 textView 上
                            textView.setText(qrCodes.valueAt(0).displayValue);
                            // 將掃描結果存入分享變數
                            share_result = qrCodes.valueAt(0).displayValue;
                            // 將掃描結果存入記錄值中
                            SharedPreferences record = getSharedPreferences("record", MODE_PRIVATE);
                            record.edit()
                                    .putString("QR Code Result", share_result)
                                    .apply();
                            // 建立提示訊息
                            Toast.makeText(getApplicationContext(), "掃描成功", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

        });
        getPermissionsCamera();

        initFab();

    }

    // 取得相機權限
    public void getPermissionsCamera() {
        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA},1);
        }
    }

    // 右下角浮動分享按紐
    private void initFab(){
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, share_result);
                    sendIntent.setType("text/plain");

                    Intent shareIntent = Intent.createChooser(sendIntent, null);
                    startActivity(shareIntent);
            }
        });
    }

}
