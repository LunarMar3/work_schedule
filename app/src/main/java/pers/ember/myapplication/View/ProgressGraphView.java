package pers.ember.myapplication.View;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import pers.ember.myapplication.Entity.ProgressNode;
import pers.ember.myapplication.R;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class ProgressGraphView extends View {
    private List<ProgressNode> nodes;
    private final float buttonSize = 50f;
    private Paint nodePaint;
    private Paint linePaint;
    private Paint textPaint;
    private Map<String, PointF> nodePositions;

    private final float nodeSize = 150f;

    public ProgressGraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }


    private void init() {
        nodePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        nodePaint.setColor(Color.YELLOW);
        nodePaint.setStyle(Paint.Style.FILL);

        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(Color.GRAY);
        linePaint.setStrokeWidth(8f);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(30f);

        nodePositions = new HashMap<>();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = 2000;
        int height = 2000;

        int measuredWidth = MeasureSpec.getSize(widthMeasureSpec);
        int measuredHeight = MeasureSpec.getSize(heightMeasureSpec);

        setMeasuredDimension(
                MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.EXACTLY ? measuredWidth : width,
                MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.EXACTLY ? measuredHeight : height
        );
    }

    public void setNodes(List<ProgressNode> nodes) {
        this.nodes = nodes;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (nodes == null || nodes.isEmpty()) return;
        float nodeSize = 150f;
        canvas.save();
        for (ProgressNode node : nodes) {
            if (node.getNext() != null) {
                for (String nextId : node.getNext()) {
                    ProgressNode nextNode = findNodeById(nextId);
                    if (nextNode != null) {
                        drawZigzagLine(
                                canvas,
                                new PointF((float) node.getX(), (float) node.getY()),
                                new PointF((float) nextNode.getX(), (float) nextNode.getY())
                        );
                    }
                }
            }
        }

        for (ProgressNode node : nodes) {
            float x = (float) node.getX();
            float y = (float) node.getY();

            // 设置节点颜色
            if (node.getFinished()) {
                nodePaint.setColor(Color.parseColor("#90EE90")); // 完成的节点为浅绿色
            } else {
                nodePaint.setColor(Color.WHITE); // 未完成的节点为白色
            }

            // 绘制按钮
            float buttonX = x + (nodeSize - buttonSize) / 2;
            float buttonY = y - buttonSize - 10f;
            drawButton(canvas, buttonX, buttonY);

            // 绘制节点圆角矩形
            canvas.drawRoundRect(
                    x, y,
                    x + nodeSize, y + nodeSize,
                    30f, 30f, nodePaint
            );

            // 绘制节点图标（用一个灰色圆圈表示）
            Paint iconPaint = new Paint();
            iconPaint.setColor(Color.LTGRAY);
            canvas.drawCircle(
                    x + nodeSize / 2,
                    y + nodeSize / 2,
                    nodeSize / 3, iconPaint
            );
        }
        canvas.restore();
    }
    private void drawButton(Canvas canvas, float x, float y) {
        Paint buttonPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        buttonPaint.setColor(Color.GREEN);
        canvas.drawRoundRect(
                x, y, x + buttonSize, y + buttonSize,
                10f, 10f, buttonPaint);
    }

    private void calculateLevels(ProgressNode node, Map<String, Integer> levels, int depth) {
        if (levels.containsKey(node.getId())) return;
        levels.put(node.getId(), depth);
        if (node.getNext() != null) {
            for (String nextId : node.getNext()) {
                ProgressNode nextNode = findNodeById(nextId);
                if (nextNode != null) {
                    calculateLevels(nextNode, levels, depth + 1);
                }
            }
        }
    }

    private ProgressNode findNodeById(String id) {
        for (ProgressNode node : nodes) {
            if (node.getId().equals(id)) {
                return node;
            }
        }
        return null;
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getPointerCount() == 1) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                PointF touchPoint = new PointF(event.getX(), event.getY());

                for (ProgressNode node : nodes) {
                    float x = (float) node.getX();
                    float y = (float) node.getY();
                    PointF nodePosition = new PointF(x, y);

                    // 检查是否点击了节点的圆圈（图标区域）
                    if (isPointInCircle(touchPoint, nodePosition)) {
                        showNodeDescription(node, nodePosition);
                        return true;
                    }

                    // 检查是否点击了按钮区域
                    if (isPointInButton(touchPoint, nodePosition)) {
                        ProgressNode beforeNode = findNodeById(node.getBefore());
                        if (beforeNode == null || beforeNode.getFinished()) {
                            node.setFinished(!node.getFinished());
                            invalidate(); // 刷新绘制
                        } else {
                            Toast.makeText(getContext(), "请先完成前置进度", Toast.LENGTH_SHORT).show();
                        }
                        return true;
                    }
                }
            }
        }
        return super.onTouchEvent(event);
    }


    private boolean isPointInCircle(PointF point, PointF nodePosition) {
        float centerX = nodePosition.x + nodeSize / 2; // 圆的中心点X
        float centerY = nodePosition.y + nodeSize / 2; // 圆的中心点Y
        float radius = nodeSize / 3; // 圆的半径
        return Math.pow(point.x - centerX, 2) + Math.pow(point.y - centerY, 2) <= Math.pow(radius, 2);
    }


    private boolean isPointInButton(PointF point, PointF nodePosition) {
        float buttonX = nodePosition.x + (nodeSize - buttonSize) / 2;
        float buttonY = nodePosition.y - buttonSize - 10f;
        return point.x >= buttonX && point.x <= buttonX + buttonSize &&
                point.y >= buttonY && point.y <= buttonY + buttonSize;
    }


    private void showNodeDescription(ProgressNode node, PointF position) {
        Context context = getContext();
        PopupWindow popupWindow = new PopupWindow(context);

        View popupView = View.inflate(context,
                R.layout.popup, null);
        TextView titleView = popupView.findViewById(R.id.title);
        TextView descriptionView = popupView.findViewById(R.id.description);

        titleView.setText(node.getName());
        descriptionView.setText(node.getDescription());

        popupWindow.setContentView(popupView);
        popupWindow.setWidth(600);
        popupWindow.setHeight(400);
        popupWindow.setFocusable(true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        int offsetX = (int) position.x;
        int offsetY = (int) position.y - 50;
        popupWindow.showAtLocation(this, Gravity.NO_GRAVITY, offsetX, offsetY);

        postDelayed(popupWindow::dismiss, 2000);
    }
    private void drawZigzagLine(Canvas canvas, PointF start, PointF end) {
        float startX = start.x + nodeSize / 2;
        float startY = start.y + nodeSize / 2;
        float endX = end.x + nodeSize / 2;
        float endY = end.y + nodeSize / 2;
        float midX = (startX + endX) / 2;

        Paint zigzagPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        zigzagPaint.setColor(Color.GRAY);
        zigzagPaint.setStrokeWidth(8f);
        zigzagPaint.setStyle(Paint.Style.STROKE);

        Path path = new Path();
        path.moveTo(startX, startY);

        if (startY != endY) {
            path.lineTo(midX, startY);
            path.lineTo(midX, endY);
        }
        path.lineTo(endX, endY);
        canvas.drawPath(path, zigzagPaint);
    }

}

