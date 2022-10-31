package com.pentaware.foodie.auth;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.pentaware.foodie.R;
import com.pentaware.foodie.api.FoodieApi;
import com.pentaware.foodie.api.RetrofitInstance;
import com.pentaware.foodie.models.api.EmailRequest;
import com.pentaware.foodie.utils.CommonMethods;
import com.pentaware.foodie.utils.CommonVariables;
import com.pentaware.foodie.utils.Constants;

import java.util.Objects;

import pl.droidsonroids.gif.GifImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivityyyy";

    private EditText editTextEmail, editTextPassword, editTextConfirmPassword;
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;

    private EditText txtOTP1, txtOTP2, txtOTP3, txtOTP4, txtOTP5, txtOTP6;
    private LinearLayout llPasswordDetails, llEnterOtp, llOtp;
    private Button btnVerifyEmail;

    private String otp = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
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

        mAuth = FirebaseAuth.getInstance();

        txtOTP1 = findViewById(R.id.txt_otp_1);
        txtOTP2 = findViewById(R.id.txt_otp_2);
        txtOTP3 = findViewById(R.id.txt_otp_3);
        txtOTP4 = findViewById(R.id.txt_otp_4);
        txtOTP5 = findViewById(R.id.txt_otp_5);
        txtOTP6 = findViewById(R.id.txt_otp_6);

        llPasswordDetails = findViewById(R.id.ll_password_details);
        llEnterOtp = findViewById(R.id.ll_enter_otp);
        llOtp = findViewById(R.id.ll_otp);
        btnVerifyEmail = findViewById(R.id.btn_verify_email);

        editTextEmail = findViewById(R.id.edit_text_email);
        editTextPassword = findViewById(R.id.edit_text_password);
        editTextConfirmPassword = findViewById(R.id.edit_text_confirm_password);
        Button btnSignUp = findViewById(R.id.btn_sign_up);
        progressBar = findViewById(R.id.progress_bar);
//        TextView txtLogin = findViewById(R.id.txt_login);

        setupOtpEditTexts();

        String receivedEmail = getIntent().getStringExtra(Constants.EXTRA_EMAIL);

        if(receivedEmail != null && !receivedEmail.isEmpty()) {
            editTextEmail.setText(receivedEmail);
        }

        btnSignUp.setOnClickListener(view -> {
            progressBar.setVisibility(View.VISIBLE);
            registerUser();
        });

//        txtLogin.setOnClickListener(view -> {
//            Intent intent = new Intent(this, LoginActivity.class);
//            startActivity(intent);
//            finish();
//        });

        btnVerifyEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnVerifyEmail.setEnabled(false);

                if(editTextEmail.getText().toString().isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "Please enter email", Toast.LENGTH_SHORT).show();
                    return;
                }

                sendOTP();
            }
        });

        llPasswordDetails.setVisibility(View.GONE);
        llEnterOtp.setVisibility(View.GONE);
        llOtp.setVisibility(View.VISIBLE);
    }

    private void sendOTP() {
        progressBar.setVisibility(View.VISIBLE);

        otp = CommonMethods.getOTP();

        String email = editTextEmail.getText().toString();
        String subject = "Foodie Email Verification";
        String body = "Your one time password for verification of email " + email + " is <b>" + otp + "</b>";

        EmailRequest requestBody = new EmailRequest(email, subject, body);

        FoodieApi foodieApi = RetrofitInstance.getRetrofitInstance(this).create(FoodieApi.class);

        Call<Void> call = foodieApi.sendMail(requestBody);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                Log.d(TAG, "Api Call Response");
                progressBar.setVisibility(View.GONE);

                // TODO: Start OTP Validation after creating firebase function for Sending OTP Email
//                if(!response.isSuccessful()) {
//                    btnVerifyEmail.setEnabled(true);
//                    Toast.makeText(RegisterActivity.this, "Some error occurred", Toast.LENGTH_SHORT).show();
//                    return;
//                }

                Toast.makeText(RegisterActivity.this, "OTP has been sent to your email address",
                        Toast.LENGTH_SHORT).show();

                llEnterOtp.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                progressBar.setVisibility(View.GONE);

                Log.d(TAG, "Api Call Failure");
                Toast.makeText(RegisterActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
                btnVerifyEmail.setEnabled(true);
            }
        });

    }

    private void registerUser() {
        boolean errorFound = false;

        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();

        if (email.equals("")) {
            Toast.makeText(this, "Email address cannnot be empty", Toast.LENGTH_SHORT).show();
            errorFound = true;
        }

        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

        if (!email.matches(emailPattern)) {
            Toast.makeText(this, "Enter a valid Email address", Toast.LENGTH_SHORT).show();
            errorFound = true;
        }


        if (password.equals("")) {
            Toast.makeText(this, "Password cannnot be empty", Toast.LENGTH_SHORT).show();
            errorFound = true;
        }

        if (confirmPassword.equals("")) {
            Toast.makeText(this, "Confirm Password cannnot be empty", Toast.LENGTH_SHORT).show();
            errorFound = true;
        }

        if (!password.equals("") && !confirmPassword.equals("")) {
            if (!password.matches(confirmPassword)) {
                Toast.makeText(this, "Entered Passwords do not match", Toast.LENGTH_SHORT).show();
                errorFound = true;
            }
        }

        if (errorFound) {
            progressBar.setVisibility(View.GONE);
            return;
        }

        createUser(email, password);
    }

    private void createUser(String email, String password) {
        CommonVariables.email = email;
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Intent intent = new Intent(RegisterActivity.this, UserDetailsActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    } else {
                        // If sign in fails, display a message to the user.
                        progressBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(RegisterActivity.this, "Registration failed" + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void verifyCode(String code) {
        progressBar.setVisibility(View.GONE);

        // TODO: Start OTP Validation after creating firebase function for Sending OTP Email
//        if(!code.equals(otp)) {
//            Toast.makeText(this, "Invalid OTP", Toast.LENGTH_SHORT).show();
//            btnVerifyEmail.setEnabled(true);
//            return;
//        }

        Toast.makeText(this, "Email verified successfully", Toast.LENGTH_SHORT).show();

        llPasswordDetails.setVisibility(View.VISIBLE);
        llOtp.setVisibility(View.GONE);
    }

    private void setupOtpEditTexts() {
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
    }
}