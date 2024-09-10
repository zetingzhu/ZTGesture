package com.zzt.gesture;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.customview.widget.ViewDragHelper;

/**
 * 移动改变视图大小
 */
public class MoveResizeLayoutV2 extends ConstraintLayout {
    private final static String TAG = "ResizeLayout";
    private ViewDragHelper viewDragHelper;

    private View gst_top,// 顶部被缩放视图
            gst_content, // 下发被控制的缩放内容
            gst_view, // 可被拖拽的小按钮
            gst_content_v1, // 下放被拖拽顶部
            gst_content_v2,// 下放被拖拽图标
            gst_bottom,// 下放被拖拽下单
            gst_bottom_open; // 底部可以直接打开视图

    private int gstViewHeight;// 拖拽按钮高度
    private int gstOpenHeight;// 底部视图默认高度


    private int gstOpenDefaultHeight;// 最底高度
    private final int gstMinHeight = 200;// 最高的高度
    private int gstViewDefaultTop;// 拖拽视图默认高度
    private int gstMiddleHeight;// 中间压缩视图高度
    private int moveOpenHeight;// 计算底部高度

    private float slipRate = 0.3f;// 控制滑动速率

    public MoveResizeLayoutV2(@NonNull Context context) {
        super(context);
        init();
    }

    public MoveResizeLayoutV2(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MoveResizeLayoutV2(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        viewDragHelper = ViewDragHelper.create(this, new ViewDragHelper.Callback() {
            @Override
            public boolean tryCaptureView(@NonNull View child, int pointerId) {
                boolean gstView = child == gst_content;
                Log.d(TAG, "上下移动 视图：" + gstView + " 视图 id:" + getResources().getResourceEntryName(child.getId()));
                if (!gstView && child == gst_bottom_open) {
                    // 飘到中间
                    int finishTop = gstViewDefaultTop;
                    gst_view.layout(0, finishTop, getWidth(), finishTop + gstViewHeight);
                    viewChanged(finishTop);
                }
                return gstView;
            }

            /**
             * STATE_IDLE 闲置状态;
             * STATE_DRAGGING 正在拖动;
             * STATE_SETTLING 放置到某个位置;
             */
            @Override
            public void onViewDragStateChanged(int state) {
                super.onViewDragStateChanged(state);

            }

            /**
             * 当你拖动的View位置发生改变的时候回调;
             * @param changedView 你当前拖动的这个View
             * @param left 距离左边的距离
             * @param top 距离右边的距离
             * @param dx x轴的变化量
             * @param dy y轴的变化量
             */
            @Override
            public void onViewPositionChanged(@NonNull View changedView, int left, int top, int dx, int dy) {
                super.onViewPositionChanged(changedView, left, top, dx, dy);
                viewChanged(top - gstViewHeight);
            }

            /**
             * 当View停止拖拽的时候调用的方法
             * @param releasedChild 你拖拽的这个View
             * @param xvel x轴的速率
             * @param yvel y轴的速率
             */
            @Override
            public void onViewReleased(@NonNull View releasedChild, float xvel, float yvel) {
                super.onViewReleased(releasedChild, xvel, yvel);
                viewReleased(releasedChild, xvel, yvel);
            }

            /**
             * 垂直方向滚动
             */
            @Override
            public int clampViewPositionVertical(@NonNull View child, int top, int dy) {
                int min = Math.min(gstOpenDefaultHeight + gstViewHeight, Math.max(gstMinHeight + gstViewHeight, (int) (top + dy * slipRate)));
//                Log.w(TAG, "上下移动 视图 min：" + min + " 视图 id:" + getResources().getResourceEntryName(child.getId()));
                return min;
            }
        });

    }

    /**
     * 计算视图高度
     */
    public void viewChanged(int top) {
        Log.e(TAG, "上下移动  top:" + top);
        if (gst_top != null) {
            // 上方高度
            ViewGroup.LayoutParams tap = gst_top.getLayoutParams();
            tap.height = top;
            gst_top.setLayoutParams(tap);

            if (top >= gstOpenDefaultHeight) {
                gst_bottom_open.setVisibility(VISIBLE);
            } else {
                gst_bottom_open.setVisibility(GONE);
            }
        }
    }

    /**
     * 停止拖拽
     */
    public void viewReleased(@NonNull View releasedChild, float xvel, float yvel) {
        int gstTop = releasedChild.getTop() - gstViewHeight;
        int topOrMiddleHalf = (gstViewDefaultTop - gstMinHeight) / 2;
        Log.i(TAG, "上下移动 viewReleased top:" + releasedChild.getTop() + " vt:" + gstViewDefaultTop + " m1:" + (gstMinHeight + topOrMiddleHalf));
        if (gstTop < (gstMinHeight + topOrMiddleHalf)) {
            // 飘到最上面
            gst_view.layout(0, gstMinHeight, getWidth(), gstMinHeight + gstViewHeight);
            viewChanged(gstMinHeight);
        } else if (gstTop >= (gstMinHeight + topOrMiddleHalf) && gstTop < (gstViewDefaultTop + gstMiddleHeight / 2)) {
            // 飘到中间
            int finishTop = gstViewDefaultTop;
            gst_view.layout(0, finishTop, getWidth(), finishTop + gstViewHeight);
            viewChanged(finishTop);
        } else if (gstTop >= (gstViewDefaultTop + gstMiddleHeight / 2) && gstTop < (gstOpenDefaultHeight - moveOpenHeight / 2)) {
            // 飘到下放建仓
            int finishTop = gstViewDefaultTop + gstMiddleHeight;
            gst_view.layout(0, finishTop, getWidth(), finishTop + gstViewHeight);
            viewChanged(finishTop);
        } else if (gstTop >= (gstOpenDefaultHeight - moveOpenHeight / 2)) {
            // 飘到底下点开按钮
            int finishTop = gstOpenDefaultHeight;
            gst_view.layout(0, finishTop, getWidth(), finishTop + gstViewHeight);
            viewChanged(finishTop);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        viewDragHelper.processTouchEvent(event);
        return true;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return viewDragHelper.shouldInterceptTouchEvent(ev);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (gst_content != null) {
            gstViewHeight = gst_view.getMeasuredHeight();
            gstOpenHeight = gst_bottom_open.getMeasuredHeight();

            // 获取移动视图最底部高度
            gstOpenDefaultHeight = (getHeight() - gstOpenHeight);
            gstViewDefaultTop = gst_top.getMeasuredHeight();
            gstMiddleHeight = gst_content_v2.getMeasuredHeight();
            Log.d(TAG, "顶部视图 topH:" + gstViewDefaultTop + " 画图高度：" + gstMiddleHeight + " 线高：" + gstViewHeight + " 和：" + (gstViewDefaultTop + gstMiddleHeight + gstViewHeight));

            moveOpenHeight = getHeight() - (gstViewDefaultTop + gstMiddleHeight) - gstOpenHeight;

            //计算完成之后， 最后把底部视图隐藏起来
            gst_bottom_open.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        gst_top = findViewById(R.id.gst_top);// 顶部被缩放视图
        gst_content = findViewById(R.id.gst_content); // 下发被控制的缩放内容
        gst_view = findViewById(R.id.gst_view); // 可被拖拽的小按钮
        gst_content_v1 = findViewById(R.id.gst_content_v1); // 下发被拖拽顶部
        gst_content_v2 = findViewById(R.id.gst_content_v2);// 下发被拖拽图标
        gst_bottom = findViewById(R.id.gst_bottom);// 下发被拖拽下单
        gst_bottom_open = findViewById(R.id.gst_bottom_open); // 底部可以直接打开视图

        if (gst_bottom_open != null) {
//            gst_bottom_open.setOnClickListener(new OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    // 飘到中间
//                    int finishTop = gstViewDefaultTop;
//                    gst_view.layout(0, finishTop, getWidth(), finishTop + gstViewHeight);
//                    viewChanged(finishTop);
//                }
//            });
        }
    }
}
