package pers.ember.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cn.hutool.json.JSONUtil;
import pers.ember.myapplication.Entity.ProgressNode;
import pers.ember.myapplication.View.ProgressGraphView;

public class design_activity extends AppCompatActivity {
    private ProgressGraphView progressGraphView;
    private EditText inputNodeId, inputNodeName, inputNodeDescription, inputNodeX, inputNodeY;
    private Button saveNodeButton, saveProgressButton;
    private List<ProgressNode> nodeList = new ArrayList<>();
    private Spinner nodeSpinner, beforeSpinner;
    private Button deleteNodeButton;
    private View rootView;
    private ScrollView scrollView;
    private ArrayAdapter<String> nodeAdapter,beforeAdapter;

    private BottomNavigationView bottomNavigationView;

    private Button decreaseXButton, increaseXButton, decreaseYButton, increaseYButton;
    private static final int STEP_SIZE = 100;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_design);
        progressGraphView = findViewById(R.id.progressGraphView);
        inputNodeId = findViewById(R.id.input_node_id);
        inputNodeName = findViewById(R.id.input_node_name);
        inputNodeDescription = findViewById(R.id.input_node_description);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.navigation_progress) {
                Intent intent = new Intent(design_activity.this,progress_activity.class);
                startActivity(intent);
                finish();
            }
            if (id == R.id.navigation_create) {
                return true;
            }
            return false;
        });
        decreaseXButton = findViewById(R.id.decrease_x_button);
        increaseXButton = findViewById(R.id.increase_x_button);
        decreaseYButton = findViewById(R.id.decrease_y_button);
        increaseYButton = findViewById(R.id.increase_y_button);
        beforeSpinner = findViewById(R.id.input_node_before_spinner);
        inputNodeX = findViewById(R.id.input_node_x);
        inputNodeY = findViewById(R.id.input_node_y);
        saveNodeButton = findViewById(R.id.save_node_button);
        saveProgressButton = findViewById(R.id.save_progress_button);
        nodeSpinner = findViewById(R.id.node_spinner);
        deleteNodeButton = findViewById(R.id.delete_node_button);
        scrollView = findViewById(R.id.node_form);
        progressGraphView.setOnTouchListener((v, event) -> true);
        saveNodeButton.setOnClickListener(v -> saveNode());
        saveProgressButton.setOnClickListener(v -> {
            saveProgress();
        });
        nodeAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, getNodeNames(nodeList)
        );
        beforeAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, getBeforeNodeOptions(nodeList)
        );
        beforeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        beforeSpinner.setAdapter(beforeAdapter);
        nodeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        nodeSpinner.setAdapter(nodeAdapter);
        deleteNodeButton.setOnClickListener(v -> {
            int selectedPosition = nodeSpinner.getSelectedItemPosition();
            if (selectedPosition >= 0 && selectedPosition < nodeList.size()) {
                ProgressNode selectedNode = nodeList.get(selectedPosition);
                deleteNode(selectedNode);
            }
        });
        decreaseXButton.setOnClickListener(v -> adjustCoordinate(inputNodeX, -STEP_SIZE));
        increaseXButton.setOnClickListener(v -> adjustCoordinate(inputNodeX, STEP_SIZE));
        decreaseYButton.setOnClickListener(v -> adjustCoordinate(inputNodeY, -STEP_SIZE));
        increaseYButton.setOnClickListener(v -> adjustCoordinate(inputNodeY, STEP_SIZE));
        scrollView = findViewById(R.id.node_form);
        rootView = findViewById(android.R.id.content);
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            Rect r = new Rect();
            rootView.getWindowVisibleDisplayFrame(r);
            int screenHeight = rootView.getRootView().getHeight();
            int keypadHeight = screenHeight - r.bottom;

            if (keypadHeight > screenHeight * 0.15) {
                View focusedView = getCurrentFocus();
                if (focusedView != null) {
                    scrollView.smoothScrollTo(0, focusedView.getBottom());
                }
            }
        });

    }
    private void saveNode() {
        String id = inputNodeId.getText().toString().trim();
        String name = inputNodeName.getText().toString().trim();
        String description = inputNodeDescription.getText().toString().trim();
        int selectedBeforeIndex = beforeSpinner.getSelectedItemPosition();
        String beforeId = selectedBeforeIndex > 0 ? nodeList.get(selectedBeforeIndex - 1).getId() : null;
        String xStr = inputNodeX.getText().toString().trim();
        String yStr = inputNodeY.getText().toString().trim();

        if (id.isEmpty() || name.isEmpty() || xStr.isEmpty() || yStr.isEmpty()) {
            Toast.makeText(this, "请完整填写节点信息！", Toast.LENGTH_SHORT).show();
            return;
        }
        double x = Double.parseDouble(xStr);
        double y = Double.parseDouble(yStr);
        List<String> next = new ArrayList<>();
        ProgressNode node = new ProgressNode(id, name, description, null, next, beforeId, false, x, y);
        if (beforeId != null) {
            for (ProgressNode n : nodeList) {
                if (n.getId().equals(beforeId)) {
                    n.getNext().add(id);
                    break;
                }
            }
        }
        for (int i = 0; i < nodeList.size(); i++) {
            if (nodeList.get(i).getId().equals(id)) {
                nodeList.set(i, node);
                refreshGraph();
                Toast.makeText(this, "节点已更新！", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        // 不存在则新增
        nodeList.add(node);
        refreshGraph();
        nodeAdapter.clear();
        nodeAdapter.addAll(getNodeNames(nodeList));
        nodeAdapter.notifyDataSetChanged();
        beforeAdapter.clear();
        beforeAdapter.addAll(getBeforeNodeOptions(nodeList));
        beforeAdapter.notifyDataSetChanged();
        Toast.makeText(this, "节点已添加！", Toast.LENGTH_SHORT).show();
    }
    private void saveProgress() {
        // 开启一个新线程处理网络请求
        new Thread(() -> {
            try {
                URL url = new URL("http://8.134.189.141:999/api/progress/insert");
                String token = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
                        .getString("auth_token", null);

                // 检查 token 是否为空
                if (token == null) {
                    runOnUiThread(() ->
                            Toast.makeText(this, "请重新登录", Toast.LENGTH_SHORT).show());
                    return;
                }

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json; utf-8");
                connection.setRequestProperty("Accept", "application/json");
                connection.setRequestProperty("Authorization", token);
                connection.setDoOutput(true);

                // 准备请求数据
                String progress = JSONUtil.toJsonStr(nodeList);
                JSONObject jsonInput = new JSONObject();
                jsonInput.put("progress", progress);
                String jsonString = jsonInput.toString();

                // 写入请求数据
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = jsonString.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                int responseCode = connection.getResponseCode();

                // 根据响应结果更新 UI
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    runOnUiThread(() ->
                            Toast.makeText(this, "进度已保存！", Toast.LENGTH_SHORT).show());
                } else {
                    runOnUiThread(() ->
                            Toast.makeText(this, "保存失败！", Toast.LENGTH_SHORT).show());
                }

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(this, "网络错误，请稍后再试", Toast.LENGTH_SHORT).show());
            }
        }).start(); // 启动线程
    }

    private void refreshGraph() {
        progressGraphView.setNodes(nodeList);
    }
    private List<String> getNodeNames(List<ProgressNode> nodeList) {
        List<String> names = new ArrayList<>();
        for (ProgressNode node : nodeList) {
            names.add(node.getName() + " (ID: " + node.getId() + ")");
        }
        return names;
    }
    private void deleteNode(ProgressNode node) {
        // 删除节点
        nodeList.remove(node);

        // 更新 ProgressGraphView 和 Spinner
        ProgressGraphView progressGraphView = findViewById(R.id.progressGraphView);
        progressGraphView.setNodes(nodeList);

        // 更新 Spinner
        nodeAdapter.clear();
        nodeAdapter.addAll(getNodeNames(nodeList));
        nodeAdapter.notifyDataSetChanged();
        beforeAdapter.clear();
        beforeAdapter.addAll(getBeforeNodeOptions(nodeList));
        beforeAdapter.notifyDataSetChanged();
    }
    private void adjustCoordinate(EditText coordinateInput, int step) {
        String currentText = coordinateInput.getText().toString();
        int currentValue = currentText.isEmpty() ? 0 : Integer.parseInt(currentText);
        coordinateInput.setText(String.valueOf(currentValue + step));
    }
    private List<String> getBeforeNodeOptions(List<ProgressNode> nodeList) {
        List<String> options = new ArrayList<>();
        options.add("无前节点");
        for (ProgressNode node : nodeList) {
            options.add(node.getName() + " (ID: " + node.getId() + ")");
        }
        return options;
    }

}