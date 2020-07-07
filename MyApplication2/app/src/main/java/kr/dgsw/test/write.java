package kr.dgsw.test;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class write extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write);

        findViewById(R.id.bt_write).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (MainActivity.getfbData() == null) {
                    Toast.makeText(write.this, "DB 연결 대기중입니다. 잠시만 기다려주세요.", Toast.LENGTH_LONG).show();
                } else {
                    if (((EditText) findViewById(R.id.ed_write)).getText().toString().length() != 0) {
                        GpsTracker gpsTracker = new GpsTracker(write.this);
                        double latitude = gpsTracker.getLatitude(); // 위도
                        double longitude = gpsTracker.getLongitude(); //경도
                        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference().child("data").child(MainActivity.getfb_id()).child(String.valueOf(MainActivity.getfbData().data.get(MainActivity.getfb_id()).size()));
                        mRef.child("posX").setValue(String.valueOf(latitude));
                        mRef.child("posY").setValue(String.valueOf(longitude));
                        mRef.child("text").setValue(((EditText) findViewById(R.id.ed_write)).getText().toString());
                        mRef.push();
                        startActivity(new Intent(write.this, activity_map.class));
                        finish();
                    }
                }
            }
        });

    }


}
