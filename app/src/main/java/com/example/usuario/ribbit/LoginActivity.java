package com.example.usuario.ribbit;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


public class LoginActivity extends ActionBarActivity {

    @InjectView(R.id.usernameField) EditText mUsernameField;
    @InjectView(R.id.passwordField) EditText mPasswordField;
    @InjectView(R.id.loginProgressBar) ProgressBar mLoginProgressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.inject(this);
        mLoginProgressBar.setVisibility(View.INVISIBLE);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @OnClick(R.id.loginButton) void submit(){
        String username = mUsernameField.getText().toString();
        String password = mPasswordField.getText().toString();

        username = username.trim();
        password = password.trim();

        if (username.isEmpty() || password.isEmpty()){
            AlertDialogGenerator dialog = new AlertDialogGenerator();
            dialog.showAlertDialog(LoginActivity.this, getString(R.string.login_error_message), getString(R.string.login_error_title));
        }
        else{

            loginUser(username, password);

        }
    }

    @OnClick(R.id.signUpText) void goToSignUpActivity(){
        Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
        startActivity(intent);
    }

    private void loginUser(String username, String password) {
        //Log in

        mLoginProgressBar.setVisibility(View.VISIBLE);

        ParseUser.logInInBackground(username, password, new LogInCallback() {
            @Override
            public void done(ParseUser parseUser, ParseException e) {
                mLoginProgressBar.setVisibility(View.INVISIBLE);

                if (e == null){
                    //Success

                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
                else{
                    AlertDialogGenerator dialog = new AlertDialogGenerator();
                    dialog.showAlertDialog(LoginActivity.this, e.getMessage(), getString(R.string.login_error_title));
                }
            }
        });

    }

}
