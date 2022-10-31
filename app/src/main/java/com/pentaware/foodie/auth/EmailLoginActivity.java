package com.pentaware.foodie.auth;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.pentaware.foodie.R;
import com.pentaware.foodie.baseactivity.HomeActivity;
import com.pentaware.foodie.baseactivity.SplashActivity;
import com.pentaware.foodie.models.Address;
import com.pentaware.foodie.models.User;
import com.pentaware.foodie.utils.CommonVariables;
import com.pentaware.foodie.utils.Constants;

import java.util.Objects;

import pl.droidsonroids.gif.GifImageView;

public class EmailLoginActivity extends AppCompatActivity {

    private static final String TAG = "EmailLoginActivityyyy";

    private EditText etEmail, etPassword;
    private TextView txtForgotPassword;
    private Button btnLogin, btnNext;
    private ImageView imgBack;
    private LinearLayout llPasswordDetails;
    private ProgressBar progressBar;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private boolean userCheckedAndFound = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_login);
        Objects.requireNonNull(getSupportActionBar()).hide();

        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        txtForgotPassword = findViewById(R.id.txt_forgot_password);
        btnLogin = findViewById(R.id.btn_login);
        btnNext = findViewById(R.id.btn_next);
        llPasswordDetails = findViewById(R.id.ll_password_details);
        imgBack = findViewById(R.id.img_back);
        progressBar = findViewById(R.id.progress_bar);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        setupClickListeners();

//        etEmail.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
//                Log.d(TAG, "Focus: " + hasFocus);
//                if(!hasFocus) {
//                    String email = etEmail.getText().toString().trim();
//
//                    if(!email.isEmpty()) {
//                        checkNewUser(email, false);
//                    }
//                }
//            }
//        });

        etEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                hideUserFoundUi();
            }
        });
    }

    private void setupClickListeners() {
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean inputValid = validateEmailInput();

                if(inputValid) {
                    String email = etEmail.getText().toString().trim();
                    checkNewUser(email, false);
                }
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean inputValid = validateInput();

                if(inputValid) {
                    String email = etEmail.getText().toString().trim();
                    String password = etPassword.getText().toString().trim();

                    if(userCheckedAndFound) {
                        signInWithEmail(email, password);
                    } else {
                        checkNewUser(email, true);
                    }

                }
            }
        });

        txtForgotPassword.setOnClickListener(view -> {
            startActivity(new Intent(this, ForgotPasswordActivity.class));
        });

        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void checkNewUser(String email, boolean checkAndSignup) {
        showProgressBar();
        userCheckedAndFound = false;

        auth.fetchSignInMethodsForEmail(email)
                .addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                    @Override
                    public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                        progressBar.setVisibility(View.GONE);

                        if(task.isSuccessful() && task.getResult() != null) {
                            boolean isNewUser = task.getResult().getSignInMethods().isEmpty();

                            if(isNewUser) {
                                showRegisterPopup(email);
                                hideUserFoundUi();

                            } else {
                                userCheckedAndFound = true;
                                showUserFoundUi();

                                if(checkAndSignup) {
                                    String password = etPassword.getText().toString().trim();
                                    signInWithEmail(email, password);
                                }
                            }
                        }
                    }
                });
    }

    private void signInWithEmail(String email, String password) {
        showProgressBar();
        CommonVariables.email = email;

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        //Sign in success, update UI with the signed-in user's information
                        LaunchActivity();
                    } else {
                        //If sign in fails, display a message to the user.
                        hideProgressbar(true);
                        Toast.makeText(this, "Authentication failed. Reason: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean validateInput() {
        String email = etEmail.getText().toString().trim();

        if(email.isEmpty()) {
            Toast.makeText(EmailLoginActivity.this, "Please enter email",
                    Toast.LENGTH_SHORT).show();

            return false;
        }

        if(!email.contains("@") || !email.contains(".")) {
            Toast.makeText(EmailLoginActivity.this, "Please enter valid email",
                    Toast.LENGTH_SHORT).show();

            return false;
        }


        String password = etPassword.getText().toString().trim();

        if(password.isEmpty()) {
            Toast.makeText(EmailLoginActivity.this, "Please enter password",
                    Toast.LENGTH_SHORT).show();

            return false;
        }

        return true;
    }

    private boolean validateEmailInput() {
        String email = etEmail.getText().toString().trim();

        if(email.isEmpty()) {
            Toast.makeText(EmailLoginActivity.this, "Please enter email",
                    Toast.LENGTH_SHORT).show();

            return false;
        }

        if(!email.contains("@") || !email.contains(".")) {
            Toast.makeText(EmailLoginActivity.this, "Please enter valid email",
                    Toast.LENGTH_SHORT).show();

            return false;
        }

        return true;
    }

    public void LaunchActivity() {
        CommonVariables.m_sFirebaseUserId = auth.getUid();
        //check if this firebase user exists in users table. if not ask for some additional details to enter
        DocumentReference docRef = db.collection("users").document(CommonVariables.m_sFirebaseUserId);
        docRef.get().addOnCompleteListener(task -> {
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
        });
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
                            auth.signOut();
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

                            LaunchHomeActivity();
                        }
                    });
        });

    }

    private void LaunchHomeActivity() {
        Intent intent = new Intent(this, HomeActivity.class);
        //prevent the user to come again to this screen in he presses back button
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void LaunchOtherDetailsActivity() {
        Intent intent = new Intent(this, UserDetailsActivity.class);
        //prevent the user to come again to this screen in he presses back btton
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void LaunchLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
        btnNext.setVisibility(View.GONE);
        btnLogin.setVisibility(View.GONE);
    }

    private void hideProgressbar(boolean showLoginButton) {
        progressBar.setVisibility(View.GONE);

        if(showLoginButton) {
            btnLogin.setVisibility(View.VISIBLE);
        } else {
            btnNext.setVisibility(View.VISIBLE);
        }
    }

    private void showUserFoundUi() {
        llPasswordDetails.setVisibility(View.VISIBLE);
        btnNext.setVisibility(View.GONE);
        btnLogin.setVisibility(View.VISIBLE);
    }

    private void hideUserFoundUi() {
        llPasswordDetails.setVisibility(View.GONE);
        btnNext.setVisibility(View.VISIBLE);
        btnLogin.setVisibility(View.GONE);
    }

    private void showRegisterPopup(String email) {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_register_popup, null);
        dialogBuilder.setView(dialogView);

        Button btnRegister = dialogView.findViewById(R.id.btn_register);
        ImageView btnCloseDialog = dialogView.findViewById(R.id.btn_close_dialog);
        TextView txtMessage = dialogView.findViewById(R.id.txt_message);

        String message = "Oops! Looks like you don't have any account associated with email <b>" + email + "</b>";
        txtMessage.setText(Html.fromHtml(message));

        AlertDialog alertDialog = dialogBuilder.create();

        btnRegister.setOnClickListener(view -> {
            alertDialog.dismiss();

            Intent intent = new Intent(this, RegisterActivity.class);
            intent.putExtra(Constants.EXTRA_EMAIL, email);
            startActivity(intent);
            finish();
        });

        btnCloseDialog.setOnClickListener(view -> {
            alertDialog.dismiss();
        });

        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            getWindow().setGravity(Gravity.BOTTOM);
            alertDialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation;
        }
        alertDialog.show();

    }
}
