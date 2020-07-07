package kr.dgsw.test;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class activity_map extends AppCompatActivity
        implements OnMapReadyCallback {

    private GoogleMap mMap;

    fbData fbdata;
    String fb_id;

    boolean noti_flag = false;
    int index;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        intentData intentData  = (intentData)getIntent().getSerializableExtra("index");
        if(intentData != null){
            noti_flag = true;
            index = intentData.index;
        }

    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        mMap = googleMap;

        fb_id = MyService.getfb_id();
        fbdata = MyService.getfbData();

        GpsTracker gpsTracker = new GpsTracker(activity_map.this);
        double latitude = gpsTracker.getLatitude(); // 위도
        double longitude = gpsTracker.getLongitude(); //경도

        for(int i = 0; i < fbdata.data.get(fb_id).size();i++){
            if((fbdata.data.get(fb_id).get(i) != null) && (fbdata.data.get(fb_id).get(i).get("posX") != null) && (fbdata.data.get(fb_id).get(i).get("posY") != null) && (fbdata.data.get(fb_id).get(i).get("text") != null)) {
                MarkerOptions markerOptions = new MarkerOptions();
                LatLng pos = new LatLng(Double.parseDouble(fbdata.data.get(fb_id).get(i).get("posX")), Double.parseDouble(fbdata.data.get(fb_id).get(i).get("posY")));
                markerOptions.position(pos);
                markerOptions.title(String.valueOf(i));
                markerOptions.snippet(fbdata.data.get(fb_id).get(i).get("text"));

                CircleOptions circle = new CircleOptions().center(pos) //원점
                        .radius(500)      //반지름 단위 : m
                        .strokeWidth(0f)  //선너비 0f : 선없음
                        .fillColor(Color.parseColor("#880000ff")); //배경색

                mMap.addMarker(markerOptions);
                mMap.addCircle(circle);

            }
        }

        MarkerOptions markerOptions = new MarkerOptions();

        BitmapDrawable bitmapdraw=(BitmapDrawable)getResources().getDrawable(R.drawable.icon);
        Bitmap b=bitmapdraw.getBitmap();
        Bitmap smallMarker = Bitmap.createScaledBitmap(b, 150, 150, false);
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(smallMarker));

        LatLng pos = new LatLng(latitude,longitude);
        markerOptions.position(pos);

        mMap.addMarker(markerOptions);

        if(noti_flag){
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Double.parseDouble(fbdata.data.get(fb_id).get(index).get("posX")), Double.parseDouble(fbdata.data.get(fb_id).get(index).get("posY"))), 15));
        } else {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 15));
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

}