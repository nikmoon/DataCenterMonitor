package org.nikbird.innopolis.datacentermonitor;

import android.content.Context;

import org.nikbird.innopolis.datacentermonitor.abstractclasses.AbstractDataCenter;
import org.nikbird.innopolis.datacentermonitor.interfaces.IServer;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by nikbird on 11.07.17.
 */

public class RemoteDataCenter extends AbstractDataCenter {

    private static final RemoteDataCenter INSTANCE = new RemoteDataCenter();
    public static final RemoteDataCenter getInstance() { return INSTANCE; }
    private RemoteDataCenter() {}

    @Override
    public boolean startDataCenter(Context context) {
        if (super.startDataCenter(context)) {
            // стартуем сервис для мониторинга дата-центра
            return true;
        }
        return false;
    }

    @Override
    public IServer[] getFailedServers() {
        List<IServer> failedServers = new ArrayList<>();
        for(IServer server: getServers()) {
            if (server.getState() == IServer.State.FAIL)
                failedServers.add(server);
        }
        return failedServers.toArray(new IServer[failedServers.size()]);
    }

    @Override
    public void repairServer(IServer server) {

    }
}
