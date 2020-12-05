package com.example.polling;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Handler;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterFragment extends Fragment {

    private Button btnRegister;
    private Toolbar toolbar;
    private EditText etName;
    private EditText etEmail;
    private EditText etPassword;
    private EditText etPasswordConfirm;
    private String error_message = "";
    private ImageView show_hide_password, show_hide_confirmation_password;

    public RegisterFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Registration");
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });


        btnRegister = view.findViewById(R.id.btnRegister);
        etName = view.findViewById(R.id.etName);
        etEmail = view.findViewById(R.id.etEmail);
        etPassword = view.findViewById(R.id.etPassword);
        etPasswordConfirm = view.findViewById(R.id.etConfirmPassword);
        show_hide_password = view.findViewById(R.id.show_hide_password);
        show_hide_confirmation_password = view.findViewById(R.id.show_hide_confirmation_password);

        btnRegister.setText(getString(R.string.btnRegister));
        etName.setHint(getString(R.string.etName));
        etEmail.setHint(getString(R.string.etEmail));
        etPassword.setHint(getString(R.string.etPassword));
        etPasswordConfirm.setHint(getString(R.string.etPasswordConfirm));

        etName.setText("");

        btnRegister.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                boolean got_error = false;

                if(etName.getText().toString().isEmpty() || etEmail.getText().toString().isEmpty() || etPassword.getText().toString().isEmpty()){
                    got_error = true;
                    error_message = "Please fill up the empty field";
                }
                else if(etPassword.getText().toString().length() < 6){
                    got_error = true;
                    error_message = "Password must be 6 character or more";
                }
                else if(!etPassword.getText().toString().equals(etPasswordConfirm.getText().toString())){
                    got_error = true;
                    error_message = "Password entered is different";
                }

                if(!got_error){
                    String email = etEmail.getText().toString();
                    String password = etPassword.getText().toString();
                    FirebaseAuth auth = FirebaseAuth.getInstance();
                    auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if (task.isSuccessful()) {
                                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                                        DatabaseReference myRef = database.getReference("User");

                                        User user = new User(etName.getText().toString(), etEmail.getText().toString());
                                        AuthResult result = (AuthResult) task.getResult();
                                        String uid = result.getUser().getUid();

                                        myRef.child(uid).setValue(user);

                                        Toast.makeText(getActivity(), "Account successfully registered", Toast.LENGTH_SHORT).show();

                                        Handler handler = new Handler();
                                        handler.postDelayed(new Runnable() {

                                            @Override
                                            public void run() {
                                                Fragment fragment = new LoginFragment();
                                                FragmentManager fm = getFragmentManager();
                                                FragmentTransaction ft = fm.beginTransaction();
                                                ft.setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit);

                                                ft.replace(R.id.fragmentFrame, fragment);
                                                ft.commit();
                                            }

                                        }, 1000);
                                    }
                                    else{
                                        Toast.makeText(getActivity(), "The account is already registered", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
                else{
                    Toast.makeText(getActivity(), error_message, Toast.LENGTH_SHORT).show();
                }
            }
        });

        showHide(show_hide_password, etPassword);
        showHide(show_hide_confirmation_password, etPasswordConfirm);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    public void showHide(final ImageView ivShowHide, final EditText etShowHide){
        ivShowHide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(etShowHide.getTransformationMethod().equals(PasswordTransformationMethod.getInstance())){
                    ivShowHide.setImageResource(R.drawable.ic_hide_password);
                    etShowHide.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }
                else{
                    ivShowHide.setImageResource(R.drawable.ic_show_password);
                    etShowHide.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });
    }
}