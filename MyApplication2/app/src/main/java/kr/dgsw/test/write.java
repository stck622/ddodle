package kr.dgsw.test;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class write extends AppCompatActivity {

    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();

    fbData fbdata;
    private ProfileTracker mProfileTracker;

    String fb_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write);

        if(Profile.getCurrentProfile() == null) {
            mProfileTracker = new ProfileTracker() {
                @Override
                protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                    fb_id = currentProfile.getId();
                    mProfileTracker.stopTracking();
                }
            };
            // no need to call startTracking() on mProfileTracker
            // because it is called by its constructor, internally.
        }
        else {
            Profile profile = Profile.getCurrentProfile();
            fb_id = profile.getId();
        }

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                fbdata = dataSnapshot.getValue(fbData.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        findViewById(R.id.bt_write).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fbdata == null) {
                    Toast.makeText(write.this, "DB 연결 대기중입니다. 잠시만 기다려주세요.", Toast.LENGTH_LONG).show();
                } else {
                    if (((EditText) findViewById(R.id.ed_write)).getText().toString().length() != 0) {
                        GpsTracker gpsTracker = new GpsTracker(write.this);
                        double latitude = gpsTracker.getLatitude(); // 위도
                        double longitude = gpsTracker.getLongitude(); //경도
                        DatabaseReference mRef = firebaseDatabase.getReference().child("data").child(fb_id).child(String.valueOf(fbdata.data.get(fb_id).size()));
                        mRef.child("posX").setValue(String.valueOf(latitude));
                        mRef.child("posY").setValue(String.valueOf(longitude));
                        mRef.child("text").setValue(((EditText) findViewById(R.id.ed_write)).getText().toString());
                        mRef.push();
                    }
                }
            }
        });

    }


}
