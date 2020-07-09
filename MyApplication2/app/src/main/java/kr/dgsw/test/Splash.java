package kr.dgsw.test;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Process;
import android.util.Log;
import android.widget.EditText;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Splash extends AppCompatActivity {

    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ConnectivityManager cm =
                (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        @SuppressLint("MissingPermission") NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        /* 인터넷 연결 여부 채크 */
        if (!isConnected) {
            AlertDialog.Builder builder = new AlertDialog.Builder(Splash.this);
            builder.setTitle("알림");
            builder.setMessage("인터넷 연결을 확인해주세요!");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    moveTaskToBack(true);                        // 태스크를 백그라운드로 이동
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        finishAndRemoveTask();                        // 액티비티 종료 + 태스크 리스트에서 지우기
                    }
                    Process.killProcess(Process.myPid());    // 앱 프로세스 종료
                }
            });
            builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialogInterface) {
                    moveTaskToBack(true);                        // 태스크를 백그라운드로 이동
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        finishAndRemoveTask();                        // 액티비티 종료 + 태스크 리스트에서 지우기
                    }
                    Process.killProcess(Process.myPid());    // 앱 프로세스 종료
                }
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        } else {

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

    }

    /* 메인 엑티비티 로딩 */
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
