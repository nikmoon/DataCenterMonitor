package org.nikbird.innopolis.datacentermonitor.activities.newgen;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import org.nikbird.innopolis.datacentermonitor.R;
import org.nikbird.innopolis.datacentermonitor.interfaces.newgen.IDataCenter;
import org.nikbird.innopolis.datacentermonitor.interfaces.newgen.IRack;
import org.nikbird.innopolis.datacentermonitor.interfaces.newgen.IServer;
import org.nikbird.innopolis.datacentermonitor.interfaces.newgen.IServerRoom;
import org.nikbird.innopolis.datacentermonitor.services.ServiceDataCenter;

public class ActivityDataCenter extends AppCompatActivity implements IDataCenter.IListener {

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            ServiceDataCenter.LocalBinder binder = (ServiceDataCenter.LocalBinder) iBinder;
            mDataCenter = binder.getDataCenter();
            mDataCenter.setEventListener(ActivityDataCenter.this);
            onReplicationEvent();
        }

        @Override public void onServiceDisconnected(ComponentName componentName) {}
    };

    private IDataCenter mDataCenter;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_center2);

        bindService(new Intent(getApplicationContext(), ServiceDataCenter.class), mConnection, BIND_AUTO_CREATE);
    }

    @Override public void onAuthenticationEvent() {}

    @Override
    public void onServerAdded(IServer server) {

    }

    @Override
    public void onServerRemoved(IServer server, IServer.ServerPosition prevPosition) {

    }

    @Override
    public void onServerStateChanged(IServer server, IServer.State prevState) {

    }

    @Override
    public void onReplicationEvent() {
        if (!mDataCenter.isReplicationComplete()) {
            String errorMsg = mDataCenter.replicationErrorMessage();
            if (errorMsg != null) {
                Toast.makeText(this, mDataCenter.replicationErrorMessage(), Toast.LENGTH_SHORT).show();
            }
            return;
        }
        IDataCenter dataCenter = mDataCenter;
    }
}
