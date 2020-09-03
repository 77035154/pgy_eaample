package com.example.pgytest;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.pgyersdk.crash.PgyCrashManager;
import com.pgyersdk.update.DownloadFileListener;
import com.pgyersdk.update.PgyUpdateManager;
import com.pgyersdk.update.UpdateManagerListener;
import com.pgyersdk.update.javabean.AppBean;

import java.io.File;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    String TAG = "fanxiaobo";

    Button btn_update;
    Button btn_crash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn_update = findViewById(R.id.btn_update);
        btn_update.setOnClickListener(this);
        btn_crash = findViewById(R.id.btn_crash);
        btn_crash.setOnClickListener(this);

        //启动 Pgyer 检测 Crash 功能
        PgyCrashManager.register(); //推荐使用
    }


    @Override
    public void onClick(View view) {
        switch(view.getId()){

            case R.id.btn_update:
                CheckUpdate();
                break;
            case R.id.btn_crash:
                PgyCrashManager.register();
                throw new RuntimeException("这是个测试bug!");

            default:break;


        }
    }

    public void CheckUpdate(){
        /** 新版本 **/
        new PgyUpdateManager.Builder()
                .setForced(true)                //设置是否强制提示更新,非自定义回调更新接口此方法有用
                .setUserCanRetry(false)         //失败后是否提示重新下载，非自定义下载 apk 回调此方法有用
                .setDeleteHistroyApk(false)     //检查更新前是否删除本地历史 Apk， 默认为true
                .setUpdateManagerListener(new UpdateManagerListener() {
                    @Override
                    public void onNoUpdateAvailable() {
                        //没有更新是回调此方法
                        Log.d(TAG, "there is no new version");
                    }
                    @Override
                    public void onUpdateAvailable(AppBean appBean) {
                        //有更新回调此方法
                        Log.d(TAG, "there is new version can update"
                                + "new versionCode is " + appBean.getVersionCode());
                        //调用以下方法，DownloadFileListener 才有效；
                        //如果完全使用自己的下载方法，不需要设置DownloadFileListener
                        PgyUpdateManager.downLoadApk(appBean.getDownloadURL());
                    }

                    @Override
                    public void checkUpdateFailed(Exception e) {
                        //更新检测失败回调
                        //更新拒绝（应用被下架，过期，不在安装有效期，下载次数用尽）以及无网络情况会调用此接口
                        Log.e(TAG, "check update failed 更新拒绝（应用被下架，过期，不在安装有效期，下载次数用尽）以及无网络情况会调用此接口", e);
                    }
                })
                //注意 ：
                //下载方法调用 PgyUpdateManager.downLoadApk(appBean.getDownloadURL()); 此回调才有效
                //此方法是方便用户自己实现下载进度和状态的 UI 提供的回调
                //想要使用蒲公英的默认下载进度的UI则不设置此方法
                .setDownloadFileListener(new DownloadFileListener() {
                    @Override
                    public void downloadFailed() {
                        //下载失败
                        Log.e(TAG, "download apk failed");
                    }

                    @Override
                    public void downloadSuccessful(File file) {
                        Log.e(TAG, "download apk success" + file.getAbsolutePath() + file.getName());
                        // 使用蒲公英提供的安装方法提示用户 安装apk
                        PgyUpdateManager.installApk(file);
                        //android.os.Process.killProcess(android.os.Process.myPid());
                    }

                    @Override
                    public void onProgressUpdate(Integer... integers) {
                        Log.e(TAG, "update download apk progress" + integers);
                    }})
                .register();
    }
}