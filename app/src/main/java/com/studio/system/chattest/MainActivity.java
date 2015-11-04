package com.studio.system.chattest;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;
import com.github.nkzawa.socketio.client.IO;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;


public class MainActivity extends Activity implements OnClickListener {

    private EditText sendMessage;
    private ArrayList<Message> mMessages = new ArrayList<Message>();
    static ArrayList<String> notiMsg = new ArrayList<String>();
    private boolean pasue = false;
    PowerManager.WakeLock wl;
    private Socket mSocketM;
    {
        try{
            mSocketM = IO.socket("http://157.7.137.182:3000");
        } catch (URISyntaxException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Timer timer = new Timer();
        Intent service = new Intent(this,MessageService.class);
        this.stopService(service);
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                mSocketM.connect();
                mSocketM.on("chat message", onNewMessage);
                mSocketM.emit("join room", "chatTest");
            }
        };
        timer.schedule(task,150);

        setContentView(R.layout.activity_main);
        sendMessage = (EditText) findViewById(R.id.message_input);
        ImageButton button = (ImageButton) findViewById(R.id.send_button);
        if(notiMsg.isEmpty() == false ){
            for(String msg : notiMsg) {
                addMessage(msg,false);
            }
        }
        button.setOnClickListener(this);
        PowerManager pm = (PowerManager)this.getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"MessageService");
        wl.acquire();
    }

    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String message = (String) args[0];
                    if(pasue){
                        addMessage(message,false);
                        createNotification(message);
                    }else
                    addMessage(message, false);
                }
            });
        }
    };

    public void addMessage(String message, boolean bool) {

        String blank = "";
        if(blank.compareTo(message)!=0) {
            mMessages.add(new Message(message, bool));
            ListView list = (ListView) findViewById(R.id.liveChat);
            list.setAdapter(new ListAdapt(this, R.layout.chat_item, mMessages) {
                @Override
                public void onEntrada(Object entrada, View view) {
                    TextView msg = (TextView) view.findViewById(R.id.tvBody);
                    msg.setText(((Message) entrada).getMessage());
                }
            });
        }
    }

    private void attemptSend(){
        String chatMessage = sendMessage.getText().toString().trim();
        if(TextUtils.isEmpty(chatMessage))return;
        sendMessage.setText("");
        mSocketM.emit("chat message",chatMessage);
        addMessage(chatMessage, true);
    }

    @Override
    protected void onDestroy(){
        //IntentFilter intentFilter = new IntentFilter();
        //registerReceiver(messageService,intentFilter);
        Intent service = new Intent(this,MessageService.class);
        this.startService(service);
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.send_button){
            attemptSend();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        pasue = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        NotificationManager nt = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        nt.cancel(0);
        pasue = false;
    }

    /*public class MessagesReceiver extends BroadcastReceiver {

                @Override
                public void onReceive(Context context, Intent intent){
                    Bundle bundle = intent.getExtras();
                    notiMsg = bundle.getStringArrayList("MESSAGES");
                    for(String nMsg : notiMsg){
                        addMessage(nMsg);
                    }
                }
            };
            private MessagesReceiver messageService = new MessagesReceiver();*/
    static void addM(String msn){
        notiMsg.add(msn);
    }

    private void createNotification(String msg){
        NotificationManager nNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.envelope)
                .setContentTitle("NEW MESSAGE")
                .setVibrate(new long[]{100, 200, 100, 500})
                .setLights(Color.CYAN,1000,1000)
                .setContentText(msg)
                .setDefaults(Notification.FLAG_SHOW_LIGHTS | Notification.DEFAULT_SOUND )
                .setPriority(Notification.PRIORITY_MAX);

        Intent intent = new Intent(this,MainActivity.class);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent,0);
        mBuilder.setContentIntent(pendingIntent);
        mBuilder.setAutoCancel(true).setWhen(System.currentTimeMillis());
        nNM.notify(0, mBuilder.build());
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}
