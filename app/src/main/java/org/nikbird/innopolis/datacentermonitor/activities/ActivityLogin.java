package org.nikbird.innopolis.datacentermonitor.activities;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.nikbird.innopolis.datacentermonitor.R;
import org.nikbird.innopolis.datacentermonitor.interfaces.IDataCenter;
import org.nikbird.innopolis.datacentermonitor.interfaces.IServer;
import org.nikbird.innopolis.datacentermonitor.services.ServiceDataCenter;

public class ActivityLogin extends AppCompatActivity implements IDataCenter.IListener {

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            ServiceDataCenter.LocalBinder binder = (ServiceDataCenter.LocalBinder) iBinder;
            mDataCenter = binder.getDataCenter();
            mLoginButton.setEnabled(true);
        }

        @Override public void onServiceDisconnected(ComponentName componentName) {}
    };

    private ConstraintLayout mLayoutLoginFields;
    private EditText mUrlField;
    private EditText mUsernameField;
    private EditText mPasswordField;
    private Button mLoginButton;
    private ProgressBar mProgressBar;

    private String mUrl;
    private String mUsername;
    private String mPassword;

    private IDataCenter mDataCenter;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mLayoutLoginFields = (ConstraintLayout) findViewById(R.id.ltLoginFields);
        mUrlField = (EditText) findViewById(R.id.etURL);
        mUrlField.requestFocus();
        mUrlField.setSelection(mUrlField.getText().toString().length());
        mUsernameField = (EditText) findViewById(R.id.etUsername);
        mPasswordField = (EditText) findViewById(R.id.etPassword);
        mLoginButton = (Button) findViewById(R.id.btnLogin);
        mProgressBar = (ProgressBar) findViewById(R.id.pbarWait);

        startService(new Intent(getApplicationContext(), ServiceDataCenter.class));
        bindService(new Intent(getApplicationContext(), ServiceDataCenter.class), mConnection, BIND_AUTO_CREATE);
    }

    @Override protected void onResume() {
        super.onResume();
        mProgressBar.setVisibility(View.INVISIBLE);
        mLayoutLoginFields.setVisibility(View.VISIBLE);
    }

    @Override protected void onStart() {
        super.onStart();
    }

    @Override protected void onStop() {
        super.onStop();
        if (mDataCenter != null)
            mDataCenter.removeEventListener(this);
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        if (mConnection != null)
            unbindService(mConnection);
    }

    public void onLoginClick(final View view) {
        if (isLoginDataValid()) {
            mLayoutLoginFields.setVisibility(View.INVISIBLE);
            mProgressBar.setVisibility(View.VISIBLE);
            mDataCenter.setEventListener(this);
            mDataCenter.authentication(mUsername, mPassword, mUrl);
        }
    }

    @Override
    public void onAuthenticationEvent() {
        if (mDataCenter.isAuthenticated())
            startActivity(new Intent(this, ActivityDataCenter.class));
        else {
            mProgressBar.setVisibility(View.INVISIBLE);
            mLayoutLoginFields.setVisibility(View.VISIBLE);
            Toast.makeText(this, mDataCenter.authErrorMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override public void onServerAdded(IServer server) {}
    @Override public void onServerRemoved(IServer server, IServer.ServerPosition prevPosition) {}
    @Override public void onServerStateChanged(IServer server, IServer.State prevState) {}
    @Override public void onReplicationEvent() {}

    private boolean isLoginDataValid() {
        mUrl = mUrlField.getText().toString();
        if (mUrl.length() <= 3 || mUrl.endsWith("://")) {
            mUrlField.requestFocus();
            mUrlField.setSelection(mUrl.length());
            Toast.makeText(this, "Invalid URL", Toast.LENGTH_SHORT).show();
            return false;
        }
        mUsername = mUsernameField.getText().toString();
        if (mUsername.length() <= 2) {
            mUsernameField.requestFocus();
            mUsernameField.setSelection(mUsername.length());
            Toast.makeText(this, "Invalid username", Toast.LENGTH_SHORT).show();
            return false;
        }
        mPassword = mPasswordField.getText().toString();
        if (mPassword.length() <= 3) {
            mPasswordField.requestFocus();
            mPasswordField.setSelection(mPassword.length());
            Toast.makeText(this, "Invalid password", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}
