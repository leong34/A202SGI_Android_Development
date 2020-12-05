package com.example.polling;

import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class RecycleViewPollAdapter extends RecyclerView.Adapter<RecycleViewPollAdapter.ViewHolder> {

    private ArrayList<Poll> pollList;
    private LayoutInflater mInflater;
    private boolean created;

    public RecycleViewPollAdapter(Context context, ArrayList<Poll> pollList, boolean created){
        this.pollList = pollList;
        this.mInflater = LayoutInflater.from(context);
        this.created = created;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recycle_view_poll, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int descOrder = pollList.size() - position - 1;
        holder.tvQuestion.setText(pollList.get(descOrder).getQuestion());
        holder.tvCreator.setText(" " + pollList.get(descOrder).getCreatorName());
        holder.tvCreatedDate.setText(" " + pollList.get(descOrder).getCreatedDate());
        holder.tvParticipant.setText(" " + pollList.get(descOrder).getParticipant() + " Voted");
        holder.tvViewed.setText(" " + pollList.get(descOrder).getViewed() + " Views");
        ArrayList<Tag>tagList = pollList.get(descOrder).getTags();
        for(int i = 0; i < tagList.size(); i++){
            if(tagList.size() == 1)
                holder.tvTags.setText(holder.tvTags.getText().toString() + "#" + tagList.get(i).getTag());
            else{
                if(i != tagList.size()-1)
                    holder.tvTags.setText(holder.tvTags.getText().toString() + "#" + tagList.get(i).getTag() + ", ");
                else
                    holder.tvTags.setText(holder.tvTags.getText().toString() + "#" + tagList.get(i).getTag());
            }
        }

        if(pollList.get(descOrder).isPublic()){
            holder.ivVisibility.setImageResource(R.drawable.ic_public);
        } else {
            holder.ivVisibility.setImageResource(R.drawable.ic_private);
        }
    }

    @Override
    public int getItemCount() {
        return pollList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tvQuestion;
        TextView tvCreator;
        TextView tvCreatedDate;
        TextView tvParticipant;
        TextView tvViewed;
        TextView tvTags;
        ImageView ivVisibility;
        ImageView ivShare, ivLink, ivDownload, ivDelete;
        Bitmap bm;
        BarChart chart;
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef;

        public ViewHolder(@NonNull final View itemView) {
            super(itemView);
            tvQuestion = itemView.findViewById(R.id.tvQuestion);
            tvCreator = itemView.findViewById(R.id.tvCreator);
            tvCreatedDate = itemView.findViewById(R.id.tvCreatedDate);
            tvParticipant = itemView.findViewById(R.id.tvParticipant);
            tvViewed = itemView.findViewById(R.id.tvViewed);
            tvTags = itemView.findViewById(R.id.tvTags);
            ivVisibility = itemView.findViewById(R.id.ivVisibility);
            ivLink = itemView.findViewById(R.id.ivLink);
            ivDelete = itemView.findViewById(R.id.ivDelete);

            ivLink.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(itemView.getContext(), "Code copied to clipboard", Toast.LENGTH_SHORT).show();
                    copyToClipboard(pollList.get(pollList.size() - getAdapterPosition() - 1).getPollId());
                }
            });

            ivDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(itemView.getContext());
                    alertDialogBuilder.setMessage("Are you sure to delete this poll?");
                            alertDialogBuilder.setPositiveButton("yes",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface arg0, int arg1) {
                                            delete(pollList.get(pollList.size() - getAdapterPosition() - 1).getPollId());
                                        }
                                    });

                    alertDialogBuilder.setNegativeButton("No",new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });

                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                }
            });

            if(!created){
                ivDelete.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int descOrder = pollList.size() - getAdapterPosition() - 1;
            Poll curPoll = pollList.get(descOrder);

            Intent intent;
            if(created){
                intent = new Intent(v.getContext(), PollCreatedDetail.class);
            }
            else{
                intent = new Intent(v.getContext(), PollDetail_1.class);
            }
            intent.putExtra("Question", curPoll.getQuestion());
            intent.putExtra("PollId", curPoll.getPollId());
            intent.putExtra("CreatorId", curPoll.getCreatorUID());
            intent.putExtra("CreatorName", curPoll.getCreatorName());
            intent.putExtra("CreatedDate", curPoll.getCreatedDate());
            intent.putExtra("Visibility", curPoll.isPublic());

            ArrayList<Tag>tagList = curPoll.getTags();
            String passTag = "";
            for(int i = 0; i < tagList.size(); i++){
                if(tagList.size() == 1)
                    passTag += "#" + tagList.get(i).getTag();
                else{
                    if(i != tagList.size()-1)
                        passTag += "#" + tagList.get(i).getTag() + ", ";
                    else
                        passTag += "#" + tagList.get(i).getTag();
                }
            }
            intent.putExtra("Tags", passTag);
            v.getContext().startActivity(intent);
        }

        public void copyToClipboard(String text){
            ClipboardManager clipboard = (ClipboardManager) itemView.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clipData = ClipData.newPlainText("code", text);
            clipboard.setPrimaryClip(clipData);
        }

        public void delete(String pollId) {
            myRef = database.getReference("Poll").child(pollId);
            myRef.child("status").setValue("Deleted");
        }
    }
}
