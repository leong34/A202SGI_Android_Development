package com.example.polling;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class PollCreatedDetail extends AppCompatActivity {

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
    Bitmap bm;
    BarChart chart;

    long validTime = 0;
    int participant = 0, viewed = 0;
    boolean rgDone = false, isPublic;
    SharedPreferences sharedPreferences;
    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poll_created_detail);

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
        chart = findViewById(R.id.barchart);
        intent = getIntent();

        sharedPreferences = getSharedPreferences("token", Context.MODE_PRIVATE);;
        userId = sharedPreferences.getString("uid", null);

        tvQuestion.setText(intent.getStringExtra("Question"));
        tvCreatedBy.setText(intent.getStringExtra("CreatorName"));
        tvCreatedDate.setText(" " + intent.getStringExtra("CreatedDate"));
        tvTags.setText(intent.getStringExtra("Tags"));
        isPublic = intent.getBooleanExtra("Visibility", true);

        if (isPublic) {
            ivVisibility.setImageResource(R.drawable.ic_public);
        } else {
            ivVisibility.setImageResource(R.drawable.ic_private);
        }

        myRef = database.getReference("Poll").child(intent.getStringExtra("PollId"));
        myRef1 = myRef.child("options");
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
                    long epoch = format.parse(format.format(currDate)).getTime();
                    long timeEnd = dataSnapshot.child("timeEnd").getValue(Long.class);
                    validTime = timeEnd - epoch;
                    if(validTime > 0){
                        new CountDownTimer(validTime,1000) {
                            @Override
                            public void onTick(long millisUntilFinished) {
                                long hours = TimeUnit.SECONDS.toHours(validTime);
                                long minute = TimeUnit.SECONDS.toMinutes(validTime) - (TimeUnit.SECONDS.toHours(validTime)* 60);
                                long second = TimeUnit.SECONDS.toSeconds(validTime) - (TimeUnit.SECONDS.toMinutes(validTime) *60);

                                tvCountDown.setText(" " + (hours >= 10 ? hours : "0" + hours) +
                                        ":" + (minute >= 10 ? minute : "0" + minute) + ":" + (second >= 10 ? second : "0" + second));
                                tvCountDown.setTextColor(Color.parseColor("#00DE02"));
                                validTime--;
                            }
                            @Override
                            public void onFinish() {
                                myRef.child("timeEnd").setValue(0);
                                tvCountDown.setText(" 00:00:00");
                                tvCountDown.setTextColor(Color.parseColor("#E82B2B"));
                            }
                        }.start();
                    }
                    else {
                        tvCountDown.setText(" 00:00:00");
                        tvCountDown.setTextColor(Color.parseColor("#E82B2B"));
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
            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                participant = 0;
                viewed = 0;
                optionsList = new ArrayList<>();
                for(DataSnapshot snapshot : dataSnapshot.child("options").getChildren()){
                    option = new Options(snapshot.child("optionAns").getValue(String.class), (int)snapshot.child("selectedUID").getChildrenCount());
                    participant += (int)snapshot.child("selectedUID").getChildrenCount();
                    optionsList.add(option);
                }

                tvViewed.setText(" " + dataSnapshot.child("viewed").getChildrenCount() + " Views");

                createChart(optionsList, intent.getStringExtra("Question"));
                tvParticipant.setText(" " + participant + " Voted");
                rgDone = true;
                Date currDate = new Date();

                RecyclerViewOptionsAdapter optionsAdapter = new RecyclerViewOptionsAdapter(getApplicationContext(), optionsList, participant, intent.getStringExtra("PollId"), userId, false, dataSnapshot.child("timeEnd").getValue(Long.class), currDate);
                rvOptions.setLayoutManager(new LinearLayoutManager(PollCreatedDetail.this));
                rvOptions.setAdapter(optionsAdapter);
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

    public void onBtnDelete(View view) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Are you sure to delete this poll?");
        alertDialogBuilder.setPositiveButton("yes",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        Toast.makeText(getApplicationContext(), "Poll Have Been Deleted", Toast.LENGTH_SHORT).show();
                        myRef = database.getReference("Poll").child(intent.getStringExtra("PollId"));
                        myRef.child("status").setValue("Deleted");
                        finish();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.created_poll, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.download:
                createPdfChart(intent.getStringExtra("PollId"), true, intent.getStringExtra("Question"), optionsList);
                break;
            case R.id.share:
                sharePdf();
                break;
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

    public void sharePdf(){
        String filepath = createPdfChart(intent.getStringExtra("PollId"), false, intent.getStringExtra("Question"), optionsList);
        File outputFile = new File(filepath);
        Uri uri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", outputFile);

        Intent share = new Intent();
        share.setAction(Intent.ACTION_SEND);
        share.setType("application/pdf");
        share.putExtra(Intent.EXTRA_STREAM, uri);

        Intent chooser = Intent.createChooser(share, "Share");
        if (share.resolveActivity(getPackageManager()) != null) {
            startActivity(chooser);
        }
    }

    public void createChart(ArrayList<Options> list, String title){
        ArrayList valueList = new ArrayList();
        ArrayList description = new ArrayList();
        ArrayList<IBarDataSet> dataSets = new ArrayList<IBarDataSet>();
        int[] color = ColorTemplate.JOYFUL_COLORS;
        int[] color1 = ColorTemplate.COLORFUL_COLORS;
        ArrayList<Integer> colors = new ArrayList<>();

        for(int i = 0; i < color.length; i++){
            colors.add(color[i]);
        }
        for(int i = 0; i < color1.length; i++){
            colors.add(color1[i]);
        }

        for(int i = 0; i < list.size(); i++){
            valueList = new ArrayList();
            valueList.add(new BarEntry(list.get(i).getCount(), 0));
            BarDataSet bardataset = new BarDataSet(valueList, "O" + (i+1));//list.get(i).getOptionAns());
            bardataset.setColor(colors.get(i));
            dataSets.add(bardataset);
        }
        description.add("");
        chart.animateY(0);

        BarData data = new BarData(description, dataSets);
        chart.setData(data);
        chart.setDescription("");
        chart.setDrawingCacheEnabled(true);
        chart.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        chart.layout(0, 0, 700, 450);
        chart.buildDrawingCache(true);
        bm = Bitmap.createBitmap(chart.getDrawingCache());
        chart.setDrawingCacheEnabled(false);
    }

    public void createChart1(ArrayList<Options> list, String title){
        ArrayList valueList = new ArrayList();
        ArrayList description = new ArrayList();

//        BarData data = null;

//        for(int i = 0; i < list.size(); i++){
////            valueList = new ArrayList();
//
//            valueList.add(new BarEntry(list.get(i).getCount(), i));
//            description.add("");
//
//            BarDataSet bardataset = new BarDataSet(valueList, list.get(i).getOptionAns());
//            bardataset.setColors(ColorTemplate.JOYFUL_COLORS);
//            chart.animateY(0);
//            data = new BarData(description, bardataset);
//        }
//
//        chart.setData(data);

        for(int i = 0; i < list.size(); i++) {
            valueList.add(new BarEntry(list.get(i).getCount(), i));
            description.add("");
        }

        BarDataSet bardataset = new BarDataSet(valueList, "");
        bardataset.setColors(ColorTemplate.JOYFUL_COLORS);
        chart.animateY(0);
        BarData data = new BarData(description, bardataset);

        chart.setData(data);
        chart.setDescription(title);

        chart.setDrawingCacheEnabled(true);
        chart.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        chart.layout(0, 0, 700, 500);

        chart.buildDrawingCache(true);
        bm = Bitmap.createBitmap(chart.getDrawingCache());
        chart.setDrawingCacheEnabled(false); // clear drawing cache
    }

    public String createPdfChart(String fname , boolean openFile, String title, ArrayList<Options> list){
        Document doc = new Document(PageSize.A4, 38, 38, 50, 38);
        doc.setPageSize(PageSize.LETTER.rotate());
        try {
            String directory_path = Environment.getExternalStorageDirectory().getPath() + "/pdf/";
            File file = new File(directory_path);

            if(!file.exists()){
                file.mkdirs();
            }

            File filepath = new File(directory_path, fname + ".pdf");

            PdfWriter.getInstance(doc, new FileOutputStream(filepath));
            doc.open();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bm.compress(Bitmap.CompressFormat.PNG, 100 , stream);
            Image myImg = Image.getInstance(stream.toByteArray());

            Font font = new Font(Font.FontFamily.TIMES_ROMAN, 20, Font.BOLD);
            doc.add(new Paragraph(title, font));
            doc.add(myImg);

            doc.newPage();
            doc.add(new Paragraph("Labels", font));
            font = new Font(Font.FontFamily.TIMES_ROMAN, 18);

            for(int i = 0; i < list.size(); i++){
                doc.add(new Paragraph("O" + (i+1) + ": "+ list.get(i).getOptionAns(), font));
            }
            if(openFile){
                openPdf(filepath.getPath());
            }
            return filepath.getPath();
        } catch (DocumentException | IOException e) {
            e.printStackTrace();
        } finally {
            doc.close();
        }
        return "";
    }

    public void openPdf(String text){
        File file = new File(text);
        if(file.exists()){
            Intent intent = new Intent(Intent.ACTION_VIEW);
            Uri uri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", file);
            intent.setDataAndType(uri, "application/pdf");
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            try{
                startActivity(intent);
            }
            catch (ActivityNotFoundException e){
            }
        }
    }
}