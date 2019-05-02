package com.pro.arcprogressbar;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;

/*
* @author Jerry
* create at 2019/5/2 下午5:05
* description:自定义view，一个圆形进度条
*/
public class ArcProgressBar extends View {

    private RectF bgRect;
    //画刷
    private Paint allArcPaint;
    private Paint progressPaint;
    private Paint vTextPaint;
    private Paint hintPaint;
    private Paint curSpeedPaint;


    private String hintColor = "#676767";
    private String bgArcColor = "#111111";

    private ValueAnimator progressAnimator;////属性动画
    private PaintFlagsDrawFilter mDrawFilter;
    private SweepGradient sweepGradient;//扫描渲染
    private Matrix rotateMatrix;


    private int diameter = 500;//圆的直径
    private float centerX;  //圆心X坐标
    private float centerY;  //圆心Y坐标
    private int[] colors = new int[]{Color.GREEN, Color.YELLOW, Color.RED, Color.RED};

    private float startAngle = 135;
    private float sweepAngle = 360;//进度条的角度，最大为360，即一圈
    private float bgArcWidth = dipToPx(2);//进度条背景的宽度，即圆圈黑线的宽度
    private float progressWidth = dipToPx(10);// 进度条的宽度，是画出来的进度条的宽度，彩色的进度条部分的宽度
    private float maxValues = 100;//最大值
    private float curValues = 0;//当前值
    private float currentAngle = 0;//当前角度
    private float lastAngle;//之前的角度
    private int aniSpeed = 2000;//动画速度，持续时间（毫秒）
    private float textSize = dipToPx(60);//文字大小
    private float hintSize = dipToPx(15);
    private float curSpeedSize = dipToPx(13);

    private boolean isNeedTitle;//是否有标题
    private boolean isNeedUnit;//是否有单位
    private boolean isNeedContent;//是否有内容（将进度展示出来）


    private String unitString;//单位
    private String titleString;//标题

    private float k;// sweepAngle / maxValues 的值，即每单位所占角度
    
    //自定view有4个构造方法
    //在java中new view用的是第一个构造方法
    public ArcProgressBar(Context context) {
        super(context);
        initView();
    }

    //在xml文件中声明，用的是第二个构造方法
    public ArcProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        initCofig(context, attrs);
        initView();
    }

    //不自动调用，如果view有style属性，一般是在第二个构造方法中主动调用
    public ArcProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initCofig(context, attrs);
        initView();
    }

    //不自动调用，如果view有style属性，一般是在第二个构造方法中主动调用
    public ArcProgressBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initCofig(context, attrs);
        initView();
    }
    
    /*
     * @author: Jerry
     * @create at 2019/5/2 下午5:06
     * @Param: 
     * @description: 从xml中初始化布局配置, 获取参数值，没有则取默认值
     * @return: 
     */
    private void initCofig(Context context, AttributeSet attrs){
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ArcProgressBar);
        int color1 = typedArray.getColor(R.styleable.ArcProgressBar_front_color1, Color.GREEN);//如果没有指定，则默认是绿色
        int color2 = typedArray.getColor(R.styleable.ArcProgressBar_front_color2, color1);
        int color3 = typedArray.getColor(R.styleable.ArcProgressBar_front_color3, color1);
        colors = new int[]{color1, color2, color3, color3};
        sweepAngle = typedArray.getInteger(R.styleable.ArcProgressBar_total_engle, 360);
        bgArcWidth = typedArray.getDimension(R.styleable.ArcProgressBar_back_width, dipToPx(2));
        progressWidth = typedArray.getDimension(R.styleable.ArcProgressBar_front_width, dipToPx(10));
        isNeedTitle = typedArray.getBoolean(R.styleable.ArcProgressBar_is_need_title, false);
        isNeedContent = typedArray.getBoolean(R.styleable.ArcProgressBar_is_need_content, false);
        isNeedUnit = typedArray.getBoolean(R.styleable.ArcProgressBar_is_need_unit, false);
        unitString = typedArray.getString(R.styleable.ArcProgressBar_string_unit);
        titleString = typedArray.getString(R.styleable.ArcProgressBar_string_title);
        float currentValues = typedArray.getFloat(R.styleable.ArcProgressBar_current_value, 0);
        float maxValue = typedArray.getFloat(R.styleable.ArcProgressBar_max_value, 100);
        setMaxValues(maxValue);//设置最大值
        setCurrentValues(currentValues);//设置当前值
        typedArray.recycle();
    }
    /*
     * @author: Jerry
     * @create at 2019/5/2 下午7:03
     * @Param:
     * @description: 初始化view参数
     * @return:
     */
    private void initView() {
        diameter = 3 * getScreenWidth() / 5;//圆的直径是屏幕宽度的3/5
        //弧形的矩阵区域
        bgRect = new RectF();
        bgRect.top = progressWidth/2;
        bgRect.left =  progressWidth/2;
        bgRect.right = diameter + progressWidth/2;
        bgRect.bottom = diameter + progressWidth/2 ;

        //圆心
        centerX = (progressWidth + diameter)/2;
        centerY = (progressWidth + diameter)/2;


        //整个弧形
        allArcPaint = new Paint();
        allArcPaint.setAntiAlias(true);
        allArcPaint.setStyle(Paint.Style.STROKE);
        allArcPaint.setStrokeWidth(bgArcWidth);
        allArcPaint.setColor(Color.parseColor(bgArcColor));
        allArcPaint.setStrokeCap(Paint.Cap.ROUND);

        //当前进度的弧形
        progressPaint = new Paint();
        progressPaint.setAntiAlias(true);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);
        progressPaint.setStrokeWidth(progressWidth);
        progressPaint.setColor(Color.GREEN);

        //内容显示文字
        vTextPaint = new Paint();
        vTextPaint.setTextSize(textSize);
        vTextPaint.setColor(Color.BLACK);
        vTextPaint.setTextAlign(Paint.Align.CENTER);

        //显示单位文字
        hintPaint = new Paint();
        hintPaint.setTextSize(hintSize);
        hintPaint.setColor(Color.parseColor(hintColor));
        hintPaint.setTextAlign(Paint.Align.CENTER);

        //显示标题文字
        curSpeedPaint = new Paint();
        curSpeedPaint.setTextSize(curSpeedSize);
        curSpeedPaint.setColor(Color.parseColor(hintColor));
        curSpeedPaint.setTextAlign(Paint.Align.CENTER);

        mDrawFilter = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);


        //产生渐变颜色的关键SweepGradient
        //扫描渲染,参数分别为中心点坐标，中心点y坐标，围绕中心渲染的颜色数组，至少要有两种颜色值，
        // 最后一个参数为相对位置的颜色数组,可为null,若为null,则颜色沿渐变线均匀分布
        sweepGradient = new SweepGradient(centerX, centerY, colors, null);


        rotateMatrix = new Matrix();

    }

    /*
     * @author: Jerry
     * @create at 2019/5/2 下午6:00
     * @Param:
     * @description: 测量
     * @return:
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //宽度 = 进度条的宽度 + 圆的直径
        int width = (int) (progressWidth + diameter);
        //高度 = 进度条的宽度 + 圆的直径
        int height= (int) (progressWidth + diameter);
        setMeasuredDimension(width, height);
    }



    /*
     * @author: Jerry
     * @create at 2019/5/2 下午9:15
     * @Param:
     * @description: 绘制
     * @return:
     */
    @Override
    protected void onDraw(Canvas canvas) {
        //抗锯齿
        canvas.setDrawFilter(mDrawFilter);

        //整个弧
        canvas.drawArc(bgRect, startAngle, sweepAngle, false, allArcPaint);

        //设置渐变色
        rotateMatrix.setRotate(130, centerX, centerY);
        sweepGradient.setLocalMatrix(rotateMatrix);
        progressPaint.setShader(sweepGradient);

        //当前进度
        canvas.drawArc(bgRect, startAngle, currentAngle, false, progressPaint);

        if (isNeedContent) {
            canvas.drawText(String.format("%.0f", curValues), centerX, centerY + textSize / 3, vTextPaint);
        }
        if (isNeedUnit) {
            canvas.drawText(unitString, centerX, centerY + 2 * textSize / 3, hintPaint);
        }
        if (isNeedTitle) {
            canvas.drawText(titleString, centerX, centerY - 2 * textSize / 3, curSpeedPaint);
        }
        //invalidate();
    }

    /**
     * 为进度设置动画
     * @param last 之前的位置
     * @param current 当前要到到位置
     * @param length 持续时间
     */
    private void setAnimation(float last, float current, int length) {
        progressAnimator = ValueAnimator.ofFloat(last, current);
        progressAnimator.setDuration(length);
        progressAnimator.setTarget(currentAngle);
        progressAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                currentAngle= (float) animation.getAnimatedValue();
                curValues = currentAngle/k;
                invalidate();
            }
        });
        progressAnimator.start();
    }
    /**
     * 得到屏幕宽度
     * @return
     */
    private int getScreenWidth() {
        WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.widthPixels;
    }

    /**
     * dip 转换成px
     * @param dip
     * @return
     */
    private int dipToPx(float dip) {
        float density = getContext().getResources().getDisplayMetrics().density;
        return (int)(dip * density + 0.5f * (dip >= 0 ? 1 : -1));
    }


//***********************一些参数的设置方法***********************//

    /**
     * 设置最大值
     * @param maxValues
     */
    public void setMaxValues(float maxValues) {
        this.maxValues = maxValues;
        k = sweepAngle/maxValues;
    }
    /**
     * 设置当前值
     * @param currentValues
     */
    public void setCurrentValues(float currentValues) {

        if (currentValues > maxValues) {//超过最大值则设为最大值，保证不超过
            currentValues = maxValues;
        }
        if (currentValues < 0) {//小于0 则设为0，不出现负值
            currentValues = 0;
        }
        this.curValues = currentValues;
        lastAngle = currentAngle;
        if(lastAngle == currentValues * k) return;//如果没有更新，那么就不用有动画
        setAnimation(lastAngle, currentValues * k, aniSpeed);
    }
    /**
     * 设置整个圆弧宽度
     * @param bgArcWidth
     */
    public void setBgArcWidth(int bgArcWidth) {
        this.bgArcWidth = bgArcWidth;
    }

    /**
     * 设置进度宽度
     * @param progressWidth
     */
    public void setProgressWidth(int progressWidth) {
        this.progressWidth = progressWidth;
    }

    /**
     * 设置标题大小
     * @param textSize
     */
    public void setTextSize(int textSize) {
        this.textSize = textSize;
    }

    /**
     * 设置单位文字大小
     * @param hintSize
     */
    public void setHintSize(int hintSize) {
        this.hintSize = hintSize;
    }

    /**
     * 设置单位文字
     * @param unitString
     */
    public void setUnit(String unitString) {
        this.unitString = unitString;
        invalidate();
    }

    /**
     * 设置直径大小
     * @param diameter
     */
    public void setDiameter(int diameter) {
        this.diameter = dipToPx(diameter);
    }

    /**
     * 设置标题
     * @param title
     */
    public void setTitle(String title){
        this.titleString = title;
    }

    /**
     * 设置是否显示标题
     * @param isNeedTitle
     */
    public void setIsNeedTitle(boolean isNeedTitle) {
        this.isNeedTitle = isNeedTitle;
    }

    /**
     * 设置是否显示单位文字
     * @param isNeedUnit
     */
    public void setIsNeedUnit(boolean isNeedUnit) {
        this.isNeedUnit = isNeedUnit;
    }

    /**
    * 设置颜色
    * @Param:
    */
    public void setColors(int []colors){
        this.colors = colors;
        sweepGradient = new SweepGradient(centerX, centerY, colors, null);
    }
}
