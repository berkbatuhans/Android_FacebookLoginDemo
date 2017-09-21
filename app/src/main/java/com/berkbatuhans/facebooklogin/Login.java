package com.berkbatuhans.facebooklogin;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.StrictMode;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.FacebookError;
import com.facebook.android.Util;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;


public class Login extends FragmentActivity {

    private LoginButton loginBtn;
    private Button postWallBtn;
    private Button getProfilDataBtn;
    private TextView userName;
    private UiLifecycleHelper uiHelper;

    private static String APP_ID = "1301760283283861"; //"Ornek API Uygulama" Facebook uygulamasının APP_ID yazdık
    private Facebook facebook;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //APP_ID yi Facebook sınıfına parametre olarak verdik
        facebook = new Facebook(APP_ID);

        uiHelper = new UiLifecycleHelper(this, statusCallback);
        uiHelper.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        //Arayüz nesnelerimizi tanımladık
        userName = (TextView) findViewById(R.id.user_name);
        loginBtn = (LoginButton) findViewById(R.id.fb_login_button);
        postWallBtn = (Button) findViewById(R.id.post_wall);
        getProfilDataBtn = (Button) findViewById(R.id.get_profil_data);

        //LoginButton sınıfı,Facebook' a login olunup, olunmadığının bilgisini tutan sınıftır.
        //setUserInfoChangedCallback metodu ile kullanıcının facebook hesabını giriş (login)kontrolunu sağlar
        loginBtn.setReadPermissions(Arrays.asList("email","user_birthday","user_location"));
        loginBtn.setUserInfoChangedCallback(new LoginButton.UserInfoChangedCallback() {
            @Override
            public void onUserInfoFetched(GraphUser user) {
                //user değiskeni doluysa kullanıcı giriş yapmış demektir.
                if (user != null) {
                    userName.setText("Hi, " + user.getName() + user.getId() + " " + user.getLocation());
                    Log.d("getId",user.toString());
                } else {
                    userName.setText("You are not logged.");
                }
            }
        });


        //Facebook kullanıcısı, duvarına herhangi bir yazı göndermek istediğinde postWallBtn click event i çalısır
        postWallBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postToWall();
            }
        });
        //Facebook kullanıcısının, profil bilgilerini getirmek istediğinde getProfilDataBtn click event i çalısır
        getProfilDataBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getProfileInformation();
            }
        });


        buttonsEnabled(false);
    }
    //Kullanıcının Session kontrolu yapılır.
    private Session.StatusCallback statusCallback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state,
                         Exception exception) {
            //Login durumu
            if (state.isOpened()) {
                buttonsEnabled(true);

            }
            //Logout durumu
            else if (state.isClosed()) {
                buttonsEnabled(false);

            }
        }
    };

    //Kullanıcı login ise postWallBtn ve getProfilDataBtn butonlarını aktif, logout ise pasif yaptık
    public void buttonsEnabled(boolean isEnabled) {
        postWallBtn.setEnabled(isEnabled);
        getProfilDataBtn.setEnabled(isEnabled);
    }



    @Override
    public void onResume() {
        super.onResume();
        uiHelper.onResume();
        buttonsEnabled(Session.getActiveSession().isOpened());
    }

    @Override
    public void onPause() {
        super.onPause();
        uiHelper.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        uiHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onSaveInstanceState(Bundle savedState) {
        super.onSaveInstanceState(savedState);
        uiHelper.onSaveInstanceState(savedState);
    }

    /**
     *Kullanıcının duvarına yazı göndermek sağlar
     */

    public void postToWall() {
        Bundle params = new Bundle();

        params.putString("link", "https://www.instagram.com/");
        params.putString("picture", "https://lh3.googleusercontent.com/aYbdIM1abwyVSUZLDKoE0CDZGRhlkpsaPOg9tNnBktUQYsXflwknnOn2Ge1Yr7rImGk=w300");
        params.putString("name", "your post title");
        params.putString("caption", "your subtitle");
        params.putString("description", "your message");
        facebook.dialog(this, "feed",params, new Facebook.DialogListener() {

            @Override
            public void onFacebookError(FacebookError e) {
            }

            @Override
            public void onError(DialogError e) {
            }

            @Override
            public void onComplete(Bundle values) {
            }

            @Override
            public void onCancel() {
            }
        });

    }

    /**
     * Kullanıcının profil bilgilerini getirir.
     */
    public void getProfileInformation() {
        TextView nameTxt=(TextView)findViewById(R.id.name);
        TextView usernameTxt=(TextView)findViewById(R.id.username);
        TextView genderTxt=(TextView)findViewById(R.id.gender);
        TextView localeTxt=(TextView)findViewById(R.id.locale);

        try {
            //StrictMode kullanarak,ağ erişiminin güvenli bir şekilde yapılmasını sağlıyoruz...
            StrictMode.ThreadPolicy policy = new
                    StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);

            //Kullanıcının user idsini kullanarak json string elde ettik
            JSONObject profile = Util.parseJson(facebook.request("1419359688181454"));
            //Json string parse yaparak, kullanıcı bilgilerini aldık
            JSONObject reader=new JSONObject(String.valueOf(profile));

            String mName = reader.getString("name");
            String gender = reader.getString("gender");
            String mUserName = reader.getString("username");
            String locale = reader.getString("locale");
            //ve değerleri TextView e atadık
            nameTxt.setText(mName);
            genderTxt.setText(gender);
            usernameTxt.setText(mUserName);
            localeTxt.setText(locale);

        } catch (FacebookError e) {

            e.printStackTrace();
        } catch (MalformedURLException e) {

            e.printStackTrace();
        } catch (JSONException e) {

            e.printStackTrace();
        } catch (IOException e) {

            e.printStackTrace();
        }

    }

}
