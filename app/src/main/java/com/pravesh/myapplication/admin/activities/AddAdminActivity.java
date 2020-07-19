package com.pravesh.myapplication.admin.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.pravesh.myapplication.R;
import com.pravesh.myapplication.admin.adapter.AdminAdapter;
import com.pravesh.myapplication.entities.Admin;
import com.pravesh.myapplication.util.Constants;
import com.pravesh.myapplication.util.CustomDialog;
import com.pravesh.myapplication.util.CustomDialogListener;
import com.shashank.sony.fancytoastlib.FancyToast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddAdminActivity extends AppCompatActivity {
    ImageView btnBack;
    MaterialButton btnAdd;
    AlertDialog dialog, dialogEdit;
    AlertDialog.Builder builder;
    EditText edtName, edtPhone, edtHouseNo;
    MaterialButton btnAddDialog;
    String addedBy;
    FirebaseFirestore database;
    RecyclerView adminRv;
    private List<Admin> adminList;
    private AdminAdapter adminAdapter;
    LinearLayout fetchingFrame, contentFrame;
    LottieAnimationView fetchingAnim;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_admin);

        btnBack = findViewById(R.id.btnBackAA);
        btnAdd = findViewById(R.id.btnAddAA);
        adminRv = findViewById(R.id.adminRecycler);
        contentFrame = findViewById(R.id.contentFrame);
        fetchingFrame = findViewById(R.id.fetchingFrame);
        fetchingAnim = findViewById(R.id.fetchingAnim);
        database = FirebaseFirestore.getInstance();

        fetchingAnim.playAnimation();
        SharedPreferences sharedPreferences = getSharedPreferences("data", MODE_PRIVATE);
        addedBy = sharedPreferences.getString("name", null) + " " + sharedPreferences.getString("phone", null);

        adminList = new ArrayList<>();
        fetchAdmins();

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("send_position"));

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AddAdminActivity.this, AdminDashboardActivity.class));
            }
        });

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                builder = new AlertDialog.Builder(AddAdminActivity.this);
                View view = getLayoutInflater().inflate(R.layout.layout_custom_input, null);
                edtName = view.findViewById(R.id.edtnameCI);
                edtPhone = view.findViewById(R.id.edtPhoneCI);
                edtHouseNo = view.findViewById(R.id.edtHouseCI);
                btnAddDialog = view.findViewById(R.id.btnAddCI);

                btnAddDialog.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String name = edtName.getText().toString().trim();
                        String phone = edtPhone.getText().toString().trim();
                        String houseNo = edtHouseNo.getText().toString().trim();
                        if (!name.equals("") && !phone.equals("") && !houseNo.equals("")) {
                            if (phone.length() == 10) {
                                hideKeyboard();
                                addAdmin(name, phone, houseNo, addedBy);
                                dialog.dismiss();
                            } else
                                FancyToast.makeText(AddAdminActivity.this, "Enter a valid phone number", FancyToast.LENGTH_LONG
                                        , FancyToast.ERROR, false).show();

                        } else {
                            FancyToast.makeText(AddAdminActivity.this, "Empty fields not allowed", FancyToast.LENGTH_LONG
                                    , FancyToast.ERROR, false).show();
                        }

                    }

                });
                builder.setView(view);
                dialog = builder.create();
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

    }

    public BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final int recyclerPosition = intent.getIntExtra("position", 0);
            builder = new AlertDialog.Builder(AddAdminActivity.this);
            View view = getLayoutInflater().inflate(R.layout.layout_popup_menu, null);
            LinearLayout edit = view.findViewById(R.id.linearEdit);
            LinearLayout delete = view.findViewById(R.id.linearDelete);
            edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    editAdminItem(recyclerPosition);
                    dialog.dismiss();
                }
            });
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    final CustomDialog confirmationDialog = new CustomDialog(AddAdminActivity.this);
                    confirmationDialog.setCancellable(false);
                    confirmationDialog.setTitle("Delete");
                    confirmationDialog.setMessage("Are you sure you want to delete this entry?");
                    confirmationDialog.setPositiveBackground("#9474F1");
                    confirmationDialog.setNegativeBackground("#386CEF");
                    confirmationDialog.setPositiveText("Delete");
                    confirmationDialog.setNegativeText("Cancel");
                    confirmationDialog.setPositiveListener(new CustomDialogListener() {
                        @Override
                        public void onClick() {
                            String phone = adminList.get(recyclerPosition).getPhone();
                            database.collection(Constants.DATABASE_ADMIN).document(phone)
                                    .delete()
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            FancyToast.makeText(AddAdminActivity.this, "Deleted", FancyToast.LENGTH_SHORT
                                                    , FancyToast.ERROR, false).show();
                                            adminList.remove(recyclerPosition);
                                            adminAdapter.notifyDataSetChanged();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            FancyToast.makeText(AddAdminActivity.this, "Error while deleting", FancyToast.LENGTH_SHORT
                                                    , FancyToast.ERROR, false).show();
                                        }
                                    });
                        }
                    });
                    confirmationDialog.setNegativeListener(new CustomDialogListener() {
                        @Override
                        public void onClick() {
                            confirmationDialog.hide();
                        }
                    });
                    confirmationDialog.show();
                }
            });
            builder.setView(view);
            dialog = builder.create();
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.show();
        }
    };

    private void editAdminItem(final int recyclerPosition) {
        Admin admin = adminList.get(recyclerPosition);
        builder = new AlertDialog.Builder(AddAdminActivity.this);
        View view = getLayoutInflater().inflate(R.layout.layout_custom_input, null);
        edtName = view.findViewById(R.id.edtnameCI);
        edtPhone = view.findViewById(R.id.edtPhoneCI);
        edtHouseNo = view.findViewById(R.id.edtHouseCI);
        btnAddDialog = view.findViewById(R.id.btnAddCI);

        edtName.setText(admin.getName());
        edtPhone.setText(admin.getPhone());
        edtPhone.setEnabled(false);
        edtHouseNo.setText(admin.getHouseNo());

        btnAddDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = edtName.getText().toString().trim();
                String phone = edtPhone.getText().toString().trim();
                String houseNo = edtHouseNo.getText().toString().trim();
                if (!name.equals("") && !phone.equals("") && !houseNo.equals("")) {
                    if (phone.length() == 10) {
                        hideKeyboard();
                        editAdmin(name, phone, houseNo, addedBy, recyclerPosition);
                        dialogEdit.dismiss();
                    } else
                        FancyToast.makeText(AddAdminActivity.this, "Enter a valid phone number", FancyToast.LENGTH_LONG
                                , FancyToast.ERROR, false).show();

                } else {
                    FancyToast.makeText(AddAdminActivity.this, "Empty fields not allowed", FancyToast.LENGTH_LONG
                            , FancyToast.ERROR, false).show();
                }
            }
        });
        builder.setView(view);
        dialogEdit = builder.create();
        dialogEdit.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialogEdit.show();
    }

    private void editAdmin(final String name, final String phone, final String houseNo, String addedBy, final int position) {
        Map<String, Object> admin = new HashMap<>();
        admin.put("name", name);
        admin.put("phone", phone);
        admin.put("houseNo", houseNo);
        admin.put("addedBy", addedBy);
        database.collection(Constants.DATABASE_ADMIN).document(phone).set(admin).
                addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        adminList.remove(position);
                        Admin admin1 = new Admin(name, phone, houseNo);
                        adminList.add(admin1);
                        adminAdapter.notifyDataSetChanged();
                        FancyToast.makeText(AddAdminActivity.this, "Edited", FancyToast.LENGTH_SHORT, FancyToast.SUCCESS, false)
                                .show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                FancyToast.makeText(AddAdminActivity.this, "Failure: " + e.getMessage(), FancyToast.LENGTH_LONG, FancyToast.ERROR, false)
                        .show();
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

    private void addAdmin(final String name, final String phone, final String houseNo, String addedBy) {
        Map<String, Object> admin = new HashMap<>();
        admin.put("name", name);
        admin.put("phone", phone);
        admin.put("houseNo", houseNo);
        admin.put("addedBy", addedBy);
        database.collection(Constants.DATABASE_ADMIN).document(phone).set(admin).
                addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Admin admin1 = new Admin(name, phone, houseNo);
                        adminList.add(admin1);
                        adminAdapter.notifyDataSetChanged();
                        FancyToast.makeText(AddAdminActivity.this, "Added", FancyToast.LENGTH_SHORT, FancyToast.SUCCESS, false)
                                .show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                FancyToast.makeText(AddAdminActivity.this, "Failure: " + e.getMessage(), FancyToast.LENGTH_LONG, FancyToast.ERROR, false)
                        .show();
            }
        });

    }

    private void fetchAdmins() {
        database.collection(Constants.DATABASE_ADMIN)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String name = document.getString("name");
                                String phoneNo = document.getString("phone");
                                String houseNo = document.getString("houseNo");
                                Admin admin = new Admin(name, phoneNo, houseNo);
                                adminList.add(admin);
                            }
                            adminRv.setLayoutManager(new LinearLayoutManager(AddAdminActivity.this));
                            Collections.sort(adminList, new Comparator<Admin>() {
                                @Override
                                public int compare(Admin o1, Admin o2) {
                                    return o1.getName().compareTo(o2.getName());
                                }
                            });
                            adminAdapter = new AdminAdapter(AddAdminActivity.this, adminList);
                            adminRv.setAdapter(adminAdapter);
                            fetchingFrame.setVisibility(View.GONE);
                            contentFrame.setVisibility(View.VISIBLE);
                            fetchingAnim.cancelAnimation();
                        } else {
                            FancyToast.makeText(AddAdminActivity.this, "Error getting list of admins", FancyToast.LENGTH_SHORT,
                                    FancyToast.CONFUSING, false).show();
                        }
                    }
                });

    }
}
