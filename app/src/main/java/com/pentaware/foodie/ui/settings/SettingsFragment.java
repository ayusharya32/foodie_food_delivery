package com.pentaware.foodie.ui.settings;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.pentaware.foodie.auth.LoginActivity;
import com.pentaware.foodie.databinding.FragmentSettingsBinding;
import com.pentaware.foodie.utils.CommonVariables;
import com.pentaware.foodie.utils.Constants;

public class SettingsFragment extends Fragment {
    private static final String TAG = "SettingsFragmentyy";

    private FragmentSettingsBinding binding;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        setOnClickListeners();

        binding.imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(getView()).popBackStack();
            }
        });
    }

    private void setOnClickListeners() {
        binding.txtOrderHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(getView())
                        .navigate(SettingsFragmentDirections.actionNavSettingsToNavOrderHistory());
            }
        });

        binding.txtLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });
    }

    private void logout() {
        if(CommonVariables.loggedInUserDetails.ongoingOrderId != null
                && !CommonVariables.loggedInUserDetails.ongoingOrderId.isEmpty()) {
            Toast.makeText(getContext(), "Cannot logout when order in process",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        CommonVariables.loggedInUserDetails.fcmToken = "";

        db.collection(Constants.COLLECTION_USERS)
                .document(CommonVariables.loggedInUserDetails.id)
                .set(CommonVariables.loggedInUserDetails)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {
                            CommonVariables.loggedInUserDetails = null;
                            CommonVariables.enteredUserDetails = null;
                            CommonVariables.m_sFirebaseUserId = null;

                            auth.signOut();
                            launchLoginActivity();
                        }
                    }
                });
    }

    private void launchLoginActivity() {
        Intent intent = new Intent(getContext(), LoginActivity.class);
        //prevent the user to come again to this screen in he presses back button
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
