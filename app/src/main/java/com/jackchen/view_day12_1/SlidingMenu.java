package com.jackchen.view_day12_1;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.HorizontalScrollView;

/**
 * Email 2185134304@qq.com
 * Created by JackChen on 2018/2/10.
 * Version 1.0
 * Description:
 */
public class SlidingMenu extends HorizontalScrollView {

    //菜单宽度
    private int mMenuWidth;
    //内容的View、菜单的View
    private View mContentView , mMenuView ;

    //处理快速滑动
    GestureDetector mGestureDetector ;

    //判断菜单是否打开
    boolean mMenuIsOpen = false ;

    //是否拦截
    boolean mIsIntercept = false ;

    public SlidingMenu(Context context) {
        this(context, null);
    }

    public SlidingMenu(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlidingMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        // 初始化自定义属性
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.SlidingMenu);

        float rightMargin = array.getDimension(
                R.styleable.SlidingMenu_menuRightMargin, ScreenUtils.dip2px(context, 50));
        // 菜单页的宽度是 = 屏幕的宽度 - 右边的一小部分距离（自定义属性）
        mMenuWidth = (int) (getScreenWidth(context) - rightMargin);
        array.recycle();

        mGestureDetector = new GestureDetector(context , mGestureListener) ;
    }


    private GestureDetector.OnGestureListener mGestureListener = new GestureDetector.SimpleOnGestureListener(){
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            //快速往左边滑是负数，往右边滑动是正数
            Log.e("TAG", "velocityX -> " + velocityX);

            //由于我们只关心快速滑动,只要快速滑动就会回调onFling
            //当打开的时候，需要快速向右边滑动，就让关闭
            //当关闭的时候，需要快速向左边滑动，就让打开
            if (mMenuIsOpen){
                //关闭
                if (velocityX < 0){
                    closeMenu();
                    return true ;
                }
            }else{
                //打开
                if (velocityX > 0){
                    openMenu();
                    return true ;
                }
            }

            return super.onFling(e1, e2, velocityX, velocityY);

        }
    } ;


    //1.运行之后发现宽度不对（乱套了），这个时候需要去指定宽度，使用下边方法
    //这个方法是布局解析完毕后 即setContentView之后调用 也就是XML文件解析完毕后调用
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        /* ========== 指定宽高 START ========== */
        //1.内容页的宽度 = 屏幕的宽度
        //由于activity_main布局是 LinearLayout包裹了layout_home_menu和layout_home_content，
        // 所以先获取LinearLayout，然后从LinearLayout中获取2个子View

        //获取LinearLayout  这个为什么不是根布局 com.view.day12.SlidingMeun
        ViewGroup container = (ViewGroup) getChildAt(0);

        //这里获取LinearLayout容器中所有子View个数，判断只能放置2个子View，如果不是2个则抛异常
        int childCount = container.getChildCount();
        if (childCount != 2){
            throw new RuntimeException("只能放置两个子View!") ;
        }
        mMenuView = container.getChildAt(0);//获取LinearLayout的第一个子View，即菜单页
        ViewGroup.LayoutParams menuParams = mMenuView.getLayoutParams();//设置宽高只能通过 LayoutParams
        menuParams.width = mMenuWidth ;
        mMenuView.setLayoutParams(menuParams);//7.0以下手机必须采用下边的方式


        //2.菜单页宽度 = 屏幕宽度 - 右边一小段距离(自定义属性)
        mContentView = container.getChildAt(1) ;  //获取LinearLayout的第二个子View，即内容页
        ViewGroup.LayoutParams contentParams = mContentView.getLayoutParams();
        contentParams.width=getScreenWidth(getContext()) ;
        mContentView.setLayoutParams(contentParams);//7.0以下手机必须采用下边的方式

        /*  此时侧滑默认是打开的 ， 我们需要将侧滑关闭，需要调用smoothScrollTo()来关闭，并且需要在onLayout()方法调用*/

    }



    //4. 处理右边的缩放，左边呢的缩放和透明度,需要不断的获取当前滚动的位置
    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        // 算一个梯度值
        float scale = 1f * l / mMenuWidth;// scale 变化是 1 - 0
        // 右边的缩放: 最小是 0.7f, 最大是 1f
        float rightScale = 0.7f + 0.3f * scale;
        // 设置右边的缩放,默认是以中心点缩放
        // 设置缩放的中心点位置
        ViewCompat.setPivotX(mContentView, 0);
        ViewCompat.setPivotY(mContentView, mContentView.getMeasuredHeight() / 2);
        ViewCompat.setScaleX(mContentView,rightScale);
        ViewCompat.setScaleY(mContentView, rightScale);

        // 菜单的缩放和透明度
        // 透明度是 半透明到完全透明  0.5f - 1.0f
        float leftAlpha = 0.5f + (1-scale)*0.5f;
        ViewCompat.setAlpha(mMenuView,leftAlpha);
        // 缩放 0.7f - 1.0f
        float leftScale = 0.7f + (1-scale)*0.3f;
        ViewCompat.setScaleX(mMenuView,leftScale);
        ViewCompat.setScaleY(mMenuView, leftScale);

        // 最后一个效果 退出这个按钮刚开始是在右边，安装我们目前的方式永远都是在左边
        // 设置平移，先看一个抽屉效果
        // ViewCompat.setTranslationX(mMenuView,l);
        // 平移 l*0.7f
        ViewCompat.setTranslationX(mMenuView, 0.25f*l);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        smoothScrollTo(mMenuWidth , 0);
    }


    //3.手指抬起时二选一，侧滑要么关闭、要么打开

    @Override
    public boolean onTouchEvent(MotionEvent ev) {


        //如果有拦截，就不要执行自己的onTouchEvent了
        if (mIsIntercept){
            return true ;
        }

        //这里需要注意：如果快速滑动了，下边的代码就不要执行了
        //这里我们需要把onTouchEvent交给mGestureDetector来处理
         if (mGestureDetector.onTouchEvent(ev)){
             return true ;
         }




        //获取手指滑动的速率，当其大于一定值就认为是快速滑动，GestureDetector(系统提供好的类)
        //当菜单打开的时候，手指触摸右边内容部分需要关闭菜单，这个时候点击头像是没有反应的，不让其响应点击事件，
        // 所以这里还需要事件拦截，想都不用想，肯定在onInterceptTouchEvent()方法处理

        if (ev.getAction() == MotionEvent.ACTION_UP){
            //这个时候只需要管手指抬起，根据当前滚动的距离来判断
            int currentX = getScrollX() ;

            //由我画的图三分析可知，如果getScrollX()，就需要关闭侧滑；否则打开
            if (currentX > mMenuWidth/2){
                closeMenu() ;
            }else{
                //打开
                openMenu() ;
            }

            //确保super.onTouchEvent(ev)不会执行
            return true ;
        }
        return super.onTouchEvent(ev);
    }


    /**
     * 当菜单打开时，点击右上角头像，让菜单关闭；当菜单关闭时点击头像，就会进入个人中心页面
     *
     * 只要当这个方法 return true，就表示拦截：
     *     所以需要判断：
     *          1.菜单是打开的情况
     *          2.点击了头像
     *          这个时候就return true，去拦截，不让响应点击事件即可
     * @param ev
     * @return
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        mIsIntercept = false ;

        if (mMenuIsOpen){
            float currentX = ev.getX() ;
            if (currentX > mMenuWidth){
                //1.关闭菜单
                closeMenu();
                //2.子View不需要相应任何事件的点击和触摸 , 拦截子View的事件，直接return true即可

                //这里需要注意：如果返回true，代表我会拦截子View的事件，但是我会相应自己的onTouchEvent事件
                mIsIntercept = true ;
                return true ;
            }
        }
        return super.onInterceptTouchEvent(ev);
    }

    /**
     * 打开菜单 滚动到 0 的位置
     */
    private void openMenu() {
        // smoothScrollTo 有动画
        smoothScrollTo(0, 0);
        mMenuIsOpen = true ;
    }

    /**
     * 关闭菜单 滚动到 mMenuWidth 的位置
     */
    private void closeMenu() {
        smoothScrollTo(mMenuWidth, 0);
        mMenuIsOpen = false ;
    }

    /**
     * 获得屏幕高度
     *
     * @param context
     * @return
     */
    private int getScreenWidth(Context context) {
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.widthPixels;
    }

    /**
     * Dip into pixels
     */
    private int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

}
