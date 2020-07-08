package kr.dgsw.test;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Map extends Fragment implements OnMapReadyCallback {

    ViewGroup viewGroup;
    private MapView mapView = null;

    fbData fbdata;
    String fb_id;

    EditText editText;

    String source;

    public void reload() {
        if(getFragmentManager() == null)
            return;
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.detach(Map.this).attach(Map.this).commit();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_map, container, false);

        mapView = (MapView) viewGroup.findViewById(R.id.map);
        mapView.getMapAsync(this);


        return viewGroup;
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onLowMemory();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (mapView != null) {
            mapView.onCreate(savedInstanceState);
        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        fb_id = MyService.getfb_id();
        fbdata = MyService.getfbData();

        GpsTracker gpsTracker = new GpsTracker(getContext());
        double latitude = gpsTracker.getLatitude(); // 위도
        double longitude = gpsTracker.getLongitude(); //경도

        for (int i = 0; i < fbdata.data.get(fb_id).size(); i++) {
            if ((fbdata.data.get(fb_id).get(i) != null) && (fbdata.data.get(fb_id).get(i).get("posX") != null) && (fbdata.data.get(fb_id).get(i).get("posY") != null) && (fbdata.data.get(fb_id).get(i).get("text") != null)) {
                MarkerOptions markerOptions = new MarkerOptions();
                LatLng pos = new LatLng(Double.parseDouble(fbdata.data.get(fb_id).get(i).get("posX")), Double.parseDouble(fbdata.data.get(fb_id).get(i).get("posY")));
                markerOptions.position(pos);
                markerOptions.title(fbdata.data.get(fb_id).get(i).get("time"));
                markerOptions.snippet("클릭해서 내용을 확인하세요!");

                CircleOptions circle = new CircleOptions().center(pos) //원점
                        .radius(500)      //반지름 단위 : m
                        .strokeWidth(0f)  //선너비 0f : 선없음
                        .fillColor(Color.parseColor("#880000ff")); //배경색

                googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                    @Override
                    public void onInfoWindowClick(final Marker marker) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setTitle(fbdata.data.get(fb_id).get((int) marker.getTag()).get("time"));
                        editText = new EditText(getContext());
                        editText.setText(fbdata.data.get(fb_id).get((int) marker.getTag()).get("text"));
                        builder.setView(editText);

                        source = editText.getText().toString();

                        builder.setPositiveButton("OK", null);

                        builder.setNegativeButton("수정", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (!source.trim().equals(editText.getText().toString().trim())) {
                                    DatabaseReference mRef = FirebaseDatabase.getInstance().getReference().child("data").child(fb_id).child(String.valueOf(marker.getTag()));
                                    mRef.child("text").setValue(editText.getText().toString());
                                    mRef.push();
                                }
                            }
                        });

                        builder.setNeutralButton("삭제", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                FirebaseDatabase.getInstance().getReference().child("data").child(fb_id).child(String.valueOf(marker.getTag())).removeValue();
                            }
                        });
                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();
                    }
                });

                googleMap.addMarker(markerOptions).setTag(i);
                googleMap.addCircle(circle);

            }
        }

        MarkerOptions markerOptions = new MarkerOptions();

        BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.icon);
        Bitmap b = bitmapdraw.getBitmap();
        Bitmap smallMarker = Bitmap.createScaledBitmap(b, 150, 150, false);
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(smallMarker));

        LatLng pos = new LatLng(latitude, longitude);
        markerOptions.position(pos);

        googleMap.addMarker(markerOptions);

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 15));

    }

}