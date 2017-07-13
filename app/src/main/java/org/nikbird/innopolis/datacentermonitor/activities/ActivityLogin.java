package org.nikbird.innopolis.datacentermonitor.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import org.nikbird.innopolis.datacentermonitor.LocalDataCenter;
import org.nikbird.innopolis.datacentermonitor.R;
import org.nikbird.innopolis.datacentermonitor.abstractclasses.AbstractDataCenter;

public class ActivityLogin extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        AbstractDataCenter.setDataCenter(LocalDataCenter.getInstance());
        AbstractDataCenter.getDataCenter().start(getApplicationContext());
    }
}
