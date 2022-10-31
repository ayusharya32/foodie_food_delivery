package com.pentaware.foodie.auth;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.pentaware.foodie.R;

import java.util.Objects;

import pl.droidsonroids.gif.GifImageView;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText editTextEmail;
    private ProgressBar progressBar;
    private Button btnResetPassword;
    private ImageView imgBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
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

        editTextEmail = findViewById(R.id.edit_text_email);

        progressBar = findViewById(R.id.progress_bar);

        btnResetPassword = findViewById(R.id.btn_reset_password);
        imgBack = findViewById(R.id.img_back);

        btnResetPassword.setOnClickListener(view -> {
            showProgressBar();

            String email = editTextEmail.getText().toString();
            FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        hideProgressBar();

                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Password recovery mail sent", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(this, "Error occured!", Toast.LENGTH_LONG).show();
                        }

                        finish();
                    });
        });

        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
        btnResetPassword.setVisibility(View.GONE);
    }

    private void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
        btnResetPassword.setVisibility(View.VISIBLE);
    }
}