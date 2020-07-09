package kr.dgsw.test;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

public class Splash extends AppCompatActivity {

    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        handler = new Handler();

        /** 포그라운드 서비스 동작 **/
        if (MyService.getLoginFlag()) {
            if (!MyService.isServiceRunningCheck(getApplicationContext())) {
                if (Build.VERSION.SDK_INT >= 26) { //안드로이드 버전 체크
                    this.startForegroundService(new Intent(this, MyService.class));
                } else {
                    this.startService(new Intent(this, MyService.class));
                }
            }

            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (MyService.getfbData() == null) ; //데이터 들어오면
                    handler.postDelayed(new splashhandler(), 0); //메인화면으로 이동
                }
            }).start();

        } else {
            handler.postDelayed(new splashhandler(), 3000);
        }

    }

    private class splashhandler implements Runnable {
        public void run() {
            startActivity(new Intent(getApplication(), MainActivity.class)); //로딩이 끝난 후, ChoiceFunction 이동
            Splash.this.finish(); // 로딩페이지 Activity stack에서 제거
        }
    }

    @Override
    public void onBackPressed() {
        //초반 플래시 화면에서 넘어갈때 뒤로가기 버튼 못누르게 함
    }


}
