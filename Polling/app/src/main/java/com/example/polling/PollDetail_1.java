package com.example.polling;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.EventListener;
import java.util.concurrent.TimeUnit;

public class PollDetail_1 extends AppCompatActivity {

    TextView tvQuestion;
    TextView tvCreatedBy;
    TextView tvCreatedDate;
    TextView tvParticipant;
    TextView tvCountDown;
    TextView tvViewed;
    TextView tvTags;
    ImageView ivVisibility;
    Intent intent;
    Toolbar toolbar;
    RecyclerView rvOptions;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef, myRef1;
    ArrayList<Options> optionsList;
    Options option;

    long validTime = 0;
    int participant = 0, viewed = 0;
    boolean rgDone = false, isPublic;
    String status;
    SharedPreferences sharedPreferences;
    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poll_detail_1);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Details");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tvQuestion = findViewById(R.id.tvQuestion);
        tvCreatedBy = findViewById(R.id.tvCreatedBy);
        tvCreatedDate = findViewById(R.id.tvCreatedDate);
        tvParticipant = findViewById(R.id.tvParticipant);
        tvViewed = findViewById(R.id.tvViewed);
        tvTags = findViewById(R.id.tvTags);
        rvOptions = findViewById(R.id.rvOptions);
        tvCountDown = findViewById(R.id.tvCountDown);
        ivVisibility = findViewById(R.id.ivVisibility);
        intent = getIntent();

        sharedPreferences = getSharedPreferences("token", Context.MODE_PRIVATE);;
        userId = sharedPreferences.getString("uid", null);

        tvQuestion.setText(intent.getStringExtra("Question"));
        tvCreatedBy.setText(" " + intent.getStringExtra("CreatorName"));
        tvCreatedDate.setText(" " + intent.getStringExtra("CreatedDate"));
        tvTags.setText(intent.getStringExtra("Tags"));
        isPublic = intent.getBooleanExtra("Visibility", true);

        if (isPublic) {
            ivVisibility.setImageResource(R.drawable.ic_public);
        } else {
            ivVisibility.setImageResource(R.drawable.ic_private);
        }

        myRef = database.getReference("Poll").child(intent.getStringExtra("PollId"));
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean visited = false;
                for(DataSnapshot snapshot : dataSnapshot.child("viewed").getChildren()) {
                    if(snapshot.child("UID").getValue(String.class).equalsIgnoreCase(userId)){
                        visited = true;
                        break;
                    }
                }
                if(!visited){
                    myRef.child("viewed").child(myRef.push().getKey()).child("UID").setValue(userId);
                }

                Date currDate = new Date();

                try {
                    SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                    long epoch = format.parse(format.format(currDate)).getTime() / 1000;
                    long timeEnd = dataSnapshot.child("timeEnd").getValue(Long.class);
                    validTime = timeEnd - epoch;
                    if(validTime > 0){
                        new CountDownTimer(validTime * 1000,1000) {
                            @Override
                            public void onTick(long millisUntilFinished) {
                                int day = (int) TimeUnit.SECONDS.toDays(validTime);
                                long hours = TimeUnit.SECONDS.toHours(validTime);
                                long minute = TimeUnit.SECONDS.toMinutes(validTime) - (TimeUnit.SECONDS.toHours(validTime)* 60);
                                long second = TimeUnit.SECONDS.toSeconds(validTime) - (TimeUnit.SECONDS.toMinutes(validTime) *60);

                                tvCountDown.setText(" " + (hours >= 10 ? hours : "0" + hours) + ":" + (minute >= 10 ? minute : "0" + minute) + ":" + (second >= 10 ? second : "0" + second));
                                tvCountDown.setTextColor(Color.parseColor("#5FED28"));
                                validTime--;
                            }
                            @Override
                            public void onFinish() {
                                myRef.child("timeEnd").setValue(0);
                                tvCountDown.setText(" 00:00:00");
                                tvCountDown.setTextColor(Color.parseColor("#FF5959"));
                            }
                        }.start();
                    }
                    else {
                        tvCountDown.setText(" 00:00:00");
                        tvCountDown.setTextColor(Color.parseColor("#FF5959"));
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot1) {
                for(DataSnapshot snapshot : snapshot1.getChildren()){
                    status = snapshot1.child("status").getValue(String.class);
                }

                for(DataSnapshot snapshot : snapshot1.getChildren()){

                    myRef = database.getReference("Poll").child(intent.getStringExtra("PollId"));

                    myRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            optionsList = new ArrayList<>();
                            viewed = 0;
                            participant = 0;
                            for(DataSnapshot snapshot : dataSnapshot.child("options").getChildren()){
                                option = new Options(snapshot.child("optionAns").getValue(String.class), (int)snapshot.child("selectedUID").getChildrenCount());
                                participant += (int)snapshot.child("selectedUID").getChildrenCount();
                                optionsList.add(option);
                            }

                            tvViewed.setText(" " + dataSnapshot.child("viewed").getChildrenCount() + " Views");
                            tvParticipant.setText(" " + participant + " Voted");
                            rgDone = true;

                            Date currDate = new Date();

                            RecyclerViewOptionsAdapter optionsAdapter;
                            if(status.equals("Deleted")){
                                getSupportActionBar().setTitle("Details (Deleted)");
                                Toast.makeText(PollDetail_1.this, "Poll Have Been Deleted", Toast.LENGTH_SHORT).show();
                                optionsAdapter = new RecyclerViewOptionsAdapter(getApplicationContext(), optionsList, participant, intent.getStringExtra("PollId"), userId, true, dataSnapshot.child("timeEnd").getValue(Long.class), currDate);
                            }
                            else{
                                getSupportActionBar().setTitle("Details");
                                optionsAdapter = new RecyclerViewOptionsAdapter(getApplicationContext(), optionsList, participant, intent.getStringExtra("PollId"), userId, false, dataSnapshot.child("timeEnd").getValue(Long.class), currDate);
                            }

                            rvOptions.setLayoutManager(new LinearLayoutManager(PollDetail_1.this));
                            rvOptions.setAdapter(optionsAdapter);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.poll, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.shareableLink:
                Toast.makeText(this, "Code copied to clipboard", Toast.LENGTH_SHORT).show();
                copyToClipboard(intent.getStringExtra("PollId"));
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void copyToClipboard(String text){
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("code", text);
        clipboard.setPrimaryClip(clipData);
    }
}