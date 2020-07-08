package kr.dgsw.test;


import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

public class Profile extends Fragment {

    ViewGroup viewGroup;

    CallbackManager callbackManager; //페이스북 로그인 콜백

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_profile,container,false);

        AccessTokenTracker accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken,
                                                       AccessToken currentAccessToken) {
                if (currentAccessToken == null) {
                    Log.d("essasse", "onLogout catched");
                }
            }
        };
        accessTokenTracker.startTracking();

        /** 페이스북 로그인 **/
        callbackManager = CallbackManager.Factory.create();

        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {

                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        Log.e("e", "로그인 성공");
                        /** 포그라운드 서비스 동작 **/
                        if (!MyService.isServiceRunningCheck(getContext())) {
                            if (Build.VERSION.SDK_INT >= 26) { //안드로이드 버전 체크
                                getActivity().startForegroundService(new Intent(getContext(),MyService.class));
                            } else {
                                getActivity().startService(new Intent(getContext(),MyService.class));
                            }
                        }
                    }

                    @Override
                    public void onCancel() {
                        Log.e("e", "로그인 캔슬");
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        Log.e("e", "로그인 오류");
                    }



                });

        return viewGroup;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }


}