package org.nikbird.innopolis.datacentermonitor.abstractclasses;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import org.nikbird.innopolis.datacentermonitor.interfaces.IDataCenter;

/**
 * Created by nikbird on 12.07.17.
 */

public abstract class ActivityListener extends AppCompatActivity implements IDataCenter.IListener {

    protected IDataCenter mDataCenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDataCenter = AbstractDataCenter.getDataCenter();
        mDataCenter.start(getApplicationContext());
        if (mDataCenter.isReplicationComplete())
            onReplicationComplete(mDataCenter);
        else
            mDataCenter.setEventListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDataCenter.removeEventListener(this);
    }
}
