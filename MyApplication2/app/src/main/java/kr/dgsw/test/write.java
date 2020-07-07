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

    fbData fbdata;
    String fb_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write);

        findViewById(R.id.bt_write).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fb_id = MyService.getfb_id();
                fbdata = MyService.getfbData();
                if (((EditText) findViewById(R.id.ed_write)).getText().toString().length() != 0) {
                    GpsTracker gpsTracker = new GpsTracker(write.this);
                    double latitude = gpsTracker.getLatitude(); // 위도
                    double longitude = gpsTracker.getLongitude(); //경도
                    DatabaseReference mRef = FirebaseDatabase.getInstance().getReference().child("data").child(fb_id).child(String.valueOf(fbdata.data.get(fb_id).size()));
                    mRef.child("posX").setValue(String.valueOf(latitude));
                    mRef.child("posY").setValue(String.valueOf(longitude));
                    mRef.child("text").setValue(((EditText) findViewById(R.id.ed_write)).getText().toString());
                    mRef.push();
                    startActivity(new Intent(write.this, activity_map.class));
                    finish();
                }

            }
        });

    }


}