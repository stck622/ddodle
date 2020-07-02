package kr.dgsw.test;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class activity_map extends AppCompatActivity
        implements OnMapReadyCallback {

    private GoogleMap mMap;

    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();

    fbData fbdata;
    private ProfileTracker mProfileTracker;
    String fb_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        if(Profile.getCurrentProfile() == null) {
            mProfileTracker = new ProfileTracker() {

                @Override
                protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                    fb_id = currentProfile.getId();
                    mProfileTracker.stopTracking();
                }
            };
        }
        else {
            Profile profile = Profile.getCurrentProfile();
            fb_id = profile.getId();
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        databaseReference.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                fbdata = dataSnapshot.getValue(fbData.class);
                mMap.clear();
                for(int i = 0; i < fbdata.data.get(fb_id).size();i++){
                    if(fbdata.data.get(fb_id).get(i) != null) {
                        MarkerOptions markerOptions = new MarkerOptions();
                        LatLng SEOUL = new LatLng(Double.parseDouble(fbdata.data.get(fb_id).get(i).get("posX")), Double.parseDouble(fbdata.data.get(fb_id).get(i).get("posY")));
                        markerOptions.position(SEOUL);
                        //markerOptions.title(String.valueOf(i));
                        Log.e("esaa",String.valueOf(i));
                        markerOptions.snippet(fbdata.data.get(fb_id).get(i).get("text"));
                        mMap.addMarker(markerOptions);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {

        mMap = googleMap;
        GpsTracker gpsTracker = new GpsTracker(activity_map.this);
        double latitude = gpsTracker.getLatitude(); // 위도
        double longitude = gpsTracker.getLongitude(); //경도

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude,longitude), 15));


    }

}