package pers.ember.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import pers.ember.myapplication.Entity.ProgressNode;
import pers.ember.myapplication.Entity.ProgressNodeList;
import pers.ember.myapplication.View.ProgressGraphView;

public class progress_activity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    private List<ProgressNodeList> myList;
    private Button deleteProgressButton;
    private Spinner spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);
        spinner = findViewById(R.id.spinner_progress_files);
        ProgressGraphView progressGraphView = findViewById(R.id.progressGraphView);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        deleteProgressButton = findViewById(R.id.delete_progress_button);
        deleteProgressButton.setOnClickListener(v -> {
            int position = spinner.getSelectedItemPosition();
            if (position >= 0 && position < myList.size()) {
                deleteProgress(myList.get(position).getId());
            }
        });
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.navigation_progress) {
                return true;
            }
            if (id == R.id.navigation_create) {
                Intent intent = new Intent(progress_activity.this,design_activity.class);
                startActivity(intent);
                finish();
                return true;
            }
            return false;
        });
        String jsonData = "[\n" +
                "  {\n" +
                "    \"id\": \"1\",\n" +
                "    \"name\": \"Minecraft\",\n" +
                "    \"description\": \"游戏的核心与故事\",\n" +
                "    \"icon\": \"icon1.png\",\n" +
                "    \"finished\": \"false\",\n" +
                "    \"x\": 100,\n" +
                "    \"y\": 500,\n" +
                "    \"next\": [\"2\"]\n" +
                "  },\n" +
                "  {\n" +
                "    \"id\": \"2\",\n" +
                "    \"name\": \"石器时代\",\n" +
                "    \"description\": \"用你的新镐挖掘石头\",\n" +
                "    \"icon\": \"icon2.png\",\n" +
                "    \"finished\": \"false\",\n" +
                "    \"x\": 400,\n" +
                "    \"y\": 500,\n" +
                "    \"before\": \"1\",\n" +
                "    \"next\": [\"3\"]\n" +
                "  },\n" +
                "  {\n" +
                "    \"id\": \"3\",\n" +
                "    \"name\": \"获得升级\",\n" +
                "    \"description\": \"制作一把更好的镐\",\n" +
                "    \"icon\": \"icon3.png\",\n" +
                "    \"finished\": \"false\",\n" +
                "    \"x\": 700,\n" +
                "    \"y\": 500,\n" +
                "    \"before\": \"2\",\n" +
                "    \"next\": [\"4\"]\n" +
                "  },\n" +
                "  {\n" +
                "    \"id\": \"4\",\n" +
                "    \"name\": \"来硬的\",\n" +
                "    \"description\": \"冶炼出一块铁锭\",\n" +
                "    \"icon\": \"icon4.png\",\n" +
                "    \"finished\": \"false\",\n" +
                "    \"before\": \"3\",\n" +
                "    \"x\": 1000,\n" +
                "    \"y\": 500,\n" +
                "    \"next\": [\"5\",\"6\",\"7\"]\n" +
                "  },\n" +
                "  {\n" +
                "    \"id\": \"5\",\n" +
                "    \"name\": \"这不是铁镐么\",\n" +
                "    \"description\": \"升级你的镐\",\n" +
                "    \"finished\": \"false\",\n" +
                "    \"before\": \"4\",\n" +
                "    \"x\": 1300,\n" +
                "    \"y\": 500,\n" +
                "    \"icon\": \"icon5.png\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"id\": \"6\",\n" +
                "    \"name\": \"热腾腾的\",\n" +
                "    \"description\": \"用铁桶装点熔岩\",\n" +
                "    \"finished\": \"false\",\n" +
                "    \"before\": \"4\",\n" +
                "    \"x\": 1300,\n" +
                "    \"y\": 800,\n" +
                "    \"icon\": \"icon6.png\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"id\": \"7\",\n" +
                "    \"name\": \"整装上阵\",\n" +
                "    \"description\": \"用铁盔甲来保护你自己\",\n" +
                "    \"finished\": \"false\",\n" +
                "    \"before\": \"4\",\n" +
                "    \"x\": 1300,\n" +
                "    \"y\": 1100,\n" +
                "    \"icon\": \"icon7.png\"\n" +
                "  }\n" +
                "] ";
        Gson gson = new Gson();
        List<ProgressNode> nodes = Arrays.asList(gson.fromJson(jsonData, ProgressNode[].class));
        progressGraphView.setNodes(nodes);
        GetProgress();

    }

    private void deleteProgress(int id) {
        new Thread(() -> {
                try {
                    URL url = new URL("http://8.134.189.141:999/api/progress/delete?id=" + id);

                    String token = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
                            .getString("auth_token", null);
                    if (token == null) {
                        runOnUiThread(() -> Toast.makeText(progress_activity.this, "请重新登录", Toast.LENGTH_SHORT).show());
                        return;
                    }
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setRequestProperty("Content-Type", "application/json; utf-8");
                    connection.setRequestProperty("Accept", "application/json");
                    connection.setRequestProperty("Authorization", token);

                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        try (Scanner scanner = new Scanner(connection.getInputStream())) {
                            scanner.useDelimiter("\\A");
                            String response = scanner.hasNext() ? scanner.next() : "";
                            JSONObject jsonResponse = new JSONObject(response);
                            int code = jsonResponse.getInt("code");
                            runOnUiThread(() -> {
                                if (code == 200) {
                                    removeProgressFromList(id);
                                    updateSpinner();
                                    Toast.makeText(progress_activity.this, "进度删除成功！", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(progress_activity.this, "进度删除失败！" , Toast.LENGTH_SHORT).show();
                                }
                            });

                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
        }).start();
    }

    private void GetProgress() {
        new Thread(() -> {
            try {
                URL url = new URL("http://8.134.189.141:999/api/progress/get");
                String token = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
                        .getString("auth_token", null);
                if (token == null) {
                    runOnUiThread(() -> Toast.makeText(progress_activity.this, "请重新登录", Toast.LENGTH_SHORT).show());
                    return;
                }

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Content-Type", "application/json; utf-8");
                connection.setRequestProperty("Accept", "application/json");
                connection.setRequestProperty("Authorization", token);

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    try (Scanner scanner = new Scanner(connection.getInputStream())) {
                        scanner.useDelimiter("\\A");
                        String response = scanner.hasNext() ? scanner.next() : "";
                        JSONObject jsonResponse = new JSONObject(response);
                        int code = jsonResponse.getInt("code");
                        final String data = jsonResponse.getString("data");
                        runOnUiThread(() -> {
                            if (code == 200) {
                                List<ProgressNodeList> progressList = parseProgressData(data);
                                setupSpinner(progressList);
                            }
                        });

                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(progress_activity.this, "网络错误，请稍后重试", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void setupSpinner(List<ProgressNodeList> progressList) {
        List<String> progressNames = new ArrayList<>();
        for (int i = 0; i < progressList.size(); i++) {
            String name = String.valueOf(i+1);
            progressNames.add("进度文件 " + name);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                progressNames
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ProgressNodeList selectedProgressList = progressList.get(position);

                runOnUiThread(() -> {
                    ProgressGraphView progressGraphView = findViewById(R.id.progressGraphView);
                    progressGraphView.setNodes(selectedProgressList.getProgressNodes());
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }
    private List<ProgressNodeList> parseProgressData(String data) {
        List<ProgressNodeList> progressLists = new ArrayList<>();
        try {
            org.json.JSONArray jsonArray = new org.json.JSONArray(data);

            for (int i = 0; i < jsonArray.length(); i++) {
                org.json.JSONObject jsonObject = jsonArray.getJSONObject(i);

                int id = jsonObject.getInt("id");
                String progressJson = jsonObject.getString("progress");

                Gson gson = new Gson();
                List<ProgressNode> progressNodes = Arrays.asList(gson.fromJson(progressJson, ProgressNode[].class));

                ProgressNodeList progressNodeList = new ProgressNodeList();
                progressNodeList.setId(id);
                progressNodeList.setProgressNodes(progressNodes);

                progressLists.add(progressNodeList);
            }
        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() -> Toast.makeText(this, "解析进度数据出错", Toast.LENGTH_SHORT).show());
        }
        myList = progressLists;
        return progressLists;
    }
    private void removeProgressFromList(int id) {
        if (myList != null) {
            for (int i = 0; i < myList.size(); i++) {
                if (myList.get(i).getId() == id) {
                    myList.remove(i);
                    break;
                }
            }
        }
    }

    private void updateSpinner() {
        if (myList == null || myList.isEmpty()) {
            return; // 如果列表为空，直接返回
        }

        List<String> progressNames = new ArrayList<>();
        for (int i = 0; i < myList.size(); i++) {
            String name = String.valueOf(i + 1);
            progressNames.add("进度文件 " + name);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, progressNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

}