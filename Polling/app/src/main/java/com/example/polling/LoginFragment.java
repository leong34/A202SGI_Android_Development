package com.example.polling;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Handler;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.data.DataBufferSafeParcelable;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginFragment extends Fragment {

    private Button btnLogin;
    private Button btnRegister;
    private TextView tvMainTitle_1;
    private TextView tvMainTitle_2;
    private EditText etEmail;
    private EditText etPassword;
    private ImageView hideShowImage;
    SharedPreferences sharedPreferences;
    String uid;

    public LoginFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sharedPreferences = getActivity().getSharedPreferences("token", Context.MODE_PRIVATE);

        btnLogin = (Button)view.findViewById(R.id.btnLogin);
        btnRegister = (Button)view.findViewById(R.id.btnRegister);
        tvMainTitle_1 = (TextView)view.findViewById(R.id.tvMainTitle_1);
        tvMainTitle_2 = (TextView)view.findViewById(R.id.tvMainTitle_2);
        etEmail = view.findViewById(R.id.etEmail);
        etPassword = view.findViewById(R.id.etPassword);
        hideShowImage = view.findViewById(R.id.show_hide_btn);

        btnLogin.setText(getString(R.string.btnLogin));
        btnRegister.setText(getString(R.string.btnRegisterAccount));
        tvMainTitle_1.setText(getString(R.string.tvMainTitle_1));
        tvMainTitle_2.setText(getString(R.string.tvMainTitle_2));

        btnLogin.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(etEmail.getText().toString().isEmpty() || etPassword.getText().toString().isEmpty()){
                    Toast.makeText(getActivity(), "Please enter your email and password", Toast.LENGTH_SHORT).show();
                }
                else{
                    String email = etEmail.getText().toString();
                    String password = etPassword.getText().toString();

                    FirebaseAuth auth = FirebaseAuth.getInstance();
                    auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(Task task) {
                                    if (task.isSuccessful()) {
                                        final SharedPreferences.Editor editor = sharedPreferences.edit();
                                        AuthResult result = (AuthResult) task.getResult();
                                        uid = result.getUser().getUid();
                                        editor.putString("uid", uid);
                                        editor.commit();

                                        Toast.makeText(getActivity(), "You have successfully login", Toast.LENGTH_SHORT).show();

                                        Handler handler = new Handler();
                                        handler.postDelayed(new Runnable() {

                                            @Override
                                            public void run() {
                                                Intent intent = new Intent(getActivity(), HomeActivity.class);
                                                startActivity(intent);
                                            }

                                        }, 0);
                                    }
                                    else{
                                        Toast.makeText(getActivity(), "Please check the email or password", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                loadFragment(new RegisterFragment(), true);
            }
        });

        hideShowImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(etPassword.getTransformationMethod().equals(PasswordTransformationMethod.getInstance())){
                    hideShowImage.setImageResource(R.drawable.ic_hide_password);
                    etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }
                else{
                    hideShowImage.setImageResource(R.drawable.ic_show_password);
                    etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    public void loadFragment(Fragment fragment, boolean addToBack){
        FragmentManager fm = getActivity().getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit);
        ft.replace(R.id.fragmentFrame, fragment);
        if(addToBack)
            ft.addToBackStack(null);
        ft.commit();
    }
}