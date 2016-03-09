package com.example.linweijie.myfacebook;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import org.json.JSONObject;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements FacebookCallback<LoginResult>{

    private CallbackManager callbackManager;
    private AccessToken accessToken;
    private boolean isLoggedIn = false; // by default assume not logged in

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Initialize the SDK before executing any other operations, especially, if you're using Facebook UI elements.
        FacebookSdk.sdkInitialize(getApplicationContext());

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //宣告callback Manager
        callbackManager = CallbackManager.Factory.create();

        //幫 LoginManager 增加callback function
        LoginManager.getInstance().registerCallback(callbackManager, this);

        //自定義按鈕
        Button bt_login = (Button)findViewById(R.id.bt_login);
        bt_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginManager.getInstance().logInWithReadPermissions(MainActivity.this, Arrays.asList("public_profile", "user_friends"));
            }
        });

        LoginManager.getInstance().logOut();
        facebookPost();
    }

    private void facebookPost() {
        //check login
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if (accessToken == null) {
            Log.d("FB", ">>>" + "Signed Out");
        } else {
            Log.d("FB", ">>>" + "Signed In");
        }
    }

    /**
     *
     * */
    @Override
    protected void onResume() {
        super.onResume();

        // Logs 'install' and 'app activate' App Events.
        AppEventsLogger.activateApp(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Logs 'app deactivate' App Event.
        AppEventsLogger.deactivateApp(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode,
                resultCode, data);
    }

    /**
     *
     * */
    @Override
    public void onSuccess(LoginResult loginResult) {
        //accessToken之後或許還會用到 先存起來

        accessToken = loginResult.getAccessToken();

        Log.d("FB","access token got.");

        //send request and call graph api

        GraphRequest request = GraphRequest.newMeRequest(
                accessToken,
                new GraphRequest.GraphJSONObjectCallback() {

                    //當RESPONSE回來的時候
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {

                        //讀出姓名 ID FB個人頁面連結
                        Log.d("FB","complete");
                        Log.d("FB",object.optString("name"));
                        Log.d("FB",object.optString("link"));
                        Log.d("FB",object.optString("id"));

                    }
                });

        //包入你想要得到的資料 送出request

        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,link");
        request.setParameters(parameters);
        request.executeAsync();
    }

    @Override
    public void onCancel() {
        // App code
        Log.d("FB","CANCEL");
    }

    @Override
    public void onError(FacebookException error) {
        // App code
        Log.d("FB",error.toString());
    }
}
