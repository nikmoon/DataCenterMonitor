package org.nikbird.innopolis.datacentermonitor.activities;

import android.view.View;

import org.nikbird.innopolis.datacentermonitor.R;
import org.nikbird.innopolis.datacentermonitor.abstractclasses.ActivityListener;
import org.nikbird.innopolis.datacentermonitor.interfaces.IDataCenter;
import org.nikbird.innopolis.datacentermonitor.interfaces.IServer;

import java.util.HashMap;
import java.util.Map;

public class ActivityDataCenter extends ActivityListener {

    private Map<IServer, View> mServerViews;

    @Override
    public void onServerStateChanged(IServer server, IServer.State statePrev) {

    }

    @Override
    public void onReplicationComplete(IDataCenter dataCenter) {
        setContentView(R.layout.activity_data_center);
        mDataCenter.setEventListener(this);
        mServerViews = new HashMap<>();

        // отрисовываем стойки и сервера
    }
}
