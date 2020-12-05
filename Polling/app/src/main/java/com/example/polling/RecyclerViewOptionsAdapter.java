package com.example.polling;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class RecyclerViewOptionsAdapter extends RecyclerView.Adapter<RecyclerViewOptionsAdapter.ViewHolder> {

    private ArrayList<Options> optionsList;
    private LayoutInflater mInflater;
    private int totalCount;
    private String pollId;
    private String userId;
    private boolean userSelected = false, deleted;
    private Long timeEnd;
    private Date currDate;
    static int i = 0;

    long validTime = 0;

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference myRef, myRef1, myRef2, myRef3;

    public RecyclerViewOptionsAdapter(Context context, ArrayList<Options> optionsList, int totalCount, String pollId, String userId, boolean deleted, Long timeEnd, Date currDate){
        this.optionsList = optionsList;
        this.mInflater = LayoutInflater.from(context);
        this.totalCount = totalCount;
        this.pollId = pollId;
        this.userId = userId;
        this.deleted = deleted;
        this.timeEnd = timeEnd;
        this.currDate = currDate;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recycle_view_options, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        myRef = database.getReference("Poll").child(pollId);
        myRef = myRef.child("options");
        myRef3 = myRef;

        float percentage = ((float)((float)optionsList.get(position).getCount() / (float)totalCount)) * 100.0f;

        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT);
        param.weight = percentage;
        holder.poll_bg.setLayoutParams(param);
        holder.tvOptions.setText(optionsList.get(position).getOptionAns());
        if(percentage % 1 == 0){
            holder.tvPercentage.setText("" + (int)(percentage) + "%");
        }
        else if (percentage > 0){
            holder.tvPercentage.setText("" + String.format("%.2f", percentage) + "%");
        }
        else{
            holder.tvPercentage.setText("0%");
        }

        try {
            SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            long epoch = format.parse(format.format(currDate)).getTime() / 1000;

            validTime = timeEnd - epoch;
            if(validTime < 0){
                deleted = true;
            }

            myRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(!userSelected){
                        for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            for (DataSnapshot snapshot1 : snapshot.child("selectedUID").getChildren()) {
                                if (snapshot1.child("UID").getValue(String.class).equals(userId)) {
                                    userSelected = true;
                                    break;
                                }
                            }
                        }
                    }

                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                        if(Integer.parseInt(snapshot.getKey()) == position){
                            for(DataSnapshot snapshot1 : snapshot.child("selectedUID").getChildren()){
                                if(snapshot1.child("UID").getValue(String.class).equals(userId)){
                                    holder.poll_bg.setBackgroundColor(Color.parseColor("#48A3FC"));
                                    holder.tvOptions.setTextColor(Color.parseColor("#404040"));
                                    holder.tvPercentage.setTextColor(Color.parseColor("#404040"));
                                }
                            }
                            if(userSelected || deleted){
                                holder.itemView.setClickable(false);
                            }
                        }
                    }
                }


                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return optionsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tvOptions, tvPercentage;
        View poll_bg;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOptions = itemView.findViewById(R.id.tvOptions);
            tvPercentage = itemView.findViewById(R.id.tvPercentage);
            poll_bg = itemView.findViewById(R.id.poll_bg);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            myRef = database.getReference("Poll").child(pollId);
            myRef = myRef.child("options");
            myRef1 = myRef.child("" + getAdapterPosition());
            myRef1.child("selectedUID").child(myRef1.push().getKey()).child("UID").setValue(userId);
        }
    }
}
