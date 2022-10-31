package com.pentaware.foodie.auth;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.pentaware.foodie.R;
import com.pentaware.foodie.baseactivity.HomeActivity;
import com.pentaware.foodie.models.Address;
import com.pentaware.foodie.models.User;
import com.pentaware.foodie.utils.CommonMethods;
import com.pentaware.foodie.utils.CommonVariables;
import com.pentaware.foodie.utils.Constants;

import java.util.List;
import java.util.Objects;

import pl.droidsonroids.gif.GifImageView;

public class UserAddressDetailsActivity extends AppCompatActivity {

    private static final String TAG = "useraddressyy";

    private EditText editTextLine1Address, editTextLine2Address, editTextPinCode,
            editTextReferralCode, editTextState;
    private AutoCompleteTextView txtSocietyName;
    private TextView txtAddSociety, txtPageCount;

    private Spinner spinnerCity;
    private Button btnFinishDetails;
    private GifImageView progressBar;

//    private ArrayAdapter<City> cityArrayAdapter;
    private ArrayAdapter<String> societyArrayAdapter;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private String m_fcmToken;
    private Boolean changeAddress = false;
    private TextView txtReferralCodePlaceholder;

    private int index = 0;
//    private City selectedCity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_address_details);
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


        Intent iin = getIntent();
        Bundle b = iin.getExtras();

        if (b != null) {
            changeAddress = (boolean) b.get(Constants.EXTRA_CHANGE_ADDRESS);
        }

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        editTextLine1Address = findViewById(R.id.edit_text_line_1_address);
        editTextLine2Address = findViewById(R.id.edit_text_line_2_address);
//        txtSocietyName = findViewById(R.id.txt_society_name);
        editTextPinCode = findViewById(R.id.edit_text_pincode);
        editTextReferralCode = findViewById(R.id.edit_text_referral_code);
        editTextState = findViewById(R.id.edit_text_state);
        txtReferralCodePlaceholder = findViewById(R.id.referral_code_placeholder);
        btnFinishDetails = findViewById(R.id.btn_finish_details);
//        spinnerCity = findViewById(R.id.spinner_city);
        progressBar = findViewById(R.id.progress_bar);
//        txtAddSociety = findViewById(R.id.txt_add_society);
//        txtPageCount = findViewById(R.id.txt_page_count);

        if(changeAddress) {
            txtPageCount.setVisibility(View.GONE);
            btnFinishDetails.setText("Finish");
        }

        btnFinishDetails.setOnClickListener(view -> {
            progressBar.setVisibility(View.VISIBLE);
            validateDetails();
        });
    }

    private void validateDetails() {

        boolean bErrorFound = false;

        if (editTextLine1Address.getText().toString().trim().equals("")) {
            Toast.makeText(this, "Flat no./Tower no. cannot be empty", Toast.LENGTH_SHORT).show();
            bErrorFound = true;
        }

        if (editTextLine2Address.getText().toString().trim().equals("")) {
            Toast.makeText(this, "Block no./Street no. cannot be empty", Toast.LENGTH_SHORT).show();
            bErrorFound = true;
        }

        if (txtSocietyName.getText().toString().trim().equals("")) {
            Toast.makeText(this, "Society name cannot be empty", Toast.LENGTH_SHORT).show();
            bErrorFound = true;
        }

        if (editTextPinCode.getText().toString().trim().equals("")) {
            Toast.makeText(this, "Pincode cannot be empty", Toast.LENGTH_SHORT).show();
            bErrorFound = true;
        }

        if (bErrorFound) {
            progressBar.setVisibility(View.GONE);
            return;
        }

        getFCMToken();
    }

    private void saveDetails() {
        User user;

        if (changeAddress) {
            user = CommonVariables.loggedInUserDetails;

            Address userAddress = new Address();
            userAddress.name = user.name;
            userAddress.phone = user.phone;
            userAddress.addressLine1 = editTextLine1Address.getText().toString();
            userAddress.addressLine2 = editTextLine2Address.getText().toString();
            userAddress.pinCode = editTextPinCode.getText().toString();
            userAddress.city = spinnerCity.getSelectedItem().toString();
            userAddress.state = editTextState.getText().toString();

//            CommonVariables.loggedInUserDetails.address = userAddress;

            db.collection(Constants.COLLECTION_USERS)
                    .document(CommonVariables.loggedInUserDetails.id)
                    .set(CommonVariables.loggedInUserDetails);

            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Address Updated Successfully",
                    Toast.LENGTH_SHORT).show();

//            Intent intent = new Intent(UserAddressDetailsActivity.this, HomeActivity.class);
//            startActivity(intent);
//            finish();

            launchHomeActivity();

        } else {

            user = CommonVariables.enteredUserDetails;

            Address userAddress = new Address();
            userAddress.name = user.name;
            userAddress.phone = user.phone;
            userAddress.addressLine1 = editTextLine1Address.getText().toString();
            userAddress.addressLine2 = editTextLine2Address.getText().toString();
            userAddress.pinCode = editTextPinCode.getText().toString();
            userAddress.city = spinnerCity.getSelectedItem().toString();
            userAddress.state = editTextState.getText().toString();

//            user.address = userAddress;
            user.referralCode = editTextReferralCode.getText().toString();

            user.profileImageUrl = null;
            user.points = 0;
            user.fcmToken = m_fcmToken;

            db.collection(Constants.COLLECTION_USERS)
                    .document(auth.getCurrentUser().getUid())
                    .set(user)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            progressBar.setVisibility(View.GONE);

                            if(!task.isSuccessful()) {
                                Log.d(TAG, task.getException().toString());
                                return;
                            }

                            user.id = auth.getCurrentUser().getUid();
                            CommonVariables.loggedInUserDetails = user;

                            launchHomeActivity();
                        }
                    });
        }
    }

    private void launchHomeActivity() {
        Intent intent = new Intent(this, HomeActivity.class);

        if(changeAddress) {
            intent.putExtra(Constants.EXTRA_CHANGE_ADDRESS, true);
        }

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void getFCMToken() {
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
            m_fcmToken = task.getResult();
            Log.d("fcm_token", "This is the token : " + task.getResult());

            saveDetails();
        });
    }
}

