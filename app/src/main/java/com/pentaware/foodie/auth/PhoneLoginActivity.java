package com.pentaware.foodie.auth;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.pentaware.foodie.R;
import com.pentaware.foodie.baseactivity.HomeActivity;
import com.pentaware.foodie.models.Address;
import com.pentaware.foodie.models.User;
import com.pentaware.foodie.utils.CommonVariables;
import com.pentaware.foodie.utils.Constants;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import pl.droidsonroids.gif.GifImageView;

public class PhoneLoginActivity extends AppCompatActivity {
    private static final String TAG = "PhoneLoginyyy";

    private EditText editTextPhoneNumber;
    private EditText txtOTP1, txtOTP2, txtOTP3, txtOTP4, txtOTP5, txtOTP6;
    private Button btnGenerateOTP;
    private LinearLayout llPhoneNumber, llEnterOtp;
    private ImageView imgBack;

    private FirebaseAuth mAuth;
    private ProgressBar progressBar;
    FirebaseFirestore db;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallBack;
    private String m_sVerificationId;
    CountDownTimer aTimer;

    private String receivedPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);
        Objects.requireNonNull(getSupportActionBar()).hide();


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//            window.setStatusBarColor(getResources().getColor(R.color.brown_300));
        }

        int flags = this.getWindow().getDecorView().getSystemUiVisibility();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            flags = flags ^ View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        }
        this.getWindow().getDecorView().setSystemUiVisibility(flags);

        FirebaseApp.initializeApp(this);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        editTextPhoneNumber = findViewById(R.id.edit_text_phone_number);

        txtOTP1 = findViewById(R.id.txt_otp_1);
        txtOTP2 = findViewById(R.id.txt_otp_2);
        txtOTP3 = findViewById(R.id.txt_otp_3);
        txtOTP4 = findViewById(R.id.txt_otp_4);
        txtOTP5 = findViewById(R.id.txt_otp_5);
        txtOTP6 = findViewById(R.id.txt_otp_6);

        progressBar = findViewById(R.id.progress_bar);
        btnGenerateOTP = findViewById(R.id.btn_generate_otp);
        llPhoneNumber = findViewById(R.id.ll_phone_number);
        llEnterOtp = findViewById(R.id.ll_enter_otp);
        imgBack = findViewById(R.id.img_back);

        receivedPhone = getIntent().getStringExtra(Constants.EXTRA_PHONE_NUMBER);

        initiatePhoneLoginSettings();

        if(receivedPhone != null && !receivedPhone.isEmpty()) {
            generateOtp(receivedPhone);
            llPhoneNumber.setVisibility(View.GONE);
        }

        btnGenerateOTP.setOnClickListener(view -> {
            String phone = editTextPhoneNumber.getText().toString();

            if(!phone.isEmpty()) {
                generateOtp(phone);
            } else {
                Toast.makeText(this, "Please provide phone number", Toast.LENGTH_SHORT).show();
            }
        });

        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(aTimer != null) {
                    aTimer.cancel();
                }

                finish();
            }
        });
    }

    private void generateOtp(String phone) {
        Log.d(TAG, "Phone Number: " + phone);
        llEnterOtp.setVisibility(View.VISIBLE);

        btnGenerateOTP.setEnabled(false);
        startTimer();
        CommonVariables.phone = phone;
        String sPhone = "+91" + phone;

        Log.d(TAG, "Phone Number with country code: " + sPhone);
        sendVerificationCode(sPhone);
        txtOTP1.requestFocus();
        Toast.makeText(this, "OTP Sent", Toast.LENGTH_SHORT).show();
    }

    private void startTimer() {
        aTimer = new CountDownTimer(60000, 1000) {
            public void onTick(long millisUntilFinished) {
                btnGenerateOTP.setText("Time Remaining -> " + millisUntilFinished / 1000);
            }

            public void onFinish() {
                btnGenerateOTP.setEnabled(true);
                btnGenerateOTP.setText("Generate OTP");
            }
        }.start();
    }

    public void sendVerificationCode(String sPhone) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                sPhone,
                60,
                TimeUnit.SECONDS,
                this,
                mCallBack
        );
    }

    private void initiatePhoneLoginSettings() {

        TelephonyManager tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        String countryCodeValue = tm.getNetworkCountryIso();

        editTextPhoneNumber.requestFocus();

        txtOTP1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String sText = s.toString();
                if (sText.length() == 1) {
                    txtOTP2.requestFocus();
                }
            }
        });

        txtOTP2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String sText = s.toString();
                if (sText.length() == 1) {
                    txtOTP3.requestFocus();
                }

                if(sText.length() == 0) {
                    txtOTP1.requestFocus();
                }
            }
        });

        txtOTP3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String sText = s.toString();
                if (sText.length() == 1) {
                    txtOTP4.requestFocus();
                }

                if(sText.length() == 0) {
                    txtOTP2.requestFocus();
                }
            }
        });

        txtOTP4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String sText = s.toString();
                if (sText.length() == 1) {
                    txtOTP5.requestFocus();
                }

                if(sText.length() == 0) {
                    txtOTP3.requestFocus();
                }
            }
        });

        txtOTP5.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String sText = s.toString();
                if (sText.length() == 1) {
                    txtOTP6.requestFocus();
                }

                if(sText.length() == 0) {
                    txtOTP4.requestFocus();
                }
            }
        });

        txtOTP6.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String sText = s.toString();
                if (sText.length() == 1) {
                    String s1 = txtOTP1.getText().toString();
                    String s2 = txtOTP2.getText().toString();
                    String s3 = txtOTP3.getText().toString();
                    String s4 = txtOTP4.getText().toString();
                    String s5 = txtOTP5.getText().toString();
                    String sCode = s1 + s2 + s3 + s4 + s5 + sText;
                    progressBar.setVisibility(View.VISIBLE);
                    verifyCode(sCode);
                }

                if(sText.length() == 0) {
                    txtOTP5.requestFocus();
                }
            }
        });


//
//        txt1.addTextChangedListener(new OTPWatcher(txt1, txt2));
//        txt2.addTextChangedListener(new OTPWatcher(txt2, txt3));
//        txt3.addTextChangedListener(new OTPWatcher(txt3, txt4));
//        txt4.addTextChangedListener(new OTPWatcher(txt4, txt5));
//        txt5.addTextChangedListener(new OTPWatcher(txt5, txt6));

        mCallBack = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                m_sVerificationId = s;
            }

            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

                String code = phoneAuthCredential.getSmsCode();
                verifyCode(code);

            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Toast.makeText(PhoneLoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        };
    }

    private void verifyCode(String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(m_sVerificationId, code);
        signInWithCredentials(credential);
    }

    private void signInWithCredentials(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        progressBar.setVisibility(View.INVISIBLE);
                        LaunchActivity();
                    } else {
                        progressBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(PhoneLoginActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });

    }

    public void LaunchActivity() {
        CommonVariables.m_sFirebaseUserId = mAuth.getUid();
        //check if this firebase user exists in users table. if not ask for some additional details to enter
        DocumentReference docRef = db.collection("users").document(CommonVariables.m_sFirebaseUserId);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();

                    if (document.exists()) {
                        CommonVariables.loggedInUserDetails = document.toObject(User.class);
                        getCurrentUserAddress();

                    } else {
                        LaunchOtherDetailsActivity();
                    }
                } else {
                    Log.d("TAG", "get failed with ", task.getException());
                }
            }
        });
    }

    private void LaunchLandingPage() {
        Intent intent = new Intent(PhoneLoginActivity.this, HomeActivity.class);
        //prevent the user to come again to this screen in he presses back button
        startActivity(intent);
        finish();
    }

    private void LaunchOtherDetailsActivity() {
        Intent intent = new Intent(PhoneLoginActivity.this, UserDetailsActivity.class);
        //prevent the user to come again to this screen in he presses back button
        startActivity(intent);
        finish();
    }

    private void LaunchLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void getCurrentUserAddress() {
        db.collection(Constants.COLLECTION_ADDRESSES)
                .document(CommonVariables.loggedInUserDetails.primaryAddressId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()) {
                            CommonVariables.loggedInUserAddress = task.getResult().toObject(Address.class);
                            getAndUpdateFCMToken();

                        } else {
                            mAuth.signOut();
                            LaunchLoginActivity();
                            Log.d("TAG", "get failed with ", task.getException());
                        }
                    }
                });
    }

    private void getAndUpdateFCMToken() {
        //Get Firebase FCM token
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(token -> {
            if (!TextUtils.isEmpty(token)) {
                Log.d("fcm_token", "retrieve token successful : " + token);
            } else {
                Log.w("fcm_token", "token should not be null...");
            }
        }).addOnFailureListener(e -> {
            //handle e
        }).addOnCanceledListener(() -> {
            //handle cancel
        }).addOnCompleteListener(task -> {

            String fcmToken = task.getResult();
            Log.v("fcm_token", "This is the token : " + fcmToken);
            DocumentReference userRef = db.collection("users")
                    .document(CommonVariables.loggedInUserDetails.id);

            userRef.update(Constants.FIELD_FCM_TOKEN, fcmToken)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d("fcm_token", "DocumentSnapshot successfully updated!");
                                CommonVariables.loggedInUserDetails.fcmToken = fcmToken;
                            }

                            LaunchLandingPage();
                        }
                    });
        });
    }
}