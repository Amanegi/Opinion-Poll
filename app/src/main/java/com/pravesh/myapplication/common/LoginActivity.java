package com.pravesh.myapplication.common;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.pravesh.myapplication.R;
import com.pravesh.myapplication.admin.activities.AdminDashboardActivity;
import com.pravesh.myapplication.util.Constants;
import com.shashank.sony.fancytoastlib.FancyToast;

import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {

    private LottieAnimationView verifyAnim;
    Button btnLogin;
    EditText edtMobile, edtOtp;
    MaterialButton btnRequestOtp;
    String codeSent, name, phone, type;
    FirebaseAuth auth;
    FirebaseFirestore database;
    CountDownTimer timer;
    boolean isAdmin;
    ConstraintLayout detailLayout;
    RadioButton radioUser, radioAdmin;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    public static final String TAG = LoginActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        verifyAnim = findViewById(R.id.verifyAnim);
        btnLogin = findViewById(R.id.btnLogin);
        edtMobile = findViewById(R.id.edtMobile);
        edtOtp = findViewById(R.id.edtPassword);
        btnRequestOtp = findViewById(R.id.txtRequestOtp);
        radioAdmin = findViewById(R.id.radioAdmin);
        radioUser = findViewById(R.id.radioUser);
        detailLayout = findViewById(R.id.detailLayout);

        auth = FirebaseAuth.getInstance();
        database = FirebaseFirestore.getInstance();

        radioUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                detailLayout.setVisibility(View.VISIBLE);
                isAdmin = false;
            }
        });
        radioAdmin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                detailLayout.setVisibility(View.VISIBLE);
                isAdmin = true;
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
                String code = edtOtp.getText().toString().trim();
                try {
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(codeSent, code);
                    signInWithPhoneAuthCredential(credential);
                } catch (Exception e) {
                    showToast("Invalid OTP", FancyToast.ERROR);
                }
            }
        });
        edtMobile.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 10) {
                    btnRequestOtp.setVisibility(View.VISIBLE);
                }
                if (s.length() < 10) {
                    btnRequestOtp.setVisibility(View.GONE);
                }

            }
        });
        btnRequestOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
                String phoneNo = edtMobile.getText().toString().trim();
                checkUser(phoneNo);
            }
        });
    }

    private void requestOtp() {
        String phone = edtMobile.getText().toString().trim();
        PhoneAuthProvider.getInstance().verifyPhoneNumber("+91" + phone, 30, TimeUnit.SECONDS, LoginActivity.this, mCallbacks);
    }

    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
            Log.d(TAG, "onVerificationCompleted: ");
            edtOtp.setText("" + codeSent);
            signInWithPhoneAuthCredential(phoneAuthCredential);
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            Log.d(TAG, "onVerificationFailed: " + e);

        }

        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            Log.d(TAG, "onCodeSent: " + s);
            codeSent = s;
        }
    };

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        auth.signInWithCredential(credential)
                .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            if (radioAdmin.isChecked()) {
                                editor.putString("name", name);
                                editor.putString("phone", phone);
                                editor.putString("type", type);
                                editor.apply();
                                startActivity(new Intent(LoginActivity.this, AdminDashboardActivity.class));
                            } else {
                                Intent i = new Intent(LoginActivity.this, VoteActivity.class);
                                editor.putString("name", name);
                                editor.putString("phone", phone);
                                editor.putString("type", type);
                                editor.apply();
                                i.putExtra("type", "userType");
                                startActivity(i);
                            }
                            finish();
                        } else {
                            // Sign in failed, display a message and update the UI
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            showToast("Login failed", FancyToast.ERROR);
                            finish();

                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                                showToast("Invalid OTP", FancyToast.ERROR);
                            }
                        }
                    }
                });
    }


    private void hideKeyboard() {
        InputMethodManager inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);

        if (getCurrentFocus() != null && inputManager != null)
            inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
    }

    private void checkUser(String phoneNumber) {
        sharedPreferences = getSharedPreferences(Constants.SHAREDPREFERENCES_FILE, MODE_PRIVATE);
        editor = sharedPreferences.edit();
        if (isAdmin) {
            DocumentReference docRef = database.collection(Constants.DATABASE_ADMIN).document(phoneNumber);
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Log.d(TAG, "DocumentSnapshot data admin: " + document.getData());
                            name = document.getString("name");
                            phone = document.getString("phone");
                            type = "admin";
                            triggerTimer();
                        } else {
                            showToast("You are not a valid admin yet please contact administrator", FancyToast.ERROR);
                        }
                    } else {
                        showToast("Could not fetch records", FancyToast.CONFUSING);
                    }
                }
            });
        } else {
            DocumentReference docRef = database.collection(Constants.DATABASE_USER).document(phoneNumber);
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Log.d(TAG, "DocumentSnapshot data user: " + document.getData());
                            name = document.getString("name");
                            phone = document.getString("phone");
                            type = "user";
                            triggerTimer();
                        } else {
                            showToast("You are not a valid user yet please contact administrator", FancyToast.ERROR);
                        }
                    } else {
                        showToast("Could not fetch records", FancyToast.CONFUSING);
                    }
                }
            });
        }
    }

    private void triggerTimer() {
        verifyAnim.playAnimation();
        int seconds = 30;
        btnRequestOtp.setEnabled(false);
        timer = new CountDownTimer(seconds * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int secondsLeft = (int) (millisUntilFinished / 1000);
                btnRequestOtp.setText(String.format("%02d", secondsLeft / 60) + ":" + String.format("%02d", secondsLeft % 60));
            }

            @Override
            public void onFinish() {
                btnRequestOtp.setEnabled(true);
                btnRequestOtp.setText("Request OTP");
                verifyAnim.pauseAnimation();
            }
        }.start();
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                requestOtp();
            }
        }, 1000 * 3);
    }

    private void showToast(String message, int type) {
        FancyToast.makeText(LoginActivity.this, message, FancyToast.LENGTH_SHORT, type, false).show();
    }
}
