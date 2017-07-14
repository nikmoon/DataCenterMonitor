package org.nikbird.innopolis.datacentermonitor.services;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;


import org.nikbird.innopolis.datacentermonitor.abstractclasses.AbstractDataCenter;
import org.nikbird.innopolis.datacentermonitor.interfaces.IServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class ServiceDataCenterListener extends Service {

    private Handler mHandler;
    private AsyncTask<String, Integer, List<String>> asyncTask;

    public ServiceDataCenterListener() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mHandler = new Handler();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String taskString = intent.getStringExtra(AbstractDataCenter.SERVICE_TASK_TAG);
        AbstractDataCenter.ServiceTask task = null;
        if (taskString == null) {
            Toast.makeText(getApplicationContext(), "Service task empty", Toast.LENGTH_LONG).show();
        }
        try {
            task = AbstractDataCenter.ServiceTask.valueOf(taskString);
        } catch (IllegalArgumentException e) {
            Toast.makeText(getApplicationContext(), "Invalid service task: " + taskString, Toast.LENGTH_LONG).show();
        }
        if (task != null) {
            switch (task) {
                case DELIVERY_STRUCTURE:
                    deliveryDataCenterInfo();
                    break;
                case DELIVERY_SERVER_EVENT:
                    break;
            }
        }
        return START_STICKY;
    }

    private void deliveryDataCenterInfo() {
        asyncTask = new AsyncTask<String, Integer, List<String>>() {
            @Override
            protected List<String> doInBackground(String... strings) {
                List<String> lines = new ArrayList<>();
                try {
                    URLConnection urlConnection = new URL("http://127.0.0.1:8080").openConnection();
                    InputStreamReader reader = new InputStreamReader(urlConnection.getInputStream());
                    BufferedReader bufferedReader = new BufferedReader(reader);
                    String line;
                    while( (line = bufferedReader.readLine()) != null) {
                        lines.add(line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return lines;
            }

            @Override
            protected void onPostExecute(List<String> lines) {
                int rackCount = Integer.valueOf(lines.get(0).split(":")[1]);
                int rackCapacity = Integer.valueOf(lines.get(1).split(":")[1]);
                int[] rackNums = new int[lines.size() - 2];
                int[] serverNums = new int[lines.size() - 2];
                IServer.State[] states = new IServer.State[lines.size() - 2];
                for(int i = 2; i < lines.size(); i++) {
                    String[] data = lines.get(i).split(":")[1].split(",");
                    rackNums[i - 2] =  Integer.valueOf(data[0]);
                    serverNums[i - 2] = Integer.valueOf(data[1]);
                    states[i - 2] = IServer.State.valueOf(data[2]);
                }
                ((AbstractDataCenter)AbstractDataCenter.getDataCenter()).deliveryDataCenterInfo(
                        rackCount, rackCapacity, rackNums, serverNums, states);
            }
        };
        asyncTask.execute();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
