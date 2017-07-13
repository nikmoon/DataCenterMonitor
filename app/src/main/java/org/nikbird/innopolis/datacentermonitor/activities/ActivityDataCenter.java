package org.nikbird.innopolis.datacentermonitor.activities;

import android.support.constraint.ConstraintLayout;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import org.nikbird.innopolis.datacentermonitor.R;
import org.nikbird.innopolis.datacentermonitor.abstractclasses.ActivityListener;
import org.nikbird.innopolis.datacentermonitor.interfaces.IDataCenter;
import org.nikbird.innopolis.datacentermonitor.interfaces.IServer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActivityDataCenter extends ActivityListener implements View.OnClickListener {

    ConstraintLayout mDataCenterLayout;
    private Map<IServer, View> mServerViews;

    private List<ViewGroup> mRackViews;

    @Override
    public void onServerStateChanged(IServer server, IServer.State statePrev) {

    }

    private ImageButton newButton(int imageResId) {
        ImageButton button = new ImageButton(this);

//        button.setScaleType(ImageView.ScaleType.FIT_CENTER);
//        button.setAdjustViewBounds(true);

//        button.setImageResource(imageResId);
        button.setId(View.generateViewId());
        return button;
    }

    public ConstraintLayout createRackView(int rackId) {
        ConstraintLayout rackView = new ConstraintLayout(this);
        rackView.setBackgroundResource(R.color.colorRack);
        rackView.setId(rackId);
        return rackView;
    }

    public void addNewRackView2Rows() {
        int rackId = View.generateViewId();
        ConstraintLayout rackView = createRackView(rackId);
        mRackViews.add(rackView);

        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(
                200, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.horizontalWeight = 1;

        if (mRackViews.size() == 1) {
            params.topToTop = mDataCenterLayout.getId();
            params.leftToLeft = mDataCenterLayout.getId();
            params.setMargins(0, 20, 0, 20);
        } else {
            int div2Rem = mRackViews.size() % 2;
            if (div2Rem == 0) {
                ViewGroup leftRackView = mRackViews.get(mRackViews.size() - 2);
                ((ConstraintLayout.LayoutParams)leftRackView.getLayoutParams()).rightToLeft = rackId;

                params.leftToRight = leftRackView.getId();
                params.topToTop = leftRackView.getId();
                params.rightToRight = mDataCenterLayout.getId();
//                params.setMargins(0, 0, 0, 20);
            } else {
                ViewGroup topRackView = mRackViews.get(mRackViews.size() - 3);
                params.topToBottom = topRackView.getId();
                params.leftToLeft = mDataCenterLayout.getId();
                params.setMargins(0, 20, 0, 0);
            }
        }
        mDataCenterLayout.addView(rackView, params);
    }


    public ImageButton createServerView(IServer server) {
        ImageButton serverView;

        if (server.getState() == IServer.State.GOOD)
            serverView = newButton(R.drawable.server_ok);
        else
            serverView = newButton(R.drawable.server_fail);

        serverView.setOnClickListener(this);
        return serverView;
    }

    public void insertServersInRack(ViewGroup rackView, IServer[] servers) {
        View topServerView = null;
        IServer server;

        for(int serverNum = 1, maxCount = servers.length; serverNum <= maxCount; serverNum++) {
            server = servers[serverNum - 1];
            if (server == null)
                continue;
            ImageButton serverView = (ImageButton) mServerViews.get(server);
            ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(
//                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    100, 50);
            if (topServerView == null) {
                params.topToTop = rackView.getId();
                params.leftToLeft = rackView.getId();
                params.setMargins(0, 0, 0, 0);
            } else {
                params.topToBottom = topServerView.getId();
                params.leftToLeft = topServerView.getId();
                params.setMargins(0, 0, 0, 0);

            }
            rackView.addView(serverView, params);
            topServerView = serverView;
        }
    }

    @Override
    public void onReplicationComplete(IDataCenter dataCenter) {
        setContentView(R.layout.activity_data_center);
        mDataCenter.setEventListener(this);
        mServerViews = new HashMap<>();
        mRackViews = new ArrayList<>();

        mDataCenterLayout = (ConstraintLayout) findViewById(R.id.ltDataCenterRoom);
        int rackCount = mDataCenter.getRackCount();
        int rackCapacity = mDataCenter.getRackCapacity();

        // создаем вьюшки для стоек
        List<IServer[]> racks = new ArrayList<>(rackCount);
        for(int rackNum = 1; rackNum <= rackCount; rackNum++) {
//            addNewRackView(0.5f);
            addNewRackView2Rows();
            racks.add(new IServer[rackCapacity]);
        }

        // создаем вьюшки для серверов в стойках
        for(IServer server: mDataCenter) {
            int rackNum = server.getRackNumber();
            int serverNum = server.getNumber();
            racks.get(rackNum - 1)[serverNum - 1] = server;
            ImageButton serverView = createServerView(server);
            mServerViews.put(server, serverView);
        }

        // добавляем сервера в стойки
        for(int rackNum = 1; rackNum <= rackCount; rackNum++) {
            insertServersInRack(mRackViews.get(rackNum - 1), racks.get(rackNum - 1));
        }


//        ImageButton btnOkServer = newButton(R.drawable.server_ok);
//        ImageButton btnFailServer = newButton(R.drawable.server_fail);
//
//        ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
//                ViewGroup.LayoutParams.WRAP_CONTENT);
//        layoutParams.setMargins(0, 0, 0, 0);
//
//        ltDataCenterRoom.addView(btnOkServer, layoutParams);
//
//        layoutParams = new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
//                ViewGroup.LayoutParams.WRAP_CONTENT);
//        layoutParams.setMargins(0, 0, 0, 0);
//        layoutParams.topToBottom = btnOkServer.getId();
//        layoutParams.leftToLeft = btnOkServer.getId();
//
//        ltDataCenterRoom.addView(btnFailServer, layoutParams);

        // отрисовываем стойки и сервера
    }

    public void onClick(View view) {
//        int width = view.getWidth();
//        ViewGroup parent = (ViewGroup) view.getParent();
//        float scale = parent.getScaleX() + 0.03f;
//        parent.setScaleX(scale);
//        parent.setScaleY(scale);
//        mDataCenterLayout.requestLayout();
    }
}
