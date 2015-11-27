package layoutdemo.heima.mq.library;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * @项目名: JellybeansLayout
 * @包名: layoutdemo.heima.mq.library
 * @文件名: JellybeansLayout
 * @创建者: mai
 * @创建时间: 2015/11/26 22:58
 * @描述: TODO
 */
public class JellybeansLayout extends ViewGroup{


    private static final String TAG = "JellybeansLayout";
    private List<Line> mLines    = new ArrayList<>();//行的数量
    //当前行
    private Line mCurrentLine = null;
    private int mVerticalSpace = 15;//垂直间距
    private int mHorizontalSpace = 15;//水平的间隙

    public JellybeansLayout(Context context) {
        this(context, null);
    }

    public JellybeansLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setSpace(int vertical, int horizontal){
        this.mVerticalSpace = vertical;
        this.mHorizontalSpace = horizontal;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        Log.d(TAG, "onMeasure");
        mCurrentLine = null;
        mLines.clear();

//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //自己的宽度
        int width = MeasureSpec.getSize(widthMeasureSpec);
        //垂直的间隙



        //1 测量子View的宽度和高度
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == View.GONE) {
                continue;
            }
            //测量孩子
            measureChild(child, widthMeasureSpec, heightMeasureSpec);

            if (mCurrentLine == null) {
                //说明一行都没有
                mCurrentLine = new Line(width, mHorizontalSpace);

                //添加行到控件中
                mLines.add(mCurrentLine);
                //添加子View到行中
                mCurrentLine.addView(child);
            }else {

                if (mCurrentLine.canAdd(child)) {
                    //行可以添加就添加
                    mCurrentLine.addView(child);
                }else {
                    //不可以添加就新建行
                    mCurrentLine = new Line(width, mHorizontalSpace);
                    mLines.add(mCurrentLine);

                    //添加到新行中
                    mCurrentLine.addView(child);
                }
            }
        }

        //2 测量自己的宽度和高度
        int height = getPaddingBottom() + getPaddingTop();
        for (int i = 0; i < mLines.size(); i++) {
            Line line = mLines.get(i);

            height += line.mLineHeight;

            if (i != mLines.size()-1){
                // 不是最后一个,都添加间隙
                height += mVerticalSpace;
            }
        }
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        int top = getPaddingTop();
        for (int i = 0; i < mLines.size(); i++) {
            Line line = mLines.get(i);
            line.layout(getPaddingLeft(), top);
            top += line.mLineHeight + mVerticalSpace;
        }
    }

    private class Line{
        //记录子控件的
        //1 属性
        //2 方法
        //3 构造

        //记录一行中子控件的数量
        private List<View> mViews = new ArrayList<>();


        private int mLineUseWidth;//已经使用的宽度
        private int mLineMaxWidth;//行的最大宽度
        private int mLineHeight;
        private int mSpace;//子VIew之间的间隙

        /**
         * 最大宽度和间隙应该有外部决定,同构造获得
         * @param lineMaxWidth
         * @param space
         */
        private Line(int lineMaxWidth, int space){
            this.mLineMaxWidth = lineMaxWidth;

            this.mSpace = space;
        }

        /**
         * 判断当前行还能不能添加子View
         * @return
         */
        public boolean canAdd(View view){

            if (mViews == null) {
                //一个子View都没有则可以添加
                return true;
            }
            int childWidth = view.getMeasuredWidth();
            if (mLineUseWidth + mSpace + childWidth <= mLineMaxWidth) {

                //如果想加入的子View的宽度加上已经存在的宽度,小于等于最大宽度,则可以添加
                return true;
            }

            return false;
        }

        /**
         * 添加前 用canAdd 判断还能不能添加
         * @param view
         */
        public void addView(View view){
            int childWidth = view.getMeasuredWidth();
            int childHeight = view.getMeasuredHeight();

            if (mViews == null){
                //集合中没有成员,这是第一个子View
                mLineUseWidth += childWidth;

                //高度
                mLineHeight = childHeight;
            } else{
                mLineUseWidth += childWidth + mSpace;

                //用最高的子View的高度作为行的高度
                mLineHeight = mLineHeight > childHeight?
                              mLineHeight:childHeight;
            }



            mViews.add(view);
        }

        public void layout(int left, int top) {

            //计算多余空间的值,并平均分给每一个子View
            int extraWidth = mLineMaxWidth - mLineUseWidth;
            int avgWidth = (int) (extraWidth* 1f / mViews.size() +0.5f);



            for (int i = 0; i < mViews.size(); i++) {
                View child = mViews.get(i);
                int childWidth = child.getMeasuredWidth();
                int childHeight = child.getMeasuredHeight();


                //多余空间的值平均分给每一个子View
                if (avgWidth > 0) {
                    int measureWidth = MeasureSpec.makeMeasureSpec(childWidth + avgWidth, MeasureSpec.EXACTLY);
                    int measureHeight= MeasureSpec.makeMeasureSpec(childHeight, MeasureSpec.EXACTLY);
                    child.measure(measureWidth, measureHeight);

                    //更新平均分多余空间后的新值
                    childWidth = child.getMeasuredWidth();
                    childHeight = child.getMeasuredHeight();
                }


                int l = left;
                int t = (int) (top + (mLineHeight - childHeight)/2.0f +0.5f);
                int r = l + childWidth;
                int b = t + childHeight;
                child.layout(l, t, r, b);


                //更新左距离,右距离也会更新
                left += childWidth + mSpace;
            }
        }
    }
}
