package com.example.test_quiz;

import android.app.Application;
import android.content.Context;

public class MyApplication extends Application { //Contextをアクティビティーのクラス以外からでも取得するための仕組みらしい
    private static Context context;
    public void onCreate(){
        super.onCreate();
        MyApplication.context = getApplicationContext();
    }

    public static Context getMyAppContext(){
        return MyApplication.context;
    }
}
