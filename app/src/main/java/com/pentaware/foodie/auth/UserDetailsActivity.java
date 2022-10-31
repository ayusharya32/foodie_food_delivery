package com.pentaware.foodie.auth;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.pentaware.foodie.R;
import com.pentaware.foodie.utils.CommonVariables;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

import pl.droidsonroids.gif.GifImageView;


public class UserDetailsActivity extends AppCompatActivity {

    private static final String TAG = "UserDetailsActivity";

    private EditText editTextName, editTextPhone, editTextEmail, editTextDob;
    private RadioButton radioBtnMale;
    private Button btnFinish;
    private ProgressBar progressBar;
    private ImageView imgBack;

    FirebaseFirestore db;
    FirebaseAuth auth;
    private Calendar myCalendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_details);
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

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        editTextName = findViewById(R.id.edit_text_name);
        editTextPhone = findViewById(R.id.edit_text_phone);
        editTextEmail = findViewById(R.id.edit_text_email);
//        editTextDob = findViewById(R.id.edit_text_dob);
        progressBar = findViewById(R.id.progress_bar);
        radioBtnMale = findViewById(R.id.radio_gender_male);
        btnFinish = findViewById(R.id.btn_next_details);

        imgBack = findViewById(R.id.img_back);

//        setupDobCalender();

        if (CommonVariables.email != null) {
            editTextEmail.setText(CommonVariables.email);
        }

        if(auth.getCurrentUser() != null && auth.getCurrentUser().getEmail() != null) {
            CommonVariables.email = auth.getCurrentUser().getEmail();
            editTextEmail.setText(CommonVariables.email);

            if(editTextEmail.getText().length() > 0) {
                editTextEmail.setEnabled(false);
            }
        }

        if(auth.getCurrentUser() != null && auth.getCurrentUser().getPhoneNumber() != null) {
            CommonVariables.phone = auth.getCurrentUser().getPhoneNumber();
            editTextPhone.setText(getPhoneNumberWithoutCountryCode(CommonVariables.phone));

            if(editTextPhone.getText().length() > 0) {
                editTextPhone.setEnabled(false);
            }
        }

        btnFinish.setOnClickListener(view -> {
            showProgressBar();
            validateDetails();
        });

        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void setupDobCalender() {
        myCalendar = Calendar.getInstance();

        DatePickerDialog.OnDateSetListener date = (view, year, monthOfYear, dayOfMonth) -> {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateLabel();
        };

        editTextDob.setOnClickListener(v -> {
            new DatePickerDialog(UserDetailsActivity.this, R.style.DatePickerTheme, date, myCalendar
                    .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                    myCalendar.get(Calendar.DAY_OF_MONTH)).show();
        });
    }

    private void updateLabel() {
        String myFormat = "dd/MM/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.getDefault());
        editTextDob.setText(sdf.format(myCalendar.getTime()));
    }

    private String getPhoneNumberWithoutCountryCode(String phone) {
        if(phone.contains("+91")) {
            return phone.replace("+91", "");
        }
        return phone;
    }

    private void validateDetails() {

        boolean bErrorFound = false;
        if (editTextName.getText().toString().trim().equals("")) {
            Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
            bErrorFound = true;
        }

        if (editTextPhone.getText().toString().trim().equals("")) {
            Toast.makeText(this, "Phone number cannot be empty", Toast.LENGTH_SHORT).show();
            bErrorFound = true;
        }

        if (editTextEmail.getText().toString().trim().equals("")) {
            Toast.makeText(this, "Email address cannot be empty", Toast.LENGTH_SHORT).show();
            bErrorFound = true;
        }

        if (editTextPhone.getText().toString().trim().length() != 10) {
            Toast.makeText(this, "Enter a valid Phone Number", Toast.LENGTH_SHORT).show();
            bErrorFound = true;
        }

        if (bErrorFound) {
            hideProgressBar();
            return;
        }
        String email = editTextEmail.getText().toString();
        checkIfEmailAlreadyExists(email);
    }

    private void checkIfEmailAlreadyExists(String email) {
        CollectionReference usersRef = db.collection("users");
        Query query = usersRef.whereEqualTo("Email", email);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot documentSnapshot : task.getResult()) {
                        String userEmail = documentSnapshot.getString("Email");

                        if (email.equals(userEmail)) {
                            hideProgressBar();
                            Toast.makeText(UserDetailsActivity.this, "Email address already registered!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                if (task.getResult().size() == 0) {
                    Log.d(TAG, "Email not Exists");
                    String phone = editTextPhone.getText().toString();
                    checkIfPhoneAlreadyExists(phone);
                }
            }
        });
    }

    private void checkIfPhoneAlreadyExists(String phoneNumber) {
        CollectionReference usersRef = db.collection("users");
        Query query = usersRef.whereEqualTo("Phone", phoneNumber);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot documentSnapshot : task.getResult()) {
                        String userPhone = documentSnapshot.getString("Phone");

                        if (phoneNumber.equals(userPhone)) {
                            hideProgressBar();
                            Toast.makeText(UserDetailsActivity.this, "Phone Number already registered!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                if (task.getResult().size() == 0) {
                    Log.d(TAG, "Phone number not Exists");
                    saveDetails();
                }
            }
        });
    }


    private void saveDetails() {
        CommonVariables.enteredUserDetails.name = editTextName.getText().toString();
        CommonVariables.enteredUserDetails.phone = editTextPhone.getText().toString();
        CommonVariables.enteredUserDetails.email = editTextEmail.getText().toString();
//        CommonVariables.enteredUserDetails.dob = editTextDob.getText().toString();

        if (radioBtnMale.isChecked()) {
            CommonVariables.enteredUserDetails.gender = "Male";
        } else {
            CommonVariables.enteredUserDetails.gender = "Female";
        }

        hideProgressBar();

        startActivity(new Intent(this, DeliveryLocationActivity.class));
        finish();
    }

    private void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
        btnFinish.setVisibility(View.GONE);
    }

    private void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
        btnFinish.setVisibility(View.VISIBLE);
    }
}