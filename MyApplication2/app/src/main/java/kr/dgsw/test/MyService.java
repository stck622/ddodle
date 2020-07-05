package kr.dgsw.test;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class MyService extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // QQQ: 두번 이상 호출되지 않도록 조치해야 할 것 같다.
        Intent clsIntent = new Intent(this, activity_map.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, clsIntent, 0);

        NotificationCompat.Builder clsBuilder;
        if (Build.VERSION.SDK_INT >= 26) {
            String CHANNEL_ID = "gpsser";
            NotificationChannel clsChannel = new NotificationChannel(CHANNEL_ID, "gpsser", NotificationManager.IMPORTANCE_DEFAULT);
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(clsChannel);
            clsBuilder = new NotificationCompat.Builder(this, CHANNEL_ID);
        } else {
            clsBuilder = new NotificationCompat.Builder(this);
        }

        // QQQ: notification 에 보여줄 타이틀, 내용을 수정한다.
        clsBuilder.setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Ddoodle").setContentText("GPS 활성화 됨")
                .setContentIntent(pendingIntent);


        // foreground 서비스로 실행한다.
        startForeground(1, clsBuilder.build());

        // QQQ: 쓰레드 등을 실행하여서 서비스에 적합한 로직을 구현한다.

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    Handler mHandler = new Handler(Looper.getMainLooper());
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                GpsTracker gpsTracker = new GpsTracker(getApplicationContext());
                                double latitude = gpsTracker.getLatitude(); // 위도
                                double longitude = gpsTracker.getLongitude(); //경도
                                Log.e("essas", String.valueOf(latitude) + " , " + String.valueOf(longitude));
                            } catch (Exception e) {
                                Log.e("edsd",e.toString());
                                e.printStackTrace();
                            }
                        }
                    }, 0);

                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }

            }
        }).start();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


}