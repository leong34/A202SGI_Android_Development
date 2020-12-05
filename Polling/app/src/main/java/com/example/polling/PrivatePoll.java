package com.example.polling;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static android.content.ClipDescription.MIMETYPE_TEXT_PLAIN;

public class PrivatePoll extends Fragment {
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference myRef = database.getReference("Poll");
    private ImageView ivNext, ivPaste, ivClose;
    private EditText etCode;
    private String pasteData = "";

    public PrivatePoll() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_private_poll, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ivNext = view.findViewById(R.id.ivNext);
        ivPaste = view.findViewById(R.id.ivPaste);
        ivClose = view.findViewById(R.id.ivClose);
        etCode = view.findViewById(R.id.etCode);

        ivClose.setVisibility(View.GONE);

        ivNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!etCode.getText().toString().isEmpty()){
                    myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String path = etCode.getText().toString().trim();
                            if(snapshot.child(path).exists() && snapshot.child(path).child("status").getValue(String.class).equals("Available")){
                                Intent intent = new Intent(getContext(), PollDetail_1.class);
                                intent.putExtra("Question", snapshot.child(path).child("question").getValue(String.class));
                                intent.putExtra("PollId", snapshot.child(path).child("pollId").getValue(String.class));
                                intent.putExtra("CreatorId", snapshot.child(path).child("creatorUID").getValue(String.class));
                                intent.putExtra("CreatorName", snapshot.child(path).child("creatorName").getValue(String.class));
                                intent.putExtra("CreatedDate", snapshot.child(path).child("createdDate").getValue(String.class));
                                intent.putExtra("Visibility", snapshot.child(path).child("public").getValue(Boolean.class));
                                ArrayList<Tag> tagList = new ArrayList<>();
                                for(int i = 0; i < snapshot.child(path).child("tags").getChildrenCount(); i++){
                                    tagList.add(new Tag(snapshot.child(path).child("tags").child(""+i).child("tag").getValue(String.class)));
                                }
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
                                getContext().startActivity(intent);
                            } else if (snapshot.child(path).exists() && snapshot.child(path).child("status").getValue(String.class).equals("Deleted")){
                                Toast.makeText(getContext(), "Poll have been deleted", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), "Poll can't be found", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                } else {
                    Toast.makeText(getContext(), "Code can't be empty", Toast.LENGTH_SHORT).show();
                }
            }
        });

        ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);

        if (!(clipboard.hasPrimaryClip())) {
            ivPaste.setEnabled(false);
        } else if (!(clipboard.getPrimaryClipDescription().hasMimeType(MIMETYPE_TEXT_PLAIN))) {
            ivPaste.setEnabled(false);
        } else {
            ivPaste.setEnabled(true);
        }

        ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
        pasteData = item.getText().toString();

        ivPaste.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etCode.setText(etCode.getText().toString() + pasteData);
            }
        });

        ivClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etCode.setText("");
            }
        });

        etCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(etCode.getText().toString().isEmpty()){
                    ivClose.setVisibility(View.GONE);
                    ivPaste.setVisibility(View.VISIBLE);
                } else {
                    ivClose.setVisibility(View.VISIBLE);
                    ivPaste.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }
}