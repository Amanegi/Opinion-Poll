package com.pravesh.myapplication.common;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.pravesh.myapplication.R;
import com.pravesh.myapplication.admin.activities.AdminDashboardActivity;
import com.pravesh.myapplication.entities.Question;
import com.pravesh.myapplication.util.Constants;
import com.shashank.sony.fancytoastlib.FancyToast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VoteActivity extends AppCompatActivity {
    RecyclerView questionsRecyler;
    List<Question> questionList;
    FirebaseFirestore database;
    String addedBy;
    ImageView bckBtn;
    VoteAdapter voteAdapter;
    LinearLayout fetchingFrame;
    SharedPreferences sharedPreferences;
    public static final String TAG = VoteActivity.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vote);

        questionsRecyler = findViewById(R.id.questionRecycler);
        fetchingFrame = findViewById(R.id.fetchingFrameV);
        bckBtn = findViewById(R.id.btnBackV);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null && bundle.getString("type").equals("userType")) {
            bckBtn.setVisibility(View.GONE);
        } else {
            bckBtn.setVisibility(View.VISIBLE);
        }

        bckBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(VoteActivity.this, AdminDashboardActivity.class));
            }
        });

        questionsRecyler.setLayoutManager(new LinearLayoutManager(this));
        database = FirebaseFirestore.getInstance();
        sharedPreferences = getSharedPreferences(Constants.SHAREDPREFERENCES_FILE, MODE_PRIVATE);
        addedBy = sharedPreferences.getString("name", null) + " " + sharedPreferences.getString("phone", null);
        fetchQuestions();


    }

    private void fetchQuestions() {
        questionList = new ArrayList<>();
        database.collection(Constants.DATABASE_QUESTION)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String question = document.getString("questionText");
                                String option1 = document.getString("option1");
                                String option2 = document.getString("option2");
                                String option3 = document.getString("option3");
                                String option4 = document.getString("option4");
                                String questionId = document.getString("questionId");
                                Question questionModel = new Question(question, option1, option2, option3, option4, addedBy, questionId);
                                questionList.add(questionModel);
                            }
                            fetchingFrame.setVisibility(View.GONE);
                            Collections.reverse(questionList);
                            voteAdapter = new VoteAdapter(VoteActivity.this, questionList);
                            questionsRecyler.setAdapter(voteAdapter);
                        } else {
                            FancyToast.makeText(VoteActivity.this, "Error getting list of questions", FancyToast.LENGTH_SHORT,
                                    FancyToast.CONFUSING, false).show();
                        }
                    }
                });
    }
}
