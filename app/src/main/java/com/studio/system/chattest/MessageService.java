package com.studio.system.chattest;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import java.net.URISyntaxException;
import java.util.ArrayList;

/**
 * Created by N1cK0 on 15/03/16.
 */
public class MessageService extends Service {

    PowerManager.WakeLock wl;
    Handler handler;
    private Socket mSocket;
    {
        try{
            mSocket = IO.socket("http://157.7.137.182:3000");
        } catch (URISyntaxException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        /*HandlerThread hThread = new HandlerThread("HandlerThread");
        hThread.start();
        final Handler hand = new Handler(hThread.getLooper());
        final int oneM = 10000;

        Runnable eachMinute = new Runnable() {
            @Override
            public void run() {
                hand.postDelayed(this,oneM);
                mSocket.emit("chat message","test");
            }
        };

        hand.postDelayed(eachMinute,oneM);*/
        handler = new Handler();
        mSocket.connect();
        mSocket.emit("join room", "chatTest");
        mSocket.on("chat message", onNewMessage);
        PowerManager pm = (PowerManager)this.getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"MessageService");
        wl.acquire();
        return START_STICKY;
    }

    @Override
    public void onCreate(){
    }

    @Override
    public void onDestroy(){
        wl.release();
        super.onDestroy();
        mSocket.off("chat message", onNewMessage);
        mSocket.disconnect();
    }

    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String message = (String) args[0];
                    createNotification(message);
                }
            });
        }
    };

    private void runOnUiThread(Runnable runnable) {
        handler.post(runnable);
    }

    private void createNotification(String msg){
        MainActivity.addM(msg);
        NotificationManager nNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.envelope)
                .setContentTitle("NEW MESSAGE")
                .setVibrate(new long[]{100, 200, 100, 500})
                .setLights(Color.CYAN, 1000, 1000)
                .setContentText(msg)
                .setDefaults(Notification.FLAG_SHOW_LIGHTS | Notification.DEFAULT_SOUND)
                .setPriority(Notification.PRIORITY_MAX);

        Intent intent = new Intent(this,MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent,0);
        mBuilder.setContentIntent(pendingIntent);
        mBuilder.setAutoCancel(true).setWhen(System.currentTimeMillis());
        nNM.notify(0, mBuilder.build());
    }

}
