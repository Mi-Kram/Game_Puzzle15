package com.kramarenko.puzzle15.Models;

import android.view.View;
import android.widget.TextView;

import com.google.android.material.imageview.ShapeableImageView;

public class NumItemViewHolder {

	View layout;
	ShapeableImageView background;
	TextView text;
	int num;

	public NumItemViewHolder(View layout, ShapeableImageView background, TextView text, int num) {
		this.layout = layout;
		this.background = background;
		this.text = text;
		this.num = num;
	}

}
