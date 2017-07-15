package org.nikbird.innopolis.datacentermonitor.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import org.nikbird.innopolis.datacentermonitor.interfaces.newgen.IDataCenter;

public class ServiceDataCenter extends Service {

    private IDataCenter mDataCenter;
    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        ServiceDataCenter getDataCenter() { return ServiceDataCenter.this; }
    }

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
}
