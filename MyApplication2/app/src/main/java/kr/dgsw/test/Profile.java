package kr.dgsw.test;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Profile extends Fragment {

    ViewGroup viewGroup;

    ProfileTracker mProfileTracker;
    com.facebook.Profile profile;
    CallbackManager callbackManager;

    TextView tv_name;
    TextView tv_id;

    /* 프래그먼트 리로드 */
    public void reload() {
        if (getFragmentManager() == null)
            return;
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.detach(Profile.this).attach(Profile.this).commit();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_profile, container, false);

        tv_name = viewGroup.findViewById(R.id.tv_name);
        tv_id = viewGroup.findViewById(R.id.tv_id);
        TextView tv_write_cnt = viewGroup.findViewById(R.id.tv_write_cnt);
        TextView tv_today = viewGroup.findViewById(R.id.tv_today);
        ImageView img_view = viewGroup.findViewById(R.id.img_profile);

        /* 프로필 페이지 값 세팅 */
        if (MyService.getLoginFlag()) {
            if (com.facebook.Profile.getCurrentProfile() == null) {
                mProfileTracker = new ProfileTracker() {
                    @Override
                    protected void onCurrentProfileChanged(com.facebook.Profile oldProfile, com.facebook.Profile currentProfile) {
                        profile = currentProfile;
                        mProfileTracker.stopTracking();
                        tv_name.setText(profile.getName());
                        tv_id.setText(profile.getId());
                    }
                };
            } else {
                profile = com.facebook.Profile.getCurrentProfile();
                tv_name.setText(profile.getName());
                tv_id.setText(profile.getId());
            }

            if (MyService.profile_img != null) {
                img_view.setImageBitmap(MyService.getProfile_img());
            }

            if (MyService.getfbData() != null)
                tv_write_cnt.setText(MyService.getfbData().data.get(profile.getId()).size() - 1 + "");
            else
                tv_write_cnt.setText("load");

            if (MyService.getfbData() != null)
                tv_today.setText(MyService.today + "");
            else
                tv_write_cnt.setText("load");

        }

        AccessTokenTracker accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken,
                                                       AccessToken currentAccessToken) {

                AccessToken accessToken = currentAccessToken.getCurrentAccessToken();
                boolean isLoggedIn = accessToken != null && !accessToken.isExpired();
                if (!isLoggedIn) {
                    getActivity().stopService(new Intent(getContext(), MyService.class));
                    Toast.makeText(getActivity(), "로그아웃", Toast.LENGTH_LONG).show();
                    MyService.profile_img = null;
                } else {
                    Toast.makeText(getActivity(), "로그인 성공", Toast.LENGTH_LONG).show();
                }
                reload();
            }
        };
        accessTokenTracker.startTracking();

        /** 페이스북 로그인 **/
        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {

                    @Override
                    public void onSuccess(LoginResult loginResult) {

                        if (com.facebook.Profile.getCurrentProfile() == null) {
                            mProfileTracker = new ProfileTracker() {
                                @Override
                                protected void onCurrentProfileChanged(com.facebook.Profile oldProfile, com.facebook.Profile currentProfile) {
                                    profile = currentProfile;
                                    new MyService.task(profile).execute(); //서비스 시작
                                    mProfileTracker.stopTracking();
                                }
                            };
                        } else {
                            profile = com.facebook.Profile.getCurrentProfile();
                            new MyService.task(profile).execute(); //서비스 시작
                        }

                        /** 포그라운드 서비스 동작 **/
                        if (!MyService.isServiceRunningCheck(getContext())) {
                            if (Build.VERSION.SDK_INT >= 26) { //안드로이드 버전 체크
                                getActivity().startForegroundService(new Intent(getContext(), MyService.class));
                            } else {
                                getActivity().startService(new Intent(getContext(), MyService.class));
                            }
                        }
                    }

                    @Override
                    public void onCancel() {
                    }

                    @Override
                    public void onError(FacebookException exception) {
                    }


                });

        return viewGroup;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data); //페이스북 로그인 콜백 등록
    }

}