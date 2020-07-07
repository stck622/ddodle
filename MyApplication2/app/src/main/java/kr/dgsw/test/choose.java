package kr.dgsw.test;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.facebook.login.LoginManager;

public class choose extends AppCompatActivity {

    Intent intent;

    /**
     * 서비스 러닝 여부 채크
     **/
    public boolean isServiceRunningCheck() {
        ActivityManager manager = (ActivityManager) this.getSystemService(Activity.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if ("kr.dgsw.test.MyService".equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose);

        intent = new Intent(this, MyService.class); //클릭시 넘겨줄 인텐트


        /** 포그라운드 서비스 동작 **/
        if (!isServiceRunningCheck()) {
            if (Build.VERSION.SDK_INT >= 26) { //안드로이드 버전 체크
                this.startForegroundService(intent);
            } else {
                this.startService(intent);
            }
        }


        /** 맵뷰 **/
        findViewById(R.id.bt_view_writing).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MyService.getfbData() != null)
                    startActivity(new Intent(choose.this, activity_map.class));
                else
                    Toast.makeText(choose.this, "DB 연결중 입니다. 잠시 기다려주세요", Toast.LENGTH_LONG).show();
            }
        });


        /** 작성 화면 **/
        findViewById(R.id.bt_write).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MyService.getfbData() != null)
                    startActivity(new Intent(choose.this, write.class));
                else
                    Toast.makeText(choose.this, "DB 연결중 입니다. 잠시 기다려주세요", Toast.LENGTH_LONG).show();
            }
        });


        /** 로그아웃 **/
        findViewById(R.id.bt_logout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginManager.getInstance().logOut(); //로그아웃

                stopService(intent);

                /** 홈으로 돌아가기 **/
                Intent intentHome = new Intent(choose.this, MainActivity.class);
                intentHome.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intentHome.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intentHome);
                finish();
            }
        });


    }
}