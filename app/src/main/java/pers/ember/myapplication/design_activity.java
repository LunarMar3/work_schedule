package pers.ember.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.yalantis.ucrop.UCrop;

import org.json.JSONObject;

import java.io.File;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import cn.hutool.core.util.IdUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.body.MultipartBody;
import cn.hutool.json.JSONUtil;
import pers.ember.myapplication.Entity.ProgressNode;
import pers.ember.myapplication.Entity.ProgressNodeList;
import pers.ember.myapplication.View.ProgressGraphView;


public class design_activity extends AppCompatActivity {
    private ProgressGraphView progressGraphView;
    private EditText inputNodeId, inputNodeName, inputNodeDescription, inputNodeX, inputNodeY;
    private Button saveNodeButton, saveProgressButton,uploadButton;
    private List<ProgressNode> nodeList = new ArrayList<>();
    private Spinner nodeSpinner, beforeSpinner;
    private Button deleteNodeButton;
    private View rootView;
    private static final int PICK_IMAGE_REQUEST = 1;
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
        uploadButton = findViewById(R.id.upload_icon);
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
        uploadButton.setOnClickListener(v -> openGallery());
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
        new Thread(() -> {
            try {
                URL url = new URL("http://8.134.189.141:999/api/progress/insert");
                String token = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
                        .getString("auth_token", null);

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

                String progress = JSONUtil.toJsonStr(nodeList);
                JSONObject jsonInput = new JSONObject();
                jsonInput.put("progress", progress);
                String jsonString = jsonInput.toString();

                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = jsonString.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                int responseCode = connection.getResponseCode();

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
        }).start();
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


        nodeList.remove(node);
        deleteicon(node.getIcon());
        ProgressGraphView progressGraphView = findViewById(R.id.progressGraphView);
        progressGraphView.setNodes(nodeList);

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

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }
    private void cropImage(Uri sourceUri) {
        File destDir = new File(getExternalFilesDir(null), "cropped_images");
        if (!destDir.exists()) {
            destDir.mkdirs();
        }

        Uri destinationUri = Uri.fromFile(new File(destDir, IdUtil.fastUUID()+"_image.jpg"));

        UCrop.of(sourceUri, destinationUri)
                .withAspectRatio(1, 1)
                .withMaxResultSize(128, 128)
                .start(design_activity.this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            cropImage(selectedImageUri);
        }

        if (requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK) {
            Uri croppedImageUri = UCrop.getOutput(data);
            saveNodeIcon(croppedImageUri);
        } else if (resultCode == UCrop.RESULT_ERROR) {
            Throwable cropError = UCrop.getError(data);
            Toast.makeText(this, "裁剪失败：" + cropError.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void saveNodeIcon(Uri iconUri) {
        File file = null;
        if ("content".equals(iconUri.getScheme())) {
            String[] projection = { MediaStore.Images.Media.DATA };
            Cursor cursor = getContentResolver().query(iconUri, projection, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex(projection[0]);
                String filePath = cursor.getString(columnIndex);
                file = new File(filePath);
                cursor.close();
            }
        } else if ("file".equals(iconUri.getScheme())) {
            file = new File(iconUri.getPath());
        }

        if (file != null && file.exists()) {
            int selectedPosition = nodeSpinner.getSelectedItemPosition();
            if (selectedPosition >= 0 && selectedPosition < nodeList.size()) {
                nodeList.get(selectedPosition).setIcon(file.getName());
                uploadIcons(file.getAbsolutePath());
                Toast.makeText(this, "图标已绑定到节点！", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "图标路径无效", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadIcons(String iconPath) {
        new Thread(() -> {
                if (iconPath != null && !iconPath.isEmpty()) {
                    try {
                        File file = new File(iconPath);
                        String url = "http://8.134.189.141:999/api/progress/icon";
                        String token = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
                                .getString("auth_token", null);

                        if (token == null) {
                            runOnUiThread(() ->
                                    Toast.makeText(this, "请重新登录", Toast.LENGTH_SHORT).show()
                            );
                            return;
                        }
                        HttpResponse response = HttpUtil.createPost(url)
                                .header("Authorization", token)
                                .form("file", file)
                                .execute();
                        if (response.getStatus() == 200) {
                            runOnUiThread(() ->
                                    Toast.makeText(this, "图标上传成功！", Toast.LENGTH_SHORT).show()
                            );
                        } else {
                            runOnUiThread(() ->
                                    Toast.makeText(this, "上传失败，错误码：" + response.getStatus(), Toast.LENGTH_SHORT).show()
                            );
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
        }).start();
    }
    private void deleteicon(String icon) {
        new Thread(() -> {
            if (icon != null && !icon.isEmpty()) {
                try {
                    URL url = new URL("http://8.134.189.141:999/api/progress/deleteIcon?icon=" + icon);

                    String token = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
                            .getString("auth_token", null);
                    if (token == null) {
                        runOnUiThread(() -> Toast.makeText(design_activity.this, "请重新登录", Toast.LENGTH_SHORT).show());
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
                                    Toast.makeText(design_activity.this, "图标删除成功！", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(design_activity.this, "图标删除失败！" , Toast.LENGTH_SHORT).show();
                                }
                            });

                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}