package pl.edu.agh.simplechat;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.*;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class ChatActivity extends Activity {

    private String nickname;
    private String ip;
    private TextView nicknameTextView;
    private ListView messagesList;
    private EditText messageEditText;
    private Button sendMessageButton;
    private ArrayList<String> listItems = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private Handler myHandler = new MyHandler(this);
    private MqttClient sampleClient = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        nickname = getIntent().getStringExtra(MainActivity.NICK);
        ip = getIntent().getStringExtra(MainActivity.IP);
        messagesList = (ListView) findViewById(R.id.listView);
        messageEditText = (EditText) findViewById(R.id.messageInput);
        sendMessageButton = (Button) findViewById(R.id.buttonSend);
        System.out.println("SCXZ");
        System.out.println(findViewById(R.id.nicknameDisplay));
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listItems);
        messagesList.setAdapter(adapter);
        nicknameTextView = (TextView) findViewById(R.id.nicknameDisplay);

        nicknameTextView.setText(nickname);
        //uruchamiamy MQTT w tle
        new Thread(new Runnable() {
            @Override
            public void run() {
                startMQTT();
            }
        }).start();
    }

    protected void onDestroy() {
        super.onDestroy();
        if (sampleClient != null) {
            try {
                sampleClient.disconnect();
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

    public void onSend(View button) {
        String message = messageEditText.getText().toString();
        MqttMessage mqttMessage = new MqttMessage();
        mqttMessage.setQos(0);
        mqttMessage.setPayload(message.getBytes());
        try {
            sampleClient.publish("/grze/" + nickname, mqttMessage);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void startMQTT() {
        MemoryPersistence persistence = new MemoryPersistence();
        try {
            String broker = "tcp://" + ip + ":1883";
            sampleClient = new MqttClient(broker, nickname, persistence);
            sampleClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable throwable) {
                    Message msg = myHandler.obtainMessage();
                    Bundle b = new Bundle();
                    b.putString("NICK", "SYSTEM");
                    b.putString("MSG", "CONNECTION LOST");
                    msg.setData(b);
                    myHandler.sendMessage(msg);
                }

                @Override
                public void messageArrived(String s, MqttMessage mqttMessage) {
                    Message msg = myHandler.obtainMessage();
                    Bundle b = new Bundle();
                    b.putString("NICK", s);
                    b.putString("MSG", mqttMessage.toString());
                    msg.setData(b);
                    myHandler.sendMessage(msg);
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

                }
            });
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            System.out.println("Connecting to broker: " + broker);
            sampleClient.connect(connOpts);
            System.out.println("Connected");
            sampleClient.subscribe("/grze/#");
        } catch (MqttException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private static class MyHandler extends Handler {
        private final WeakReference<ChatActivity> sActivity;

        MyHandler(ChatActivity activity) {
            sActivity = new WeakReference<>(activity);
        }

        public void handleMessage(Message msg) {
            ChatActivity activity = sActivity.get();
            activity.listItems.add("[" + msg.getData().getString("NICK") + "]" +
                    msg.getData().getString("MSG"));
            activity.adapter.notifyDataSetChanged();
            activity.messagesList.setSelection(activity.listItems.size() - 1);
        }
    }
}
