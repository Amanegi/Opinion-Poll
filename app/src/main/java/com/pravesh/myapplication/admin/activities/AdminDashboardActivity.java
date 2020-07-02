package com.pravesh.myapplication.admin.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.pravesh.myapplication.R;
import com.pravesh.myapplication.common.LoginActivity;
import com.pravesh.myapplication.common.VoteActivity;
import com.pravesh.myapplication.util.Constants;
import com.pravesh.myapplication.util.CustomDialog;
import com.pravesh.myapplication.util.CustomDialogListener;

public class AdminDashboardActivity extends AppCompatActivity {
    private CardView addAdmin, addUser, addQuestion, vote;
    private TextView nameTag;
    private ImageButton btnLogout;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        btnLogout = findViewById(R.id.btnLogout);
        addAdmin = findViewById(R.id.cardAddAdmin);
        addUser = findViewById(R.id.cardAddUser);
        addQuestion = findViewById(R.id.cardAddQuestion);
        vote = findViewById(R.id.cardVote);
        nameTag = findViewById(R.id.nameTag);

        sharedPreferences = getSharedPreferences(Constants.SHAREDPREFERENCES_FILE, MODE_PRIVATE);
        String name = sharedPreferences.getString("name", null);
        nameTag.setText("Hi , " + name);

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final CustomDialog customDialog = new CustomDialog(AdminDashboardActivity.this);
                customDialog.setCancellable(true);
                customDialog.setImageView(0);
                customDialog.setTitle("Logout");
                customDialog.setMessage("Are you sure you want to logout?");
                customDialog.setPositiveText("Confirm");
                customDialog.setNegativeText("Cancel");
                customDialog.setPositiveBackground("#9474F1");
                customDialog.setNegativeBackground("#386CEF");
                customDialog.setPositiveListener(new CustomDialogListener() {
                    @Override
                    public void onClick() {
                        logout();
                    }
                });
                customDialog.setNegativeListener(new CustomDialogListener() {
                    @Override
                    public void onClick() {
                        customDialog.hide();
                    }
                });
                customDialog.show();
            }
        });

        addAdmin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AdminDashboardActivity.this, AddAdminActivity.class));
            }
        });

        addUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AdminDashboardActivity.this, AddUserActivity.class));
            }
        });

        addQuestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AdminDashboardActivity.this, AddQuestionActivity.class));
            }
        });
        vote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AdminDashboardActivity.this, VoteActivity.class));
            }
        });
    }

    private void logout() {
        sharedPreferences = getSharedPreferences("data", MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
        startActivity(new Intent(AdminDashboardActivity.this, LoginActivity.class));
    }
}
