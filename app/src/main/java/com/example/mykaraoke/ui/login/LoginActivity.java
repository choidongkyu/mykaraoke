package com.example.mykaraoke.ui.login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.mykaraoke.MainActivity;
import com.example.mykaraoke.R;
import com.example.mykaraoke.SplashActivity;
import com.example.mykaraoke.adapter.SongItem;
import com.example.mykaraoke.util.Config;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;

public class LoginActivity extends AppCompatActivity {
    private DatabaseReference ref;
    private EditText usernameEditText;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameEditText = findViewById(R.id.username);
        final Button loginButton = findViewById(R.id.login);

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        ref = db.getReference("users");
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                        //users에 해당하는 모든 데이터를 탐색
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            //입력된 닉네임이 데이터베이스에 있다면
                            if (snapshot.getKey().equals(usernameEditText.getText().toString())) {
                                String token = (String) snapshot.child("token").getValue(); //닉네임에 등록된 토큰값 얻어오기
                                //닉네임에 등록된 토큰 값이 현재 기기의 토큰값과 다르다면, 다른 기기에서 해당 닉네임을 사용하고 있으므로 무시
                                if(!token.equals(loadTokenFromSharedPref())) {
                                    Toast.makeText(getApplicationContext(), "다른기기에서 해당 닉네임을 사용하고 있습니다. 다시 시도하세요.", Toast.LENGTH_LONG).show();
                                    return;
                                }
                                Toast.makeText(getApplicationContext(), "접속하였습니다.", Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                                return;
                            }
                        }
                        registerId();
                        Toast.makeText(getApplicationContext(),"새로운 닉네임으로 등록하였습니다. 다시 접속하여주세요.",Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
    }

    //sharedPref에 저장된 token을 불러오는 메소드
    private String loadTokenFromSharedPref() {
        SharedPreferences sharedPreferences = getSharedPreferences("FirebaseFcm", MODE_PRIVATE); //fcm에 관련된 pref를 얻어옴
        return sharedPreferences.getString(Config.TOKEN_KEY, null);
    }

    private void registerId() {
        ref.child(usernameEditText.getText().toString()).child("token").setValue(loadTokenFromSharedPref());
    }
}