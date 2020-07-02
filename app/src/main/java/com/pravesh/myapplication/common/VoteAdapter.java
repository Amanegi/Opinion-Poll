package com.pravesh.myapplication.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.pravesh.myapplication.R;
import com.pravesh.myapplication.entities.Question;
import com.pravesh.myapplication.util.Constants;
import com.shashank.sony.fancytoastlib.FancyToast;

import java.util.ArrayList;
import java.util.List;

public class VoteAdapter extends RecyclerView.Adapter<VoteAdapter.MyVoteHolder> {
    List<Question> questionList;
    Context context;
    FirebaseFirestore database;
    SharedPreferences sharedPreferences;
    Typeface tf;
    ArrayList<Integer> colors;

    public VoteAdapter(Context context, List<Question> questionList) {
        this.questionList = questionList;
        sharedPreferences = context.getSharedPreferences(Constants.SHAREDPREFERENCES_FILE, Context.MODE_PRIVATE);
        database = FirebaseFirestore.getInstance();
        colors = new ArrayList<>();
        for (int c : ColorTemplate.VORDIPLOM_COLORS) colors.add(c);
        tf = ResourcesCompat.getFont(context, R.font.poppins_regular);
        this.context = context;
    }

    @NonNull
    @Override
    public MyVoteHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_question_vote, parent, false);
        return new MyVoteHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyVoteHolder holder, int position) {
        final Question question = questionList.get(position);
        final boolean[] alreadyVoted = new boolean[1];
        final Handler handler = new Handler();
        holder.txtQuestion.setText(question.getQuestionText());
        final DocumentReference documentReference = database.collection(Constants.DATABASE_QUESTION).document(question.getQuestionId());
        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                List<String> numbersVoted = (List<String>) documentSnapshot.get("votedBy");
                if (numbersVoted != null && numbersVoted.contains(sharedPreferences.getString("phone", null))) {
                    alreadyVoted[0] = true;
                    drawChart(documentReference, question, holder.pieChart);
                    holder.pieChart.animateY(1200, Easing.EaseInOutQuad);

                } else {
                    alreadyVoted[0] = false;
                    holder.option1.setText(question.getOption1());
                    holder.option2.setText(question.getOption2());
                    Log.d("VoteAdapter", "onBindViewHolder: " + question.getOption3() + " " + question.getOption4());
                    if (question.getOption3().equals("")) {
                        holder.option3.setVisibility(View.GONE);
                    } else holder.option3.setText(question.getOption3());
                    if (question.getOption4().equals("")) {
                        holder.option4.setVisibility(View.GONE);
                    } else holder.option4.setText(question.getOption4());
                    holder.btnVote.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (holder.optionGroup.getCheckedRadioButtonId() == -1) {
                                FancyToast.makeText(context, "Please select an option", FancyToast.LENGTH_SHORT, FancyToast.CONFUSING, false)
                                        .show();
                            } else {
                                holder.btnVote.setEnabled(false);
                                if (holder.optionGroup.getCheckedRadioButtonId() == holder.option1.getId())
                                    documentReference.update("option1Selected", FieldValue.increment(1));
                                if (holder.optionGroup.getCheckedRadioButtonId() == holder.option2.getId())
                                    documentReference.update("option2Selected", FieldValue.increment(1));
                                if (holder.optionGroup.getCheckedRadioButtonId() == holder.option3.getId())
                                    documentReference.update("option3Selected", FieldValue.increment(1));
                                if (holder.optionGroup.getCheckedRadioButtonId() == holder.option4.getId())
                                    documentReference.update("option4Selected", FieldValue.increment(1));
                                String phone = sharedPreferences.getString("phone", null);
                                documentReference.update("votedBy", FieldValue.arrayUnion(phone)).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        FancyToast.makeText(context, "Voted", FancyToast.LENGTH_SHORT, FancyToast.SUCCESS, false).show();
                                        holder.optionGroup.setVisibility(View.GONE);
                                        holder.btnVote.setEnabled(false);
                                        holder.btnVote.setVisibility(View.INVISIBLE);
                                        drawChart(documentReference, question, holder.pieChart);
                                        holder.pieChart.setVisibility(View.VISIBLE);
                                        handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                holder.pieChart.animateY(1200, Easing.EaseInOutQuad);
                                            }
                                        }, 1500);
                                    }
                                });

                            }
                        }
                    });
                }

            }
        });

        holder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.txtQuestion.setMaxLines(Integer.MAX_VALUE);
                holder.moreBtn.setVisibility(View.GONE);
                holder.lessBtn.setVisibility(View.VISIBLE);
                if (alreadyVoted[0]) {
                    holder.pieChart.animateY(1200, Easing.EaseInOutQuad);
                    holder.pieChart.setVisibility(View.VISIBLE);
                } else {
                    holder.optionGroup.setVisibility(View.VISIBLE);
                    holder.btnVote.setVisibility(View.VISIBLE);
                }
            }
        });
        holder.lessBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.txtQuestion.setMaxLines(1);
                holder.lessBtn.setVisibility(View.GONE);
                holder.moreBtn.setVisibility(View.VISIBLE);
                if (alreadyVoted[0]) {
                    holder.pieChart.setVisibility(View.GONE);
                } else {
                    holder.optionGroup.setVisibility(View.GONE);
                    holder.btnVote.setVisibility(View.GONE);
                }
            }
        });

    }

    private void drawChart(DocumentReference documentReference, final Question question, final PieChart pieChart) {
        //creating a piechart
        final ArrayList entryList = new ArrayList<>();
        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                long option1Selected = (long) documentSnapshot.get("option1Selected");
                long option2Selected = (long) documentSnapshot.get("option2Selected");
                long option3Selected = (long) documentSnapshot.get("option3Selected");
                long option4Selected = (long) documentSnapshot.get("option4Selected");
                entryList.add(new PieEntry(option1Selected, question.getOption1()));
                entryList.add(new PieEntry(option2Selected, question.getOption2()));
                if (!question.getOption3().equals(""))
                    entryList.add(new PieEntry(option3Selected, question.getOption3()));
                if (!question.getOption4().equals("")) {
                    entryList.add(new PieEntry(option4Selected, question.getOption4()));
                }
                PieDataSet dataSet = new PieDataSet(entryList, "");
                dataSet.setSliceSpace(2f);
                dataSet.setSelectionShift(4f);
                dataSet.setColors(colors);
                PieData data = new PieData(dataSet);
                data.setValueFormatter(new PercentFormatter());
                data.setValueTextSize(14f);
                data.setValueTypeface(tf);
                data.setValueTextColor(Color.BLACK);
                pieChart.setData(data);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                FancyToast.makeText(context, "Failure", FancyToast.LENGTH_SHORT, FancyToast.ERROR, false).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return questionList.size();
    }

    public class MyVoteHolder extends RecyclerView.ViewHolder {
        TextView txtQuestion;
        ImageView moreBtn, lessBtn;
        RadioGroup optionGroup;
        RadioButton option1, option2, option3, option4;
        MaterialButton btnVote;
        PieChart pieChart;

        public MyVoteHolder(@NonNull View itemView) {
            super(itemView);
            txtQuestion = itemView.findViewById(R.id.txtQuestion);
            option1 = itemView.findViewById(R.id.radio1);
            option2 = itemView.findViewById(R.id.radio2);
            option3 = itemView.findViewById(R.id.radio3);
            option4 = itemView.findViewById(R.id.radio4);
            lessBtn = itemView.findViewById(R.id.lessBtn);
            optionGroup = itemView.findViewById(R.id.voteRadioGroup);
            moreBtn = itemView.findViewById(R.id.moreBtn);
            btnVote = itemView.findViewById(R.id.btnVote);
            pieChart = itemView.findViewById(R.id.pieChart);

            pieChart.setUsePercentValues(true);
            pieChart.setDrawHoleEnabled(true);
            pieChart.setDescription(null);
            pieChart.setHoleColor(Color.WHITE);
            pieChart.setTransparentCircleColor(Color.WHITE);
            pieChart.setTransparentCircleAlpha(110);
            pieChart.setRotationEnabled(true);
            pieChart.setRotationAngle(0);
            pieChart.setHighlightPerTapEnabled(true);
            pieChart.setHoleRadius(58f);
            pieChart.setEntryLabelColor(Color.BLACK);
            pieChart.setEntryLabelTypeface(tf);
            pieChart.setTransparentCircleRadius(61f);
            pieChart.setExtraOffsets(5, 10, 5, 5);
            pieChart.setDragDecelerationFrictionCoef(0.95f);

            Legend l = pieChart.getLegend();
            l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
            l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
            l.setOrientation(Legend.LegendOrientation.VERTICAL);
            l.setDrawInside(false);
            l.setEnabled(false);

        }
    }
}
