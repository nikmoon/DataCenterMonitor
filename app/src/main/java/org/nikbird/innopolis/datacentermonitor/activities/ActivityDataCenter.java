package org.nikbird.innopolis.datacentermonitor.activities;

import org.nikbird.innopolis.datacentermonitor.R;
import org.nikbird.innopolis.datacentermonitor.abstractclasses.ActivityListener;
import org.nikbird.innopolis.datacentermonitor.interfaces.IDataCenter;
import org.nikbird.innopolis.datacentermonitor.interfaces.IServer;

public class ActivityDataCenter extends ActivityListener {

    @Override
    public void onServerStateChanged(IServer server, IServer.State statePrev) {

    }

    @Override
    public void onReplicationComplete(IDataCenter dataCenter) {
        setContentView(R.layout.activity_data_center);
        // отрисовываем стойки и сервера
        mDataCenter.setEventListener(this);
    }
}
