package com.example.linweijie.myfacebook;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

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
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.model.ShareVideo;
import com.facebook.share.model.ShareVideoContent;
import com.facebook.share.widget.ShareButton;
import com.facebook.share.widget.ShareDialog;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements FacebookCallback<LoginResult>{

    private CallbackManager callbackManager;
    private ShareDialog shareDialog;
    private AccessToken accessToken;
    private Button bt_login, bt_share;

    private ImageView iv_picture;
    private TextView tv_id, tv_name, tv_friends, tv_link;

    private String usr_picture_is_silhouette = "";
    private String usr_picture_url = "";
    private String usr_id = "";
    private String usr_name = "";
    private String usr_friends = "";
    private String usr_link = "";

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
        bt_login = (Button)findViewById(R.id.bt_login);
        bt_share = (Button)findViewById(R.id.bt_share);
        bt_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (facebookPost()) {
                    LoginManager.getInstance().logOut();
                    bt_login.setBackground(getDrawable(R.drawable.selector_button1));
                    bt_login.setText("Login with facebook");
                    Log.d("FB", ">>>" + "Signed Out");

                    resetProfileData();
                    setProfileData();
                } else {
                    LoginManager.getInstance().logInWithReadPermissions(MainActivity.this, Arrays.asList("public_profile", "user_friends"));
                }
            }
        });
        bt_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (facebookPost()){
                    Log.d("FB", "Share massage");

                    if (ShareDialog.canShow(ShareLinkContent.class)) {
                        //Links
//                        ShareLinkContent content = new ShareLinkContent.Builder()
//                                .setContentTitle("Hello Facebook")
//                                .setContentDescription(
//                                        "The 'Hello Facebook' sample  showcases simple Facebook integration")
//                                .setContentUrl(Uri.parse("http://developers.facebook.com/android"))
//                                .build();

                        //Photos
                        Bitmap image = BitmapFactory.decodeResource(getResources(), R.drawable.original);
                        List<SharePhoto> photos = new ArrayList<SharePhoto>();
                        for (int i=0; i<4; ++i){
                            SharePhoto photo = new SharePhoto.Builder()
                                    .setBitmap(image)
                                    .build();
                            photos.add(photo);
                        }
                        SharePhotoContent content = new SharePhotoContent.Builder()
                                .addPhotos(photos)
                                .build();

                        //Videos
//                        Uri videoFileUri = ...;
//                        ShareVideo video = new ShareVideo.Builder()
//                                .setLocalUrl(videoFileUri)
//                                .build();
//                        ShareVideoContent content = new ShareVideoContent.Builder()
//                                .setVideo(video)
//                                .build();

                        shareDialog.show(content);
                    }
                }
            }
        });
        //檢查是否登入過
        if (facebookPost()){
            Log.d("FB", ">>>" + "Signed In");
            LoginManager.getInstance().logInWithReadPermissions(MainActivity.this, Arrays.asList("public_profile", "user_friends"));
            bt_login.setBackground(getDrawable(R.drawable.selector_button2));
            bt_login.setText("Logout");
        } else {
            Log.d("FB", ">>>" + "Signed Out");
            bt_login.setBackground(getDrawable(R.drawable.selector_button1));
            bt_login.setText("Login with facebook");
        }

        //設定使用者資料
        setProfileData();

        //share message
        shareDialog = new ShareDialog(this);
        shareDialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() {
            @Override
            public void onSuccess(Sharer.Result result) {
                Log.d("FB", "Share Success");
            }

            @Override
            public void onCancel() {
                Log.d("FB", "Share Cancel");
            }

            @Override
            public void onError(FacebookException error) {
                Log.d("FB", "Share Error");
            }
        });
    }

    private void resetProfileData(){
        usr_picture_is_silhouette = "";
        usr_picture_url = "";
        usr_id = "";
        usr_name = "";
        usr_friends = "";
        usr_link = "";
    }
    private void getProfileData(JSONObject object){
        try {
            JSONObject picture = object.getJSONObject("picture");
            usr_picture_is_silhouette = picture.getJSONObject("data").getString("is_silhouette");
            usr_picture_url = picture.getJSONObject("data").getString("url");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        usr_id = object.optString("id");
        usr_name = object.optString("name");

        try {
            JSONObject friends = object.getJSONObject("friends");
            usr_friends = friends.getJSONObject("summary").getString("total_count");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        usr_link = object.optString("link");
    }
    private void setProfileData(){
        iv_picture = (ImageView)findViewById(R.id.iv_picture);
        tv_id = (TextView)findViewById(R.id.tv_id);
        tv_name = (TextView)findViewById(R.id.tv_name);
        tv_friends = (TextView)findViewById(R.id.tv_friends);
        tv_link = (TextView)findViewById(R.id.tv_link);

        if (!usr_picture_url.equals("")){
            Picasso.with(this).load(usr_picture_url).into(iv_picture);
        } else {
            iv_picture.setImageDrawable(getResources().getDrawable(R.drawable.original));
        }
        tv_id.setText(usr_id);
        tv_name.setText(usr_name);
        tv_friends.setText(usr_friends);
        tv_link.setText(usr_link);

        tv_link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!usr_link.equals("")){
                    if (!usr_link.startsWith("http://") && !usr_link.startsWith("https://"))
                        usr_link = "http://" + usr_link;

                    Uri uriUrl = Uri.parse(usr_link);
                    Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
                    startActivity(launchBrowser);
                }
            }
        });
    }

    private boolean facebookPost() {
        //check login
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if (accessToken == null) {
            return false;
        }
        return true;
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

                        //讀出姓名 ID FB個人頁面連結等資訊
                        Log.d("FB","complete");
                        getProfileData(object);
                        setProfileData();
                    }
                });

        //包入你想要得到的資料 送出request
        Bundle parameters = new Bundle();
        parameters.putString("fields", "picture, id, name, friends, link");
        request.setParameters(parameters);
        request.executeAsync();


        // 變更按鈕
        bt_login.setBackground(getDrawable(R.drawable.selector_button2));
        bt_login.setText("Logout");
        Log.d("FB", ">>>" + "Signed In");
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
