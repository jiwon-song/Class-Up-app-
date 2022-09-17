package com.example.project01;

import static com.example.project01.common.CommonMethod.teacherDTO;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.project01.ATask.TLoginSelect;
import com.example.project01.DTO.StudentDTO;
import com.kakao.sdk.auth.model.OAuthToken;
import com.kakao.sdk.common.KakaoSdk;
import com.kakao.sdk.user.UserApiClient;
import com.navercorp.nid.NaverIdLoginSDK;
import com.navercorp.nid.oauth.NidOAuthLogin;
import com.navercorp.nid.oauth.OAuthLoginCallback;
import com.navercorp.nid.oauth.view.NidOAuthLoginButton;
import com.navercorp.nid.profile.NidProfileCallback;
import com.navercorp.nid.profile.data.NidProfileResponse;
import com.nhn.android.naverlogin.OAuthLogin;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutionException;

import kotlin.Unit;
import kotlin.jvm.functions.Function2;

public class TLogin extends AppCompatActivity {
    private static final String TAG = "TLogin";

    ImageView back;

    EditText etId, etPw;
    CheckBox auto;
    Button btnLogin;
    String state = "";

    SharedPreferences setting;

    SharedPreferences.Editor editor;


    // 네이버 로그인
    //client 정보
    private static String OAUTH_CLIENT_ID = "nSU3FcDBSR0dEiJT2SZA";
    private static String OAUTH_CLIENT_SECRET = "rS6EMiQkej";
    private static String OAUTH_CLIENT_NAME = "project";
    private static OAuthLogin mOAuthLoginInstance;
    private static Context mContext;



    // 로그인 화면 띄울시 dto를 null로 만들어서 문제 해결
    @Override
    protected void onResume() {
        super.onResume();

        if( teacherDTO != null){
            teacherDTO = null;
        }

    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tlogin);

        // 위험권한 물어보기 ( 발표시 주석 풀기 )
        // checkDangerousPermissions();


        // 카카오 로그인
        //getHashKey(); // 해쉬키 받아오기
        KakaoSdk.init(this,"eeaf3d6c1d83726a88804885c211cfa5");
        findViewById(R.id.kakaoLogin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
               // 카카오톡이 설치되어 있으면 카카오톡으로 로그인, 아니면 카카오계정으로 로그인
                if (UserApiClient.instance.isKakaoTalkLoginAvailable(context)) {
                    UserApiClient.instance.loginWithKakaoTalk(context) { token, error ->
                        if (error != null) {
                            Log.e(TAG, "카카오톡으로 로그인 실패", error)

                            // 사용자가 카카오톡 설치 후 디바이스 권한 요청 화면에서 로그인을 취소한 경우,
                            // 의도적인 로그인 취소로 보고 카카오계정으로 로그인 시도 없이 로그인 취소로 처리 (예: 뒤로 가기)
                            if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                                return@loginWithKakaoTalk
                            }

                            // 카카오톡에 연결된 카카오계정이 없는 경우, 카카오계정으로 로그인 시도
                            UserApiClient.instance.loginWithKakaoAccount(context, callback = callback)
                        } else if (token != null) {
                            Log.i(TAG, "카카오톡으로 로그인 성공 ${token.accessToken}")
                        }
                    }
                } else {
                    UserApiClient.instance.loginWithKakaoAccount(context, callback = callback)
                }
                */
                //lamda식 자바코드를 함수형으로 간편하게 줄여 사용한 것
                //(OAuthToken?, Throwable?) -> Unit
                Function2<OAuthToken, Throwable, Unit> callback = new Function2<OAuthToken, Throwable, Unit>() {
                    @Override
                    public Unit invoke(OAuthToken oAuthToken, Throwable throwable) {
                        if(oAuthToken != null) {
                            Log.d("토큰", "invoke: 받아옴");
                            kakao_profile();
                        }
                        if(throwable != null) {
                            Log.d("토큰", "invoke: 오류있음");
                        }
                        return null;
                    }
                };


                //카카오톡앱 설치 여부를 판단, 깔려있으면 카톡앱으로 인증
                if(UserApiClient.getInstance().isKakaoTalkLoginAvailable(TLogin.this)) {
                    UserApiClient.getInstance().loginWithKakaoTalk(TLogin.this, callback = callback);
                }else {
                    UserApiClient.getInstance().loginWithKakaoAccount(TLogin.this, callback = callback);
                }
            }
        });




        //네이버 로그인 초기화
        NaverIdLoginSDK.INSTANCE.initialize(
                this,OAUTH_CLIENT_ID,
                OAUTH_CLIENT_SECRET,
                OAUTH_CLIENT_NAME);

        NidOAuthLoginButton btn_naver = findViewById(R.id.btn_naver);
        btn_naver.setOAuthLoginCallback(new OAuthLoginCallback() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "onSuccess: " + NaverIdLoginSDK.INSTANCE.getAccessToken());
                Log.d(TAG, "onSuccess: " + NaverIdLoginSDK.INSTANCE.getRefreshToken());
                getNaverProfile();

            }

            @Override
            public void onFailure(int i, String s) {
                Log.d(TAG, "onFailure: " + s);
            }

            @Override
            public void onError(int i, String s) {
                Log.d(TAG, "onError: " + s);
            }
        }); // 네이버 로그인




        // 로그인 화면 띄울시 자동으로 dto 를 null로 만듬
        StudentDTO studentDTO = null;

        // 배경지정
        back = findViewById(R.id.back);
        Glide.with(this)
                .load(R.drawable.bg1)
                .centerCrop()
                .into(back); 			// 이미지를 넣을 이미지뷰

        // 찾기
        etId = findViewById(R.id.etId);
        etPw = findViewById(R.id.etPw);
        btnLogin = findViewById(R.id.btnLogin);






        /////////////////자동로그인//////////////////////////////////////////////////
        // 자동로그인 체크박스 찾기
        auto = findViewById(R.id.auto);
        setting = getSharedPreferences("setting", 0);
        editor= setting.edit();

        // checkBox 자동로그인 체크시
        auto.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){

                    String ID = etId.getText().toString();

                    String PW = etPw.getText().toString();

                    editor.putString("ID", ID);

                    editor.putString("PW", PW);

                    editor.putBoolean("Auto_Login_enabled", true);

                    editor.commit();

                }else{

                    editor.clear();

                    editor.commit();

                }
            }
        });

        if(setting.getBoolean("Auto_Login_enabled", false)){

            etId.setText(setting.getString("ID", ""));

            etPw.setText(setting.getString("PW", ""));

            auto.setChecked(true);

        }


        //처음에는 SharedPreferences에 아무런 정보도 없으므로 값을 저장할 키들을 생성한다.
        // getString의 첫 번째 인자는 저장될 키, 두 번쨰 인자는 값입니다.
        // 첨엔 값이 없으므로 키값은 원하는 것으로 하시고 값을 null을 줍니다.
        String loginId = setting.getString("ID",null);
        String loginPwd = setting.getString("PW",null);


        if (loginId != null && loginPwd != null) {
            Toast.makeText(TLogin.this, loginId + "님 자동로그인 입니다.", Toast.LENGTH_SHORT).show();


            // editText 에 적은 아이이디와 패스워드를 가져온다
            if(etId.getText().toString().length()>0 && etPw.getText().toString().length() > 0){
                String teacher_id = etId.getText().toString();
                String teacher_pw = etPw.getText().toString();

                // 서버로 데이터를 보낸다 :  AsyncTask를 상속받는 java파일을 만든다
                TLoginSelect loginSelect = new TLoginSelect(teacher_id, teacher_pw);
                try {
                    state = loginSelect.execute().get();

                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // loginSelect에서 loginDTO를 채웠으면 로그인이 된거고
                // loginDTO가 null이면 id나 pw 가 틀린것이다
                if(teacherDTO != null){
                    Toast.makeText(TLogin.this, "로그인이 잘 되었네", Toast.LENGTH_SHORT).show();

                    // 로그인이 되었으므로 로그인화면 없애고 메인화면을 부른다
                    Intent intent = new Intent(TLogin.this, TMain.class);
                    startActivity(intent);
                    finishAffinity();

                }else{  //loginDTO가 null 이면
                    Toast.makeText(TLogin.this, "아이디나 비밀번호가 맞지 않습니다", Toast.LENGTH_SHORT).show();
                    etId.setText("");
                    etPw.setText("");
                    etId.requestFocus();

                }



            }else{  // 에디트텍스트에 내용이 없을 때
                Toast.makeText(TLogin.this, "아이디나 비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show();
                etId.setText("");
                etPw.setText("");
                etId.requestFocus();
                return;
            }

        }


        // 로그인 버튼 클릭시 ( 값 던짐 )
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // editText 에 적은 아이이디와 패스워드를 가져온다
                if(etId.getText().toString().length()>0 && etPw.getText().toString().length() > 0){
                    String teacher_id = etId.getText().toString();
                    String teacher_pw = etPw.getText().toString();

                    // 서버로 데이터를 보낸다 :  AsyncTask를 상속받는 java파일을 만든다
                    TLoginSelect loginSelect = new TLoginSelect(teacher_id, teacher_pw);
                    try {
                        state = loginSelect.execute().get();

                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    // loginSelect에서 loginDTO를 채웠으면 로그인이 된거고
                    // loginDTO가 null이면 id나 pw 가 틀린것이다
                    if(teacherDTO != null){
                        Toast.makeText(TLogin.this, "로그인이 잘 되었네", Toast.LENGTH_SHORT).show();

                        // 로그인이 되었으므로 로그인화면 없애고 메인화면을 부른다
                        Intent intent = new Intent(TLogin.this, TMain.class);
                        startActivity(intent);
                        finishAffinity();

                    }else{  //loginDTO가 null 이면
                        Toast.makeText(TLogin.this, "아이디나 비밀번호가 맞지 않습니다", Toast.LENGTH_SHORT).show();
                        etId.setText("");
                        etPw.setText("");
                        etId.requestFocus();

                    }



                }else{  // 에디트텍스트에 내용이 없을 때
                    Toast.makeText(TLogin.this, "아이디나 비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show();
                    etId.setText("");
                    etPw.setText("");
                    etId.requestFocus();
                    return;
                }

            } // onClick
        });  // setOnListener





    } // onCreate



    // 네이버 로그인
    public void getNaverProfile(){ //<- 억세스 토큰 O일때만 성공함.
        NidOAuthLogin authLogin = new NidOAuthLogin();
        authLogin.callProfileApi(new NidProfileCallback<NidProfileResponse>() {
            @Override
            public void onSuccess(NidProfileResponse nidProfileResponse) {

                Log.d(TAG, "onSuccess: " + nidProfileResponse.getProfile().getId());
                Log.d(TAG, "onSuccess: " + nidProfileResponse.getProfile().getEmail());
                Log.d(TAG, "onSuccess: " + nidProfileResponse.getProfile().getMobile());
                Log.d(TAG, "onSuccess: " + nidProfileResponse.getProfile().getName());


                String id = nidProfileResponse.getProfile().getId();

                // 서버로 데이터를 보낸다 :  AsyncTask를 상속받는 java파일을 만든다
                TLoginSelect loginSelect = new TLoginSelect(id, "social");
                try {
                    state = loginSelect.execute().get();

                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // loginSelect에서 loginDTO를 채웠으면 로그인이 된거고
                // loginDTO가 null이면 id나 pw 가 틀린것이다
                if(teacherDTO != null){
                    Toast.makeText(TLogin.this, "로그인이 잘 되었네", Toast.LENGTH_SHORT).show();

                    // 로그인이 되었으므로 로그인화면 없애고 메인화면을 부른다
                    Intent intent = new Intent(TLogin.this, TMain.class);
                    startActivity(intent);
                    finishAffinity();

                }else{  //loginDTO가 null 이면
                    Toast.makeText(TLogin.this, "아이디나 비밀번호가 맞지 않습니다", Toast.LENGTH_SHORT).show();
                    etId.setText("");
                    etPw.setText("");
                    etId.requestFocus();

                }


            }

            @Override
            public void onFailure(int i, String s) {
                Log.d(TAG, "onFailure: " + s);
            }

            @Override
            public void onError(int i, String s) {
                Log.d(TAG, "onFailure: " + s);
            }
        });

    }


    // 카카오 로그인 : 해쉬키 받아오기
    // 만약 다른 컴퓨터에서 작업을 할려면 키를 받아서 카카오프로젝트에 추가해주어야 함
    private void getHashKey(){
        PackageInfo packageInfo = null;
        try {
            packageInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (packageInfo == null)
            Log.e("KeyHash", "KeyHash:null");

        for (Signature signature : packageInfo.signatures) {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            } catch (NoSuchAlgorithmException e) {
                Log.e("KeyHash", "Unable to get MessageDigest. signature=" + signature, e);
            }
        }
    }


    public void kakao_profile() {
        UserApiClient.getInstance().me((user, throwable) -> {
            if(throwable != null) {
                //오류났을 때 어떤 오류인지 코드로 줌 KOE + 숫자(무지 나옴)

            }else {
                Log.d("카카오", "kakao_profile: " + user.getKakaoAccount().getProfile().getNickname()); // 홍
                Log.d("카카오", "kakao_profile: " + user.getId());
                Log.d("카카오", "kakao_profile: " + user.getKakaoAccount().getName());
            }

            // 서버로 데이터를 보낸다 :  AsyncTask를 상속받는 java파일을 만든다
            TLoginSelect loginSelect = new TLoginSelect("" + user.getId(), "social");
            try {
                state = loginSelect.execute().get();

            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // loginSelect에서 loginDTO를 채웠으면 로그인이 된거고
            // loginDTO가 null이면 id나 pw 가 틀린것이다
            if(teacherDTO != null){
                Toast.makeText(TLogin.this, "로그인이 잘 되었네", Toast.LENGTH_SHORT).show();

                // 로그인이 되었으므로 로그인화면 없애고 메인화면을 부른다
                Intent intent = new Intent(TLogin.this, TMain.class);
                startActivity(intent);
                finishAffinity();

            }else{  //loginDTO가 null 이면
                Toast.makeText(TLogin.this, "아이디나 비밀번호가 맞지 않습니다", Toast.LENGTH_SHORT).show();
            }





            return null;
        });

        /*UserApiClient.getInstance().me(new Function2<User, Throwable, Unit>() {
            @Override
            public Unit invoke(User user, Throwable throwable) {
                return null;
            }
        });*/
    }



    // 위험권한 : 실행시 허용여부를 다시 물어봄   ///////////////////////////////////////////////////////
    private void checkDangerousPermissions() {
        String[] permissions = {
                // 위험권한 내용 : 메니페스트에 권한을 여기에 적음

                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_MEDIA_LOCATION,
                Manifest.permission.CAMERA
        };

        int permissionCheck = PackageManager.PERMISSION_GRANTED;
        for (int i = 0; i < permissions.length; i++) {
            permissionCheck = ContextCompat.checkSelfPermission(this, permissions[i]);
            if (permissionCheck == PackageManager.PERMISSION_DENIED) {
                break;
            }
        }

        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "권한 있음", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "권한 없음", Toast.LENGTH_LONG).show();

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0])) {
                Toast.makeText(this, "권한 설명 필요함.", Toast.LENGTH_LONG).show();
            } else {
                ActivityCompat.requestPermissions(this, permissions, 1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, permissions[i] + " 권한이 승인됨.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, permissions[i] + " 권한이 승인되지 않음.", Toast.LENGTH_LONG).show();
                }
            }
        }
    }



}//class



