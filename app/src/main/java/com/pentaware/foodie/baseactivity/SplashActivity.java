package com.pentaware.foodie.baseactivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.pentaware.foodie.auth.LoginActivity;
import com.pentaware.foodie.auth.UserDetailsActivity;
import com.pentaware.foodie.models.Address;
import com.pentaware.foodie.models.AppInfo;
import com.pentaware.foodie.models.CartItem;
import com.pentaware.foodie.models.User;
import com.pentaware.foodie.utils.CommonVariables;
import com.pentaware.foodie.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class SplashActivity extends AppCompatActivity {
    private static final String TAG = "SplashActivityyyyy";

    Boolean splashLoaded = false;
    Handler handler;
    View mContentView;
    LottieAnimationView splashImg;
    FirebaseAuth auth;
    FirebaseFirestore db;
    CountDownTimer timer;
    TextView txtAppTitle;
    ImageView imgSplash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);

        splashScreen.setKeepOnScreenCondition(() -> true);

//        getSupportActionBar().hide();

        Log.d("ActivityOpenyyy", "Splash Activity");

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (!splashLoaded) {
            handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
//                    startTimer();
                    getAppInfo();
                }
            }, 0);
            splashLoaded = true;
        }
    }

    private void startTimer() {
        timer = new CountDownTimer(0, 0) {
            public void onTick(long millisUntilFinished) {

            }

            public void onFinish() {

                if (auth.getCurrentUser() != null) {
                    CommonVariables.m_sFirebaseUserId = auth.getUid();
                }
            }
        }.start();
    }

    private void getAppInfo() {
        db.collection("AppInfo")
                .document("AppInfo")
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()) {
                            CommonVariables.appInfo = task.getResult().toObject(AppInfo.class);

                            if (auth.getCurrentUser() != null) {
                                CommonVariables.m_sFirebaseUserId = auth.getUid();
                            }

                            if (auth.getCurrentUser() != null) {
                                LaunchActivity();
                            } else {
                                LaunchLoginActivity();
                            }
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
                            if(task.isSuccessful()) {
                                Log.d("fcm_token", "DocumentSnapshot successfully updated!");
                                CommonVariables.loggedInUserDetails.fcmToken = fcmToken;
                            }

                            launchHomeActivity();
                        }
                    });
        });
    }

    private void LaunchLoginActivity() {
        Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void LaunchOtherDetailsActivity() {
        Intent intent = new Intent(SplashActivity.this, UserDetailsActivity.class);
        startActivity(intent);
        finish();
    }

    private void launchHomeActivity() {
        Intent intent = new Intent(SplashActivity.this, HomeActivity.class);

        Log.d(TAG, "Notification Data: " + getIntent().getExtras());

        if(getIntent().getExtras() != null) {
            intent.putExtras(getIntent());
        }

        startActivity(intent);
        finish();
    }

    public void LaunchActivity() {
        CommonVariables.m_sFirebaseUserId = auth.getUid();
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
                auth.signOut();
                LaunchLoginActivity();
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
                            getUserCartItems();

                        } else {
                            auth.signOut();
                            LaunchLoginActivity();
                            Log.d("TAG", "get failed with ", task.getException());
                        }
                    }
                });
    }

    private void getUserCartItems() {
        db.collection(Constants.COLLECTION_CART_ITEMS)
                .whereEqualTo(Constants.FIELD_USER_ID, CommonVariables.loggedInUserDetails.id)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()) {
                            Log.d(TAG, "onComplete: getUserCartItems: "
                                    + task.getResult().toObjects(CartItem.class));
                            CommonVariables.userCartItems =
                                    task.getResult().toObjects(CartItem.class);

                            getAndUpdateFCMToken();

                        } else {
                            auth.signOut();
                            LaunchLoginActivity();
                            Log.d("TAG", "get failed with ", task.getException());
                        }
                    }
                });
    }
}


