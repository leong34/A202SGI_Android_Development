package com.example.polling;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class CreatedPoll extends Fragment {
    View v;
    LinearLayout cbGroup;
    RecyclerView recyclerView;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("Poll");
    ArrayList<Poll> pollList = new ArrayList<>();
    ArrayList<String> filterList = new ArrayList<>();
    SharedPreferences sharedPreferences;
    Poll tempPoll;
    int totalParticipant = 0, viewed;

    private SearchView searchView = null;
    private SearchView.OnQueryTextListener queryTextListener;

    public CreatedPoll() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v =  inflater.inflate(R.layout.fragment_home, container, false);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sharedPreferences = this.getActivity().getSharedPreferences("token", Context.MODE_PRIVATE);
        getFilter();
        getData("", filterList);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.search_add_poll, menu);
        MenuItem searchItem = menu.findItem(R.id.search);
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);

        if (searchItem != null) {
            searchView = (SearchView) searchItem.getActionView();
        }
        if (searchView != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
            searchView.setQueryHint("Question, Name");

            queryTextListener = new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextChange(String newText) {
                    getData(newText, filterList);
                    return true;
                }
                @Override
                public boolean onQueryTextSubmit(String query) {
                    getData(query, filterList);
                    return true;
                }
            };

            searchView.setOnQueryTextListener(queryTextListener);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search:
                // Not implemented here
                return false;
            case R.id.create_poll:
                Intent intent = new Intent(getContext(), CreatePoll.class);
                startActivity(intent);
                break;
            default:
                break;
        }
        searchView.setOnQueryTextListener(queryTextListener);
        return super.onOptionsItemSelected(item);
    }

    public void getData(final String filter, final ArrayList<String> filterList){
        myRef = database.getReference("Poll");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                pollList.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    if(snapshot.child("creatorUID").getValue(String.class).equals(sharedPreferences.getString("uid", null)) && snapshot.child("status").getValue(String.class).equals("Available")){
                        if(snapshot.child("question").getValue(String.class).toLowerCase().contains(filter.toLowerCase()) || snapshot.child("creatorName").getValue(String.class).toLowerCase().contains(filter.toLowerCase())) {
                            if(filterList.size() != 0){
                                boolean haveFilter = false;
                                int match = 0;

                                for(int i = 0; i < snapshot.child("tags").getChildrenCount(); i++){
                                    if(filterList.contains(snapshot.child("tags").child(""+i).child("tag").getValue(String.class))){
                                        match++;
                                    }
                                    if(match == filterList.size()){
                                        haveFilter = true;
                                    }
                                }

                                if(!haveFilter){
                                    continue;
                                }
                            }

                            totalParticipant = 0;
                            for(int i = 0; i < snapshot.child("options").getChildrenCount(); i++){
                                totalParticipant += snapshot.child("options").child("" + i).child("selectedUID").getChildrenCount();
                            }
                            tempPoll = new Poll(snapshot.child("pollId").getValue(String.class), snapshot.child("creatorUID").getValue(String.class), snapshot.child("creatorName").getValue(String.class), snapshot.child("question").getValue(String.class), null, totalParticipant, snapshot.child("createdDate").getValue(String.class), snapshot.child("public").getValue(Boolean.class));

                            ArrayList<Tag> tagList = new ArrayList<>();
                            for(int i = 0; i < snapshot.child("tags").getChildrenCount(); i++){
                                tagList.add(new Tag(snapshot.child("tags").child("" + i).child("tag").getValue(String.class)));
                            }

                            tempPoll.setTags(tagList);

                            if(snapshot.child("viewed").exists()){
                                tempPoll.setViewed((int) snapshot.child("viewed").getChildrenCount());
                            } else {
                                tempPoll.setViewed(0);
                            }
                            pollList.add(tempPoll);
                        }
                    }
                }
                recyclerView = v.findViewById(R.id.rvHome);
                if (getActivity()!=null){
                    RecycleViewPollAdapter pollAdapter = new RecycleViewPollAdapter(getActivity(), pollList, true);
                    recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                    recyclerView.setAdapter(pollAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void getFilter(){
        cbGroup = v.findViewById(R.id.cbGroup);
        myRef = database.getReference("Tag");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int i = 0;
                filterList.clear();
                cbGroup.removeAllViews();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    if(getContext() != null){
                        i++;
                        final CheckBox cb = new CheckBox(getContext());
                        cb.setText(snapshot.child("name").getValue(String.class));
                        cb.setButtonDrawable(R.drawable.cb_custom);

                        Resources res = getResources();
                        Drawable drawable = ResourcesCompat.getDrawable(res, R.drawable.cb_custom, null);
                        cb.setBackground(drawable);

                        cb.setTextColor(getResources().getColorStateList(R.color.cb_text_color));
                        cb.setTypeface(Typeface.DEFAULT_BOLD);
                        cb.setGravity(Gravity.CENTER);
                        cb.setPadding(100, 35, 100,35);

                        cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                if(isChecked){
                                    filterList.add(cb.getText().toString());
                                } else {
                                    filterList.remove(cb.getText().toString());
                                }

                                if(!searchView.getQuery().toString().isEmpty()){
                                    getData(searchView.getQuery().toString(), filterList);
                                } else {
                                    getData("", filterList);
                                }

                            }
                        });

                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT);

                        if(i != dataSnapshot.getChildrenCount())
                            params.rightMargin = 25;

                        cbGroup.addView(cb, params);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}