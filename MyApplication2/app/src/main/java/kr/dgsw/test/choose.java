package kr.dgsw.test;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class choose extends AppCompatActivity {

    public boolean isServiceRunningCheck() {
        ActivityManager manager = (ActivityManager) this.getSystemService(Activity.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if ("kr.dgsw.test.MyService".equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();

    fbData fbdata;
    private ProfileTracker mProfileTracker;

    String fb_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose);

        if (!isServiceRunningCheck()) {
            Intent intent = new Intent(this, MyService.class);
            if (Build.VERSION.SDK_INT >= 26) {
                this.startForegroundService(intent);
            } else {
                this.startService(intent);
            }
        }

        findViewById(R.id.bt_view_writing).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(choose.this, activity_map.class));
            }
        });

        findViewById(R.id.bt_write).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(choose.this, write.class));
            }
        });

        findViewById(R.id.bt_logout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                LoginManager.getInstance().logOut();

                Intent intentHome = new Intent(choose.this, MainActivity.class);
                intentHome.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intentHome.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intentHome);
                finish();

            }
        });

        if (Profile.getCurrentProfile() == null) {
            mProfileTracker = new ProfileTracker() {
                @Override
                protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                    fb_id = currentProfile.getId();
                    mProfileTracker.stopTracking();
                }
            };
            // no need to call startTracking() on mProfileTracker
            // because it is called by its constructor, internally.
        } else {
            Profile profile = Profile.getCurrentProfile();
            fb_id = profile.getId();
        }

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child("data").child(fb_id).getValue() == null) {
                    GpsTracker gpsTracker = new GpsTracker(choose.this);
                    double latitude = gpsTracker.getLatitude(); // 위도
                    double longitude = gpsTracker.getLongitude(); //경도
                    DatabaseReference mRef = firebaseDatabase.getReference().child("data").child(fb_id).child("0");
                    mRef.child("posX").setValue(String.valueOf(latitude));
                    mRef.child("posY").setValue(String.valueOf(longitude));
                    mRef.child("text").setValue("환영합니다. 낙서를 시작해보세요!");
                    mRef.push();
                }
                fbdata = dataSnapshot.getValue(fbData.class);
                if (fbdata.data == null)
                    return;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

    }
}
