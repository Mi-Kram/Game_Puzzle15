package com.kramarenko.puzzle15.UI_Components;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

public class SquaredRelativeLayout extends RelativeLayout {

	protected int size = 0;
	protected OnSizeChanged sizeChangedEvent = null;

	public SquaredRelativeLayout(Context context) {
		super(context);
	}

	public SquaredRelativeLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SquaredRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}


	public void setOnSizeChangedListener(OnSizeChanged listener){
		sizeChangedEvent = listener;
	}

	public int getSize() {
		return size;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);

		int newSize;
		if (widthMode == MeasureSpec.EXACTLY && widthSize > 0) newSize = widthSize;
		else if (heightMode == MeasureSpec.EXACTLY && heightSize > 0) newSize = heightSize;
		else newSize = Math.min(widthSize, heightSize);

		if(newSize != size){
			size = newSize;
			if(sizeChangedEvent != null) sizeChangedEvent.onSizeChanged(size);
		}
		int finalMeasureSpec = MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY);
		super.onMeasure(finalMeasureSpec, finalMeasureSpec);
	}

	public interface OnSizeChanged{
		void onSizeChanged(int size);
	}

}
