package pers.ember.myapplication;

import androidx.appcompat.app.AppCompatActivity;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
        saveProgressButton.setOnClickListener(v -> saveProgress());
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
        // 保存整个进度的逻辑，可以是写入文件或上传到服务器
        Toast.makeText(this, "进度已保存！", Toast.LENGTH_SHORT).show();
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