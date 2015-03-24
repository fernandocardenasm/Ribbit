package com.example.usuario.ribbit;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


public class SignUpActivity extends ActionBarActivity {

    @InjectView(R.id.usernameField) EditText mUsernameField;
    @InjectView(R.id.passwordField) EditText mPasswordField;
    @InjectView(R.id.emailField) EditText mEmailField;
    @InjectView(R.id.signUpProgressBar) ProgressBar mSignUpProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        ButterKnife.inject(this);
        mSignUpProgressBar.setVisibility(View.INVISIBLE);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        /*if (id == R.id.action_settings) {
            return true;
        }
        */

        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.signUpButton) void submit(){
        String username = mUsernameField.getText().toString();
        String password = mPasswordField.getText().toString();
        String email = mEmailField.getText().toString();

        username = username.trim();
        password = password.trim();
        email = email.trim();

        if (username.isEmpty() || password.isEmpty() || email.isEmpty()){
            AlertDialogGenerator dialog = new AlertDialogGenerator();
            dialog.showAlertDialog(SignUpActivity.this, getString(R.string.sign_up_error_message), getString(R.string.sign_up_error_title));
        }
        else{

            signUpUser(username, password, email);

        }
    }

    private void signUpUser(String username, String password, String email) {
        //Create the user
        mSignUpProgressBar.setVisibility(View.VISIBLE);
        ParseUser newUser = new ParseUser();
        newUser.setUsername(username);
        newUser.setPassword(password);
        newUser.setEmail(email);
        newUser.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                mSignUpProgressBar.setVisibility(View.INVISIBLE);
                if (e == null){
                    //Success
                    Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
                else{
                    AlertDialogGenerator dialog = new AlertDialogGenerator();
                    dialog.showAlertDialog(SignUpActivity.this, e.getMessage(), getString(R.string.sign_up_error_title));

                }
            }
        });
    }
}
