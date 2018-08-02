package com.chen.wayne.myfillmemory;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;


public class FillMemoryService extends Service {
    public static final String ACTION_FILL = "com.chen.wayne.myfillmemory.ACTION_FILL";
    public static final String ACTION_QUERY_RESULT = "com.chen.wayne.myfillmemory.ACTION_QUERY_RESULT";
    public static final String ACTION_RELEASE = "com.chen.wayne.myfillmemory.ACTION_RELEASE";
    public static final String EXTRA_FILL_SIZE = "extra_fill_size";
    private static final int NOTIFICATION_ID = 20171;
    static {
        System.loadLibrary("native_fill");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            Log.i("FillMemoryService", "intent is not null");
            String action = intent.getAction();
            if (ACTION_FILL.equals(action)) {
                onFill(intent);
            } else if (ACTION_RELEASE.equals(action)) {
                onRelease();
            } else if (ACTION_QUERY_RESULT.equals(action)) {
                onQuery();
            }
        }
        else {
            Log.i("FillMemoryService", "intent is null");
        }
        return super.onStartCommand(intent, flags, startId);
    }
    public static class GrayInnerService extends Service {
        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }
        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            startForeground(NOTIFICATION_ID, new Notification());
            stopForeground(true);
            stopSelf();
            return super.onStartCommand(intent, flags, startId);
        }
    }
    public void setForeground() {
        // 获取到通知管理器
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // 定义内容
        int notificationIcon = R.drawable.ic_launcher_foreground;
        CharSequence notificationTitle = "测试通知栏--title";
        long when = System.currentTimeMillis();
        Intent intent = new Intent(getApplicationContext(), FillMemoryActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
        // Notification notification = new Notification(notificationIcon, notificationTitle, when);
        Notification.Builder noticeBuilder = new Notification.Builder(getApplicationContext());
        noticeBuilder.setContentTitle("内存填充")
                .setContentText("测试展开内容")
                .setContentIntent(pendingIntent);
        Notification notification = createNotification(noticeBuilder);
        notification.defaults = Notification.DEFAULT_ALL;
        // notification.setLatestEventInfo(getApplicationContext(), "内存填充", "测试展开内容", pendingIntent);
        if (notification != null) {
            Log.e("notifacation", "notifacation is ok");
            startForeground(NOTIFICATION_ID, notification);
            // mNotificationManager.notify(1000+i, notification);
        }
    }
    public class FillThread extends Thread {
        public int fillSize;
        public FillThread(int fillSize) {
            this.fillSize = fillSize;
        }
        public void run() {
            if (fillSize > 0) {
                fill(fillSize);
                Log.i("==fill memory==", "fillSize > 0");
                // start foreground
                startForeground(NOTIFICATION_ID, new Notification());
                Log.i("==start foreground==", "erw");
//                if (Build.VERSION.SDK_INT < 18) {
//                    startForeground(NOTIFICATION_ID, new Notification()); // API < 18，此方法能有效隐藏Notification上的图标
//                } else {
//                    startForeground(NOTIFICATION_ID, new Notification());
//                    Intent innerIntent = new Intent(FillMemoryService.this, GrayInnerService.class);
//                    startService(innerIntent);
//                }
            } else {
                fill(0);
                stopForeground(true);
                Log.i("==stop foreground==", "erw");
            }
            // notify change
            onQuery();
        }
    }
    public class ReleaseThread extends Thread {
        public void run() {
            fill(0);
            Log.i("==release memory==", "fillSize > 0");
            stopForeground(true);
            Log.i("==stop foreground==", "re");
            onQuery();
        }
    }
    private void onFill(Intent intent) {
        int fillSize = intent.getIntExtra(EXTRA_FILL_SIZE, 0);
        Thread fillThread = new FillThread(fillSize);
        fillThread.start();
    }
    private void onQuery() {
        Intent intent = new Intent(ACTION_QUERY_RESULT);
        intent.putExtra(EXTRA_FILL_SIZE, getFilledSize());
        sendBroadcast(intent);
    }
    private void onRelease() {
        Thread releaseThread = new ReleaseThread();
        releaseThread.start();
    }
    private native void fill(int size);

    //private native void fill(int size);
    private native int getFilledSize();

    private Notification createNotification(Notification.Builder builder) {
        if (builder == null) {
            return null;
        }
        if (android.os.Build.VERSION.SDK_INT > 11 & android.os.Build.VERSION.SDK_INT < 15) {
            return builder.getNotification();
        } else if (android.os.Build.VERSION.SDK_INT > 15) {
            return builder.build();
        }
        return null;
    }
}
