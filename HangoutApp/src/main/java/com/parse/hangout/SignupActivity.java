package com.parse.hangout;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

/**
 * Created by fuzhongyuan on 4/12/16.
 */
public class SignupActivity extends AppCompatActivity {
    private EditText username;
    private EditText password;
    private EditText password_confirm;
    private Button signup_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        username = (EditText) findViewById(R.id.username_edit_text);
        password = (EditText) findViewById(R.id.password_edit_text);
        password_confirm = (EditText) findViewById(R.id.password_again_edit_text);
        signup_button = (Button) findViewById(R.id.signup);

        signup_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signup();
                //startActivity(new Intent(LoginActivity.this, LoginActivity.class));
            }
        });
    }

    private void signup() {
        String username_text = username.getText().toString().trim();
        String password_text = password.getText().toString().trim();
        String password_confirm_text = password_confirm.getText().toString().trim();

        if (password_text == null || password_text == "") {
            Toast.makeText(SignupActivity.this, "passwords cannot be empty", Toast.LENGTH_LONG).show();
            return;
        }

        if (!password_text.equals(password_confirm_text)) {
            Toast.makeText(SignupActivity.this, "passwords did not match", Toast.LENGTH_LONG).show();
            return;
        }

        ParseUser user = new ParseUser();
        user.setUsername(username_text);
        user.setPassword(password_text);

        user.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Intent intent = new Intent(SignupActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK |
                            Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } else {
                    Toast.makeText(SignupActivity.this, "error", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.login_screen:
                startActivity(new Intent(this, LoginActivity.class));
                return true;
            case R.id.signup_screen:
                startActivity(new Intent(this, SignupActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
