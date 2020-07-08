package kr.dgsw.test;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Write extends Fragment {

    ViewGroup viewGroup;

    fbData fbdata;
    String fb_id;

    BottomNavigationView bottomNavigationView;

    Write(BottomNavigationView bottomNavigationView) {
        this.bottomNavigationView = bottomNavigationView;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_write, container, false);

        viewGroup.findViewById(R.id.bt_write).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fb_id = MyService.getfb_id();
                fbdata = MyService.getfbData();
                GpsTracker gpsTracker = new GpsTracker(getContext());
                double latitude = gpsTracker.getLatitude(); // 위도
                double longitude = gpsTracker.getLongitude(); //경도
                DatabaseReference mRef = FirebaseDatabase.getInstance().getReference().child("data").child(fb_id).child(String.valueOf(fbdata.data.get(fb_id).size()));
                mRef.child("posX").setValue(String.valueOf(latitude));
                mRef.child("posY").setValue(String.valueOf(longitude));
                mRef.child("text").setValue(((EditText) viewGroup.findViewById(R.id.ed_write)).getText().toString());
                mRef.child("time").setValue(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date(System.currentTimeMillis())));
                mRef.push();
                ((EditText) viewGroup.findViewById(R.id.ed_write)).setText("");
                bottomNavigationView.setSelectedItemId(R.id.bottom_map);
            }
        });

        return viewGroup;
    }


}