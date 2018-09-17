package com.zhouzhuo.pdfdemo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.joanzapata.pdfview.PDFView;
import com.joanzapata.pdfview.listener.OnPageChangeListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by zhouzhuo on 2018/9/17.
 */

public class MainActivity extends Activity {
    private final String TAG = "MainActivity";
    private PDFView pdfView;
    private String url2 ="https://static.87.cn/user_service/87fadan.pdf";
    //private final String url2 = "http://m.87.cn/androidapp/Lottery87_4.8.0.apk";
    private String path ;
    private TextView textView;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 4:
                    File apkfile = new File(Environment.getExternalStorageDirectory()
                            .getAbsolutePath()
                            + "/newsWeiziApkFile/"
                            + appName);
                    if(apkfile.exists()){

                        pdfView.fromFile(apkfile)
                                .pages(0, 2, 1, 3, 3, 3)
                                .defaultPage(1)
                                .showMinimap(false)
                                .enableSwipe(true)
                                .onDraw(null)
                                .onLoad(null)
                                .onPageChange(new OnPageChangeListener() {
                                    @Override
                                    public void onPageChanged(int page, int pageCount) {
                                    }
                                })
                                .load();
                    }else {
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        checkPermissions();
        //initData(url);
        downloadApk();

    }

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private void checkPermissions(){
        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }


    private void initView() {
        pdfView= findViewById(R.id.pdfview);
    }


    /**
     * 下载apk
     */
    private void downloadApk() {
        // 开启另一线程下载
        Thread downLoadThread = new Thread(downApkRunnable);
        downLoadThread.start();
    }


    private String appName ="87fadan.pdf";
    private ProgressBar progressBar;

    // 进度条显示数值
    private int progress = 0;
    private boolean isInterceptDownload ;


    /**
     * 从服务器下载新版apk的线程
     */
    private Runnable downApkRunnable = new Runnable() {
        @Override
        public void run() {

            try {
                // 服务器上新版apk地址
                URL url = new URL(url2);
                HttpURLConnection conn = (HttpURLConnection) url
                        .openConnection();
                conn.connect();
                int length = conn.getContentLength();
                InputStream is = conn.getInputStream();

                File file = new File(Environment
                        .getExternalStorageDirectory().getAbsolutePath()
                        + "/newsWeiziApkFile/");
                if (!file.exists()) {
                    // 如果文件夹不存在,则创建
                    file.mkdir();
                }
                // 下载服务器中新版本软件（写文件）
                String apkFile = Environment.getExternalStorageDirectory()
                        .getAbsolutePath()
                        + "/newsWeiziApkFile/"
                        + "87fadan.pdf";


                File ApkFile = new File(apkFile);
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(ApkFile);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                int count = 0;
                byte buf[] = new byte[1024];
                do {
                    int numRead = is.read(buf);
                    count += numRead;
                    // 更新进度条
                    progress = (int) (((float) count / length) * 100);
                    // progressBar.setProgress(progress);
                    handler.sendEmptyMessage(10);
                    if (numRead <= 0) {
                        // 下载完成通知安装
                        handler.sendEmptyMessage(4);
                        break;
                    }
                   Log.d(TAG,"numRead:"+numRead);
                   Log.d(TAG,"count:"+count);
                    fos.write(buf, 0, numRead);
                    // 当点击取消时，则停止下载
                } while (!isInterceptDownload);
                is.close();
                fos.close();
                    // 当点击取消时，则停止下载
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                // Toast.makeText(mContext, "写入文件出错，请检查是否有sd卡",
                // Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }

    };

}
