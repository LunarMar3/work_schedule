package pers.ember.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class update_activity extends AppCompatActivity {

    private EditText passwordEditText;
    private EditText emailEditText;
    private EditText codeEditText;
    private Button sendCodeButton;
    private Button updateButton;
    private Button cancelButton;
    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);

        passwordEditText = findViewById(R.id.editTextPassword);
        emailEditText = findViewById(R.id.editTextEmail);
        codeEditText = findViewById(R.id.editTextCode);
        sendCodeButton = findViewById(R.id.buttonSend);
        updateButton = findViewById(R.id.buttonRegisterConfirm);
        cancelButton = findViewById(R.id.buttonReturn2);

        sendCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString();
                if (email.isEmpty()) {
                    Toast.makeText(update_activity.this, "请输入邮箱", Toast.LENGTH_SHORT).show();
                    return;
                }
                sendVerificationCode(email);
            }
        });

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String password = passwordEditText.getText().toString();
                String email = emailEditText.getText().toString();
                String code = codeEditText.getText().toString();

                if (password.isEmpty() || email.isEmpty() || code.isEmpty()) {
                    Toast.makeText(update_activity.this, "请完整填写所有信息", Toast.LENGTH_SHORT).show();
                    return;
                }

                updatePassword(password, email, code);
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(update_activity.this, login_activity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void sendVerificationCode(String email) {
        new Thread(() -> {
            try {
                URL url = new URL("http://8.134.189.141:999/api/user/sms");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json; utf-8");
                connection.setRequestProperty("Accept", "application/json");
                connection.setDoOutput(true);

                JSONObject jsonInput = new JSONObject();
                jsonInput.put("email", email);
                String jsonString = jsonInput.toString();

                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = jsonString.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    runOnUiThread(() -> {
                        Toast.makeText(update_activity.this, "验证码已发送", Toast.LENGTH_SHORT).show();
                        startCountDownTimer();
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(update_activity.this, "验证码发送失败", Toast.LENGTH_SHORT).show());
                }

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(update_activity.this, "网络错误，请稍后重试", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void startCountDownTimer() {
        countDownTimer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                sendCodeButton.setText("重新发送 (" + millisUntilFinished / 1000 + "秒)");
                sendCodeButton.setEnabled(false);
            }

            @Override
            public void onFinish() {
                sendCodeButton.setText("发送验证码");
                sendCodeButton.setEnabled(true);
            }
        };

        countDownTimer.start();
    }

    private void updatePassword(String password, String email, String code) {
        new Thread(() -> {
            try {
                URL url = new URL("http://8.134.189.141:999/api/user/update");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json; utf-8");
                connection.setRequestProperty("Accept", "application/json");
                connection.setDoOutput(true);

                JSONObject jsonInput = new JSONObject();
                jsonInput.put("password", password);
                jsonInput.put("email", email);
                jsonInput.put("code", code);
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
                        int recode = jsonResponse.getInt("code");
                        final String message = jsonResponse.getString("message");
                        final String data = jsonResponse.getString("data");

                        runOnUiThread(() -> {
                            if (recode == 200) {
                                Toast.makeText(update_activity.this, "修改成功！", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(update_activity.this, login_activity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(update_activity.this, message, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } else {
                    runOnUiThread(() -> Toast.makeText(update_activity.this, "密码修改失败，请重试", Toast.LENGTH_SHORT).show());
                }

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(update_activity.this, "网络错误，请稍后重试", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}
