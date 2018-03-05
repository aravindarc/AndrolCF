package com.example.aravinda.androlcf;

import android.content.Context;

import android.content.SharedPreferences;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {
    private static final String myPreferences = "myUser";
    private static final String[] storedValuesIds = {"userId", "userName", "password"};
    SharedPreferences sharedPreferences;

    EditText userId, userName, password;
    Button saveButton;

    public boolean isAllFieldsFull;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        userId = findViewById(R.id.userId);
        userName = findViewById(R.id.userName);
        password = findViewById(R.id.password);
        saveButton = findViewById(R.id.save);

        userId.addTextChangedListener(watcher);
        userName.addTextChangedListener(watcher);
        password.addTextChangedListener(watcher);

        sharedPreferences   = getSharedPreferences(myPreferences, Context.MODE_PRIVATE);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (isAllFieldsFull) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();

                    editor.putString(storedValuesIds[0], userId.getText().toString());
                    editor.putString(storedValuesIds[1], userName.getText().toString());
                    editor.putString(storedValuesIds[2], password.getText().toString());
                    editor.commit();

                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "All fields need to be correctly filled in or connection will not be established!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private final TextWatcher watcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            if(userId.length() == 0 || userName.length() == 0 || password.length() == 0)
                isAllFieldsFull = false;
            else
                isAllFieldsFull = true;
        }
    };

}
