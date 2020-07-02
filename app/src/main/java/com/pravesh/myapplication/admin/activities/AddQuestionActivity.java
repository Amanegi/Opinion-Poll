package com.pravesh.myapplication.admin.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.pravesh.myapplication.R;
import com.pravesh.myapplication.util.Constants;
import com.shashank.sony.fancytoastlib.FancyToast;

import java.util.HashMap;
import java.util.Map;

public class AddQuestionActivity extends AppCompatActivity {
    EditText edtQuestionEng, edtOption1, edtOption2, edtOption3, edtOption4;
    FirebaseFirestore database;
    Button btnSubmit;
    ImageView bckBtn;
    LinearLayout savingLinear;
    LottieAnimationView savingAnim;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_question);
        edtQuestionEng = findViewById(R.id.edtQuestionEng);
        edtOption1 = findViewById(R.id.edtAns1);
        edtOption2 = findViewById(R.id.edtAns2);
        edtOption3 = findViewById(R.id.edtAns3);
        edtOption4 = findViewById(R.id.edtAns4);
        btnSubmit = findViewById(R.id.btnSubmitAQ);
        bckBtn = findViewById(R.id.btnBackAQ);
        savingLinear = findViewById(R.id.savingLinear);
        savingAnim = findViewById(R.id.savingAnim);

        database = FirebaseFirestore.getInstance();
        bckBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AddQuestionActivity.this, AdminDashboardActivity.class));
            }
        });
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String question = edtQuestionEng.getText().toString().trim();
                if (question.equals("")) {
                    edtQuestionEng.requestFocus();
                    showToast("Enter a question to proceed", FancyToast.ERROR);
                } else {
                    String option1 = edtOption1.getText().toString().trim();
                    String option2 = edtOption2.getText().toString().trim();
                    String option3 = edtOption3.getText().toString().trim();
                    String option4 = edtOption4.getText().toString().trim();
                    if (option1.equals("") || option2.equals("")) {
                        showToast("Enter atleast two options to proceed", FancyToast.ERROR);
                    } else {
                        addQuestion(question, option1, option2, option3, option4);
                        hideKeyboard();
                    }
                }
            }
        });
    }

    private void addQuestion(String question, String option1, String option2, String option3, String option4) {
        savingLinear.setVisibility(View.VISIBLE);
        savingAnim.playAnimation();
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHAREDPREFERENCES_FILE, MODE_PRIVATE);
        String addedBy = sharedPreferences.getString("name", null) + " " + sharedPreferences.getString("phone", null);
        int questionId = sharedPreferences.getInt("questionId", 1);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("questionId", questionId + 1);
        editor.apply();
        String questionCode = sharedPreferences.getString("phone", null) + " " + questionId;
        Map<String, Object> questionMap = new HashMap<>();
        questionMap.put("questionId",questionCode);
        questionMap.put("questionText", question);
        questionMap.put("option1", option1);
        questionMap.put("option2", option2);
        questionMap.put("option3", option3);
        questionMap.put("option4", option4);
        questionMap.put("addedBy", addedBy);
        questionMap.put("option1Selected",0);
        questionMap.put("option2Selected",0);
        questionMap.put("option3Selected",0);
        questionMap.put("option4Selected",0);

        database.collection(Constants.DATABASE_QUESTION).document(questionCode).
                set(questionMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                savingLinear.setVisibility(View.INVISIBLE);
                savingAnim.cancelAnimation();
                showToast("Question Added", FancyToast.SUCCESS);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                savingLinear.setVisibility(View.INVISIBLE);
                showToast("Cannot add question", FancyToast.ERROR);
            }
        });
    }

    private void showToast(String message, int type) {
        FancyToast.makeText(AddQuestionActivity.this, message, FancyToast.LENGTH_SHORT, type, false).show();
    }

    private void hideKeyboard() {
        InputMethodManager inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);

        if (getCurrentFocus() != null && inputManager != null)
            inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
    }
}
