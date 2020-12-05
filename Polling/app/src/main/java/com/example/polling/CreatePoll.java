package com.example.polling;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CreatePoll extends AppCompatActivity {

    Toolbar toolbar;
    LinearLayout parentAnsLayout;
    ImageView ivVisibility;
    ArrayList<String>etOptions;
    ArrayList<String> tagList;
    EditText tempOption;
    EditText etQuestion;
    EditText etTag;
    TextView tvDateTime;
    Button createBtn;
    SharedPreferences sharedPreferences;
    String username = "";
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference();

    ArrayList<Options> optionList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_poll);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Poll");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        parentAnsLayout = findViewById(R.id.parentAnsLayout);
        sharedPreferences = getSharedPreferences("token", Context.MODE_PRIVATE);
        tvDateTime = findViewById(R.id.tvDateTime);
        ivVisibility = findViewById(R.id.ivVisibility);
        createBtn = findViewById(R.id.createBtn);
        etTag = findViewById(R.id.etTag);
        createBtn.setClickable(true);
    }

    public void onAdd(View view) {
        boolean error = false;
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View rowView = inflater.inflate(R.layout.ans_layout, null);
        for (int i = 0; i < parentAnsLayout.getChildCount(); i++){
            tempOption = parentAnsLayout.getChildAt(i).findViewById(R.id.etOption);
            if(tempOption.getText().toString().isEmpty()){
                error = true;
            }
        }
        if(error){
            Toast.makeText(this, "Please fill the empty options", Toast.LENGTH_SHORT).show();
        }
        else{
            if(parentAnsLayout.getChildCount() <= 5)
                parentAnsLayout.addView(rowView, parentAnsLayout.getChildCount());
            else
                Toast.makeText(this, "You have reach max number of options", Toast.LENGTH_SHORT).show();
        }
    }

    public void onDelete(View view) {
        parentAnsLayout.removeView((View) view.getParent());
    }

    public void onCreatePoll(View view) {
        boolean error = false;
        etOptions = new ArrayList<String>();
        etQuestion = findViewById(R.id.etQuestion);

        for (int i = 0; i < parentAnsLayout.getChildCount(); i++){
            tempOption = parentAnsLayout.getChildAt(i).findViewById(R.id.etOption);
            if(!tempOption.getText().toString().trim().isEmpty()){
                etOptions.add(tempOption.getText().toString().trim());
            }
            else{
                error = true;
                Toast.makeText(this, "Please fill the empty option", Toast.LENGTH_SHORT).show();
                break;
            }
        }

        if(parentAnsLayout.getChildCount() < 2 && !error){
            error = true;
            Toast.makeText(this, "Please add more options", Toast.LENGTH_SHORT).show();
        }

        if(tvDateTime.getText().toString().equals("-")){
            error = true;
            Toast.makeText(this, "Please select an end time", Toast.LENGTH_SHORT).show();
        }

        if(etQuestion.getText().toString().trim().isEmpty()){
            error = true;
            Toast.makeText(this, "Please state your question", Toast.LENGTH_SHORT).show();
        }

        tagList = exploitTag(etTag.getText().toString().trim());

        if(tagList.isEmpty()){
            error = true;
            Toast.makeText(this, "Please make sure tag is all start with # and not empty", Toast.LENGTH_SHORT).show();
        }

        if(!error){
            final String userId = sharedPreferences.getString("uid", null);
            for(int i = 0; i < etOptions.size(); i++){
                optionList.add(new Options(etOptions.get(i)));
            }

            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    myRef = database.getReference("Tag");
                    if(dataSnapshot.child("Tag").exists()){
                        for (int i = 0; i < tagList.size(); i++) {
                            boolean found = false;
                            for(DataSnapshot snapshot : dataSnapshot.child("Tag").getChildren()){
                                if(snapshot.child("name").getValue(String.class).equalsIgnoreCase(tagList.get(i))){
                                    found = true;
                                    break;
                                }
                            }

                            if(!found){
                                myRef.child(myRef.push().getKey()).child("name").setValue(tagList.get(i));
                            }
                        }
                    }
                    else {
                        for (int i = 0; i < tagList.size(); i++) {
                            myRef.child(myRef.push().getKey()).child("name").setValue(tagList.get(i));
                        }
                    }

                    for(DataSnapshot snapshot : dataSnapshot.child("User").getChildren()){
                        if(snapshot.getKey().equals(userId)){
                            username = snapshot.child("name").getValue(String.class);

                            boolean isPublic = ivVisibility.getDrawable().getConstantState().equals(getResources().getDrawable(R.drawable.ic_public).getConstantState()) ? true : false;

                            Poll poll = new Poll(myRef.push().getKey(), userId, etQuestion.getText().toString().trim(), optionList, username, isPublic);

                            ArrayList<Tag> tags = new ArrayList<>();

                            for(int i = 0; i < tagList.size(); i++){
                                tags.add(new Tag(tagList.get(i)));
                            }

                            poll.setTags(tags);

                            myRef = database.getReference("Poll");

                            try {
                                long epoch = new SimpleDateFormat("dd-MM-yyyy HH:mm").parse(tvDateTime.getText().toString()).getTime() / 1000;
                                myRef.child(poll.getPollId()).setValue(poll);
                                myRef.child(poll.getPollId()).child("timeEnd").setValue(epoch);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                            //removing useless data from database
                            myRef.child(poll.getPollId()).child("participant").removeValue();
                            for(int i = 0; i < poll.getOptions().size(); i++){
                                myRef.child(poll.getPollId()).child("options").child("" + i).child("count").removeValue();
                            }

                            Toast.makeText(CreatePoll.this, "Poll have been created", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }


    private void datePicker(){
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
//                String date = getResources().getString(R.string.date_format);
                String formatDate = String.format("%d-%d-%d", dayOfMonth, month + 1, year);
                timePicker(formatDate);
            }
        }, year, month, dayOfMonth);
        long now = System.currentTimeMillis() - 1000;
        calendar.add(Calendar.MONTH, 1);
        long maxDate = calendar.getTimeInMillis();
        datePickerDialog.getDatePicker().setMinDate(now);
        datePickerDialog.getDatePicker().setMaxDate(maxDate); //After one month from now
        datePickerDialog.show();
    }

    private void timePicker(final String date){
        final Calendar calendar = Calendar.getInstance();
        final int hour = calendar.get(Calendar.HOUR_OF_DAY);
        final int minute = calendar.get(Calendar.MINUTE);
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
        Date currDate = new Date();
        if(format.format(currDate).equals(date)){
            RangeTimePickerDialog timePickerDialog = new RangeTimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
//                String time = getResources().getString(R.string.time_format);
                    String formatTime = String.format("%d:%s", hourOfDay, minute >= 10 ? ("" + minute) : ("0" + minute));
                    String dateTime = date + "   " + formatTime;
                    tvDateTime.setText(dateTime);
                }
            }, hour + 1, minute, false);
            timePickerDialog.setMin(hour + 1, minute);
            timePickerDialog.show();
        }
        else {
            RangeTimePickerDialog timePickerDialog = new RangeTimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
//                String time = getResources().getString(R.string.time_format);
                    String formatTime = String.format("%d:%s", hourOfDay, minute >= 10 ? ("" + minute) : ("0" + minute));
                    String dateTime = date + "   " + formatTime;
                    tvDateTime.setText(dateTime);
                }
            }, hour + 1, minute, false);
            timePickerDialog.show();
        }
    }

    public void setTime(View view) {
        datePicker();
    }

    public void chgVisibility(View view) {
        if(ivVisibility.getDrawable().getConstantState().equals(getResources().getDrawable(R.drawable.ic_public).getConstantState()))
            ivVisibility.setImageResource(R.drawable.ic_private);
        else
            ivVisibility.setImageResource(R.drawable.ic_public);
    }

    public ArrayList<String> exploitTag(String tags){
        ArrayList<String> tag = new ArrayList<>();
        if(tags.contains(", ")){
            String [] temp = tags.replaceAll(" ", "").split(",");

            for(int i = 0; i < temp.length; i++){
                if(!temp[i].startsWith("#") || temp[i].replace("#","").trim().isEmpty()){
                    tag.clear();
                    break;
                }
                else{
                    tag.add(temp[i].replace("#","").trim());
                }
            }
        } else {
            if(tags.startsWith("#") && !tags.replaceAll("#", "").trim().isEmpty()){
                tag.add(tags.replaceAll("#", "").trim());
            }
        }

        return tag;
    }
}