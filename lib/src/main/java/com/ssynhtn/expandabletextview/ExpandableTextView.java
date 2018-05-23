package com.ssynhtn.expandabletextview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by huangtongnao on 2018/5/22
 * 一个可以点击展开收拢文字的Layout
 */

public class ExpandableTextView extends AppCompatTextView {

    /**
     * 当前展开比例, 0~1
     */
    private float expansion;
    /**
     * 目标展开比例, 0或者1
     */
    private int targetExpansion;

    /**
     * 收拢状态下文字最大行数
     */
    private int collapsedLines;


    public ExpandableTextView(@NonNull Context context) {
        this(context, null);
    }

    public ExpandableTextView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ExpandableTextView);
        boolean expanded = typedArray.getBoolean(R.styleable.ExpandableTextView_text_expanded, false);
        expansion = targetExpansion = expanded ? 1 : 0;
        collapsedLines = typedArray.getInt(R.styleable.ExpandableTextView_text_collapsed_lines, 1);
        boolean clickToToggle = typedArray.getBoolean(R.styleable.ExpandableTextView_text_click_to_toggle, false);
        typedArray.recycle();

        setMinLines(collapsedLines);

        if (clickToToggle) {
            setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggle();
                }
            });
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMaxLines(collapsedLines);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int collapsedWidth = getMeasuredWidth();
        int collapsedHeight = getMeasuredHeight();

        setMaxLines(Integer.MAX_VALUE);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int expandedWidth = getMeasuredWidth();
        int expandedHeight = getMeasuredHeight();

        int width = (int) (expandedWidth * expansion + collapsedWidth * (1 - expansion));
        int height = (int) (expandedHeight * expansion + collapsedHeight * (1 - expansion));
        setMeasuredDimension(width, height);

        if (expansion == 0) {
            setMaxLines(collapsedLines);
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }

        if (textExpansionListener != null) {
            textExpansionListener.onCanExpand(expandedHeight > collapsedHeight);
        }
    }

    private ValueAnimator animator;
    public void expand() {
        expandTo(1);
    }

    public void collapse() {
        expandTo(0);
    }

    public void toggle() {
        expandTo(1 - targetExpansion);
    }


    private void expandTo(final int target) {
        if (animator != null) {
            animator.cancel();
            animator = null;
        }

        this.targetExpansion = target;

        if (expansion == targetExpansion) return;

        animator = ValueAnimator.ofFloat(expansion, targetExpansion);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float val = (float) animation.getAnimatedValue();
                setExpansion(val);

                if (textExpansionListener != null) {
                    textExpansionListener.onExpansionChanged(val, targetExpansion == 1);
                }
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            private boolean canceled;
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                if (textExpansionListener != null && !canceled) {
                    if (targetExpansion == 1) {
                        textExpansionListener.onExpanded();
                    } else {
                        textExpansionListener.onCollapsed();
                    }
                }

            }

            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
                canceled = true;

            }
        });
        animator.start();
    }


    private void setExpansion(float expansion) {
        if (this.expansion == expansion) return;
        this.expansion = expansion;
        requestLayout();
    }

    public interface TextExpansionListener {
        void onExpanded();
        void onCollapsed();
        void onExpansionChanged(float expansion, boolean isExpanding);
        void onCanExpand(boolean canExpand);
    }

    private TextExpansionListener textExpansionListener;

    public void setTextExpansionListener(TextExpansionListener textExpansionListener) {
        this.textExpansionListener = textExpansionListener;
    }
}
