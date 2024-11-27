package pers.ember.myapplication.View;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.transition.Transition;

import pers.ember.myapplication.Entity.ProgressNode;
import pers.ember.myapplication.R;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class ProgressGraphView extends View {
    private List<ProgressNode> nodes;
    private final float buttonSize = 50f;
    private MediaPlayer mediaPlayer;
    private Paint nodePaint;
    private Paint linePaint;
    private Paint textPaint;
    private Map<String, PointF> nodePositions;
    private Map<String, Bitmap> iconCache = new HashMap<>();
    private final float nodeSize = 150f;

    public ProgressGraphView(Context context, AttributeSet attrs) throws IOException {
        super(context, attrs);
        init();
    }


    private void init(){
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
        canvas.save();
        for (ProgressNode node : nodes) {
            // 绘制连线
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
            if (node.getFinished()) {
                nodePaint.setColor(Color.parseColor("#90EE90"));
            } else {
                nodePaint.setColor(Color.WHITE);
            }
            canvas.drawRoundRect(
                    x, y,
                    x + nodeSize, y + nodeSize,
                    30f, 30f, nodePaint
            );
            float buttonX = x + (nodeSize - buttonSize) / 2;
            float buttonY = y - buttonSize - 10f;
            drawButton(canvas, buttonX, buttonY);
            String iconName = node.getIcon();
            if (iconName != null && !iconName.isEmpty()) {
                Bitmap iconBitmap = iconCache.get(iconName);
                if (iconBitmap != null) {
                    canvas.drawBitmap(
                            iconBitmap,
                            x + (nodeSize - iconBitmap.getWidth()) / 2,
                            y + (nodeSize - iconBitmap.getHeight()) / 2,
                            null
                    );
                } else {
                    loadIcon(iconName);
                }
            } else {
                Paint iconPaint = new Paint();
                iconPaint.setColor(Color.LTGRAY);
                canvas.drawCircle(
                        x + nodeSize / 2,
                        y + nodeSize / 2,
                        nodeSize / 3, iconPaint
                );
            }
        }
        canvas.restore();
    }
    private void loadIcon(String iconName) {
        String url = "http://8.134.189.141:999/icons/" + iconName;
        Glide.with(getContext())
                .asBitmap()
                .load(url)
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(new com.bumptech.glide.request.target.CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        iconCache.put(iconName, resource);
                        invalidate();
                    }
                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                    }
                });
    }
    private void drawButton(Canvas canvas, float x, float y) {
        Paint buttonPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        buttonPaint.setColor(Color.parseColor("#007bff"));
        canvas.drawRoundRect(x, y, x + buttonSize, y + buttonSize, 10f, 10f, buttonPaint);
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

                    if (isPointInCircle(touchPoint, nodePosition)) {
                        showNodeDescription(node, nodePosition);
                        return true;
                    }

                    if (isPointInButton(touchPoint, nodePosition)) {
                        ProgressNode beforeNode = findNodeById(node.getBefore());
                        if (beforeNode == null || beforeNode.getFinished()) {
                            if (node.getFinished()){
                                node.setFinished(!node.getFinished());
                            }else {
                                node.setFinished(!node.getFinished());
                                playSound("http://8.134.189.141:999/sound/firework.mp3");
                            }
                            invalidate();
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
        float centerX = nodePosition.x + nodeSize / 2;
        float centerY = nodePosition.y + nodeSize / 2;
        float radius = nodeSize / 3;
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
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
    private void playSound(String url) {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.release();
            }
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(url);
            mediaPlayer.setOnPreparedListener(MediaPlayer::start);
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnCompletionListener(mp -> {
                mp.release();
                mediaPlayer = null;
            });
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "音效加载失败", Toast.LENGTH_SHORT).show();
        }
    }
}

