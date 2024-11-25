package pers.ember.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class login_activity extends AppCompatActivity {

    private EditText usernameEditText;
    private EditText passwordEditText;
    private TextView forgetPassword;
    private Button loginButton;
    private Button registerButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameEditText = findViewById(R.id.editTextUsername2);
        passwordEditText = findViewById(R.id.editTextPassword2);
        loginButton = findViewById(R.id.buttonLogin);
        registerButton = findViewById(R.id.buttonRegister);
        forgetPassword = findViewById(R.id.textViewForget);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                login(username, password);
            }
        });
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(login_activity.this,register_activity.class);
                startActivity(intent);
                finish();
            }
        });
        forgetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(login_activity.this,update_activity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void login(String username, String password) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (username.isEmpty() || password.isEmpty()) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(login_activity.this, "请输入用户名和密码", Toast.LENGTH_SHORT).show();
                            }
                        });
                        return;
                    }
                    URL url = new URL("http://8.134.189.141:999/user/login");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/json; utf-8");
                    connection.setRequestProperty("Accept", "application/json");
                    connection.setDoOutput(true);

                    JSONObject jsonInput = new JSONObject();
                    jsonInput.put("email", username);
                    jsonInput.put("password", password);
                    String jsonString = jsonInput.toString();

                    try (OutputStream os = connection.getOutputStream()) {
                        byte[] input = jsonString.getBytes("utf-8");
                        os.write(input, 0, input.length);
                    }

                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        try (Scanner scanner = new Scanner(connection.getInputStream())) {
                            scanner.useDelimiter("\\A");
                            String response = scanner.hasNext() ? scanner.next() : "";

                            JSONObject jsonResponse = new JSONObject(response);
                            int code = jsonResponse.getInt("code");
                            final String message = jsonResponse.getString("message");
                            final String data = jsonResponse.getString("data");

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(login_activity.this, message, Toast.LENGTH_SHORT).show();
                                    // 此处可以进一步处理JWT，例如保存到SharedPreferences
                                }
                            });
                        }
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(login_activity.this, "登录失败，请重试", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(login_activity.this, "网络错误，请稍后再试", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
    }
}