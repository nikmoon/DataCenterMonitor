package org.nikbird.innopolis.datacentermonitor.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import org.nikbird.innopolis.datacentermonitor.LocalDataCenter;
import org.nikbird.innopolis.datacentermonitor.R;
import org.nikbird.innopolis.datacentermonitor.abstractclasses.AbstractDataCenter;
import org.nikbird.innopolis.datacentermonitor.services.ServiceDataCenter;

public class ActivityLogin extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    public void startMonitoring(View view) {
        AbstractDataCenter.setDataCenter(LocalDataCenter.getInstance());
        startActivity(new Intent(this, ActivityDataCenter.class));
    }

    public void startDataCenter(View view) {
        Intent intent = new Intent(this, ServiceDataCenter.class);
        startService(intent);
    }

}
