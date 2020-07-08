package kr.dgsw.test;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.facebook.AccessToken;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MyService extends Service {

    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();
    private static ProfileTracker mProfileTracker;

    private static fbData fbdata;
    private static String fb_id;

    private static double latitude;
    private static double longitude;

    ArrayList<Integer> flag = new ArrayList<>();

    /**
     * fb 데이터 가져오기
     **/
    static fbData getfbData() {
        return fbdata;
    }


    /**
     * 페이스북 아이디 가져오기
     **/
    static String getfb_id() {
        if (Profile.getCurrentProfile() == null) {
            mProfileTracker = new ProfileTracker() {
                @Override
                protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                    fb_id = currentProfile.getId();
                    mProfileTracker.stopTracking();
                }
            };
        } else {
            Profile profile = Profile.getCurrentProfile();
            fb_id = profile.getId();
        }

        return fb_id;
    }

    static boolean getLoginFlag() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        boolean isLoggedIn = accessToken != null && !accessToken.isExpired();
        return isLoggedIn;
    }

    public static boolean isServiceRunningCheck(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Activity.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if ("kr.dgsw.test.MyService".equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Intent clsIntent = new Intent(this, MainActivity.class);
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
                                latitude = gpsTracker.getLatitude(); // 위도
                                longitude = gpsTracker.getLongitude(); //경도
                                Log.e("essas", String.valueOf(latitude) + " , " + String.valueOf(longitude));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }, 0);

                    try {

                        if (fbdata != null) {
                            if (fbdata.data.get(fb_id) != null) {
                                for (int i = 0; i < fbdata.data.get(fb_id).size(); i++) {
                                    if ((fbdata.data.get(fb_id).get(i) != null) && (fbdata.data.get(fb_id).get(i).get("posX") != null) && (fbdata.data.get(fb_id).get(i).get("posY") != null) && (fbdata.data.get(fb_id).get(i).get("text") != null)) {

                                        double mm = distance(latitude, longitude, Double.parseDouble(fbdata.data.get(fb_id).get(i).get("posX")), Double.parseDouble(fbdata.data.get(fb_id).get(i).get("posY")));

                                        if (mm <= 500) {
                                            if (!flag.contains(i)) {
                                                flag.add(i);

                                                Intent intent = new Intent(MyService.this, MainActivity.class);
                                                intentData intentdata = new intentData(i);
                                                intent.putExtra("index", intentdata);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                PendingIntent pendingIntent = PendingIntent.getActivity(MyService.this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

                                                String chId = "test";
                                                Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION); // 알림 왔을때 사운드.

                                                NotificationCompat.Builder notiBuilder = new NotificationCompat.Builder(MyService.this, chId)
                                                        .setSmallIcon(R.mipmap.ic_launcher)
                                                        .setContentTitle("낙서를 발견했습니다!")
                                                        .setContentText("이곳에서 낙서를 작성했었습니다. 클릭해서 확인해주세요.")
                                                        .setAutoCancel(true)
                                                        .setSound(soundUri)
                                                        .setContentIntent(pendingIntent);

                                                NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                                                /* 안드로이드 오레오 버전 대응 */
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                                    String chName = "ch name";
                                                    NotificationChannel channel = new NotificationChannel(chId, chName, NotificationManager.IMPORTANCE_HIGH);
                                                    manager.createNotificationChannel(channel);
                                                }
                                                manager.notify(0, notiBuilder.build());

                                                Log.e("essas", "INCIRCLE!");
                                            }
                                        } else {
                                            if (flag.contains(i)) {
                                                flag.remove(flag.indexOf(i));
                                                Log.e("essas", "OUTCIRCLE!");
                                            }
                                        }

                                    }
                                }
                            }
                        }


                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }

            }
        }).start();

        databaseReference.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                fbdata = dataSnapshot.getValue(fbData.class);
                fb_id = getfb_id();

                if (MainActivity.fragment_map != null)
                    MainActivity.fragment_map.reload();

                if (dataSnapshot.child("data").child(fb_id).getValue() == null) {
                    GpsTracker gpsTracker = new GpsTracker(getApplicationContext());
                    double latitude = gpsTracker.getLatitude(); // 위도
                    double longitude = gpsTracker.getLongitude(); //경도
                    DatabaseReference mRef = firebaseDatabase.getReference().child("data").child(fb_id).child("0");
                    mRef.child("posX").setValue(String.valueOf(latitude));
                    mRef.child("posY").setValue(String.valueOf(longitude));
                    mRef.child("text").setValue("환영합니다. 낙서를 시작해보세요!");
                    mRef.child("time").setValue(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date(System.currentTimeMillis())));
                    mRef.push();
                }

                if (fbdata.data == null)
                    return;

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }

        });

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

    private float distance(double currentlatitude, double currentlongitude, double originLat, double originLon) {

        float[] results = new float[1];
        Location.distanceBetween(currentlatitude, currentlongitude, originLat, originLon, results);
        float distanceInMeters = results[0];

        return distanceInMeters;
    }

}