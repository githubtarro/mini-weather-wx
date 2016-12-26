package cn.edu.pku.wangxin.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import cn.edu.pku.wangxin.miniweather.MainActivity;
import cn.edu.pku.wangxin.util.NetUtil;

public  class AutoUpdateService extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(120000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Intent intent1=new Intent("Weather_CHANGED_ACTION");
                sendBroadcast(intent1);
                Log.d("Service","onStartCommand");
            }
        }).start();
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}