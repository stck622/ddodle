package kr.dgsw.test;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class Map extends Fragment implements OnMapReadyCallback {

    ViewGroup viewGroup;
    private MapView mapView = null;

    fbData fbdata;
    String fb_id;

    EditText editText;

    String source;

    int index = -1;

    static GoogleMap googleMap = null;

    static BitmapDrawable bitmapdraw;

    static Marker Pos_Marker = null;

    /* 페이지 리로드 */
    public void reload() {
        if (getFragmentManager() == null)
            return;
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.detach(Map.this).attach(Map.this).commit();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_map, container, false);

        /* 맵뷰 세팅 */
        mapView = (MapView) viewGroup.findViewById(R.id.map);
        mapView.getMapAsync(this);

        /* 푸시로 온경우 인덱스 받기 */
        Bundle bundle = getArguments();
        index = bundle.getInt("index", -1);
        bundle.putInt("index", -1);

        /* MY POS 비트맵 가져오기 */
        bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.icon);

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

        /* ID, DATA 받기 */
        fb_id = MyService.getfb_id();
        fbdata = MyService.getfbData();

        this.googleMap = googleMap; //구글맵 초기화

        /* 데이터 로딩 덜됐을 경우 리로딩*/

        if (fbdata == null) {
            MainActivity.fragment_map.reload();
            return;
        }

        if (fbdata.data == null) {
            MainActivity.fragment_map.reload();
            return;
        }



        for (int i = 1; i < fbdata.data.get(fb_id).size(); i++) {
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

        /* MY LOCATION 표시 */
        GpsTracker gpsTracker = new GpsTracker(getContext());
        double latitude = gpsTracker.getLatitude(); // 위도
        double longitude = gpsTracker.getLongitude(); //경도
        showMyPos(latitude, longitude);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 15));

        /* 푸시를 클릭한 맵뷰일 경우 */
        if (index != -1) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle(fbdata.data.get(fb_id).get(index).get("time"));
            editText = new EditText(getContext());
            editText.setText(fbdata.data.get(fb_id).get(index).get("text"));
            builder.setView(editText);

            source = editText.getText().toString();

            builder.setPositiveButton("OK", null);

            builder.setNegativeButton("수정", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (!source.trim().equals(editText.getText().toString().trim())) {
                        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference().child("data").child(fb_id).child(String.valueOf(index));
                        mRef.child("text").setValue(editText.getText().toString());
                        mRef.push();
                    }
                }
            });

            builder.setNeutralButton("삭제", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    FirebaseDatabase.getInstance().getReference().child("data").child(fb_id).child(String.valueOf(index)).removeValue();
                }
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();

            index = -1; //인텐트 데이터 초기화 (안하면 계속 마커 이동함)

        }

    }


    static void showMyPos(double latitude, double longitude) {

        if (Pos_Marker != null)
            Pos_Marker.remove();

        MarkerOptions markerOptions = new MarkerOptions();

        Bitmap b = bitmapdraw.getBitmap();
        Bitmap smallMarker = Bitmap.createScaledBitmap(b, 150, 150, false);
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(smallMarker));

        LatLng pos = new LatLng(latitude, longitude);
        markerOptions.position(pos);

        Pos_Marker = googleMap.addMarker(markerOptions);
    }


}