package pl.edu.agh.simplechat;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import java.lang.ref.WeakReference;

public class MainActivity extends AppCompatActivity {

    private EditText ip;
    private EditText nickname;
    private Button loginButton;

    public static String IP="ip";
    public static String NICK="nick";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ip = (EditText) findViewById(R.id.ip);
        nickname = (EditText) findViewById(R.id.nickname);
        loginButton = (Button) findViewById(R.id.buttonLogin);
    }

    public void onLogin(View button){
        Intent intent = new Intent(MainActivity.this, ChatActivity.class);
        intent.putExtra(IP, ip.getText().toString());
        intent.putExtra(NICK, nickname.getText().toString());
        startActivity(intent);
    }
}
