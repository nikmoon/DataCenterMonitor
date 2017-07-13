package org.nikbird.innopolis.datacentermonitor;

import android.content.Context;

import org.nikbird.innopolis.datacentermonitor.abstractclasses.AbstractDataCenter;
import org.nikbird.innopolis.datacentermonitor.interfaces.IServer;


/**
 * Created by nikbird on 11.07.17.
 */

public class RemoteDataCenter extends AbstractDataCenter {

    private static final RemoteDataCenter INSTANCE = new RemoteDataCenter();
    public static final RemoteDataCenter getInstance() { return INSTANCE; }
    private RemoteDataCenter() {}

    @Override
    public boolean start(Context context) {
        if (super.start(context)) {
            // стартуем сервис для мониторинга дата-центра
            return true;
        }
        return false;
    }

    @Override
    public void repairServer(IServer server) {

    }
}
