package com.pentaware.foodie.auth;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
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

import pl.droidsonroids.gif.GifImageView;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "Loginyyy";
    private EditText editTextEmail, editTextPassword, etPhone;
    private TextView txtPrivacyPolicy;
    private FirebaseAuth mAuth;
    private GifImageView progressBar;
    private static final int RC_SIGN_IN = 123;
    private GoogleSignInClient mGoogleSignInClient;
    FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Objects.requireNonNull(getSupportActionBar()).hide();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//            window.setStatusBarColor(getResources().getColor(R.color.white));
        }

        int flags = this.getWindow().getDecorView().getSystemUiVisibility();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags = flags ^ View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        }
        this.getWindow().getDecorView().setSystemUiVisibility(flags);

        FirebaseApp.initializeApp(this);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        editTextEmail = findViewById(R.id.edit_text_email);
        editTextPassword = findViewById(R.id.edit_text_password);
        etPhone = findViewById(R.id.et_phone);
        txtPrivacyPolicy = findViewById(R.id.txt_privacy_policy);

        Button btnLoginPhone = findViewById(R.id.btn_login);
        Button btnLoginEmail = findViewById(R.id.btn_email_login);
        Button btnGoogleSignIn = findViewById(R.id.btn_google_sign_in);

        TextView txtForgotPassword = findViewById(R.id.txt_forgot_password);

        progressBar = findViewById(R.id.progress_bar);

        btnLoginEmail.setOnClickListener(view -> {
//            progressBar.setVisibility(View.VISIBLE);
//            String email = editTextEmail.getText().toString();
//            String password = editTextPassword.getText().toString();
//            if (email.equals("") || password.equals("")) {
//                progressBar.setVisibility(View.GONE);
//                Toast.makeText(this, "Enter valid credentials", Toast.LENGTH_SHORT).show();
//            } else {
//                signInWithEmail(email, password);
//            }
            startActivity(new Intent(this, EmailLoginActivity.class));
        });

        btnLoginPhone.setOnClickListener(view -> {
            String phone = etPhone.getText().toString().trim();

            if(phone.length() != 10) {
                Toast.makeText(this, "Please provide valid phone number", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(this, PhoneLoginActivity.class);
            intent.putExtra(Constants.EXTRA_PHONE_NUMBER, phone);
            startActivity(intent);
        });

        btnGoogleSignIn.setOnClickListener(view -> {
            progressBar.setVisibility(View.VISIBLE);
            signInWithGoogle();
        });

        txtForgotPassword.setOnClickListener(view -> {
            startActivity(new Intent(this, ForgotPasswordActivity.class));
        });

//        txtSignUp.setOnClickListener(view -> {
//            Intent intent = new Intent(this, RegisterActivity.class);
//            startActivity(intent);
//            finish();
//        });

//        txtPrivacyPolicy.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                startActivity(new Intent(LoginActivity.this, PrivacyPolicyActivity.class));
//            }
//        });
    }


    private void signInWithGoogle() {

        if (mGoogleSignInClient == null) {
            // Configure sign-in to request the user's ID, email address, and basic
            // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();

            mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        }
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.d(TAG, e.toString());

                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Google sign in failed: Reason - " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d("TAG", "firebaseAuthWithGoogle:" + acct.getId());
        CommonVariables.email = acct.getEmail();
        CommonVariables.userName = acct.getDisplayName();

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            LaunchActivity();
                            //  updateUI(user);
                        } else {
                            progressBar.setVisibility(View.GONE);
                            Exception ex = task.getException();
                            // If sign in fails, display a message to the user.
                            Log.w("google exception", "signInWithCredential:failure", task.getException());
                        }
                    }
                });
    }

    private void signInWithEmail(String email, String password) {
        CommonVariables.email = email;
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        //Sign in success, update UI with the signed-in user's information
                        LaunchActivity();
                    } else {
                        //If sign in fails, display a message to the user.
                        progressBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(this, "Authentication failed. Reason: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void LaunchHomeActivity() {
        Intent intent = new Intent(this, HomeActivity.class);
        //prevent the user to come again to this screen in he presses back button
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }


    public void LaunchActivity() {
        CommonVariables.m_sFirebaseUserId = mAuth.getUid();
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

    private void LaunchOtherDetailsActivity() {
        Intent intent = new Intent(this, UserDetailsActivity.class);
        //prevent the user to come again to this screen in he presses back btton
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
                            if(task.isSuccessful()) {
                                Log.d("fcm_token", "DocumentSnapshot successfully updated!");
                                CommonVariables.loggedInUserDetails.fcmToken = fcmToken;
                            }

                            LaunchHomeActivity();
                        }
                    });
        });
    }

    private void LaunchLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
