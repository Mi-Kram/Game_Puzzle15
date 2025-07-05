package com.kramarenko.puzzle15.Models;

import android.annotation.SuppressLint;
import androidx.annotation.NonNull;

import java.io.Serializable;

public class Point implements Serializable {
	public int row;
	public int col;

	public Point(int row, int col){
		this.row = row;
		this.col = col;
	}

	public Point(Point p){
		this.row = p.row;
		this.col = p.col;
	}

	public void set(int row, int col){
		this.row = row;
		this.col = col;
	}

	public void set(Point p){
		this.row = p.row;
		this.col = p.col;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Point){
			Point p = (Point) obj;
			return p.row == row && p.col == col;
		}
		return false;
	}

	public boolean equals(int row, int col) {
		return this.row == row && this.col == col;
	}

	public boolean equalsRows(Point p){
		return p.row == row;
	}

	public boolean equalsCols(Point p){
		return p.col == col;
	}

	public boolean isNear(Point p){
		return Math.abs(row-p.row) + Math.abs(col-p.col) == 1;
	}

	@NonNull
	@Override
	@SuppressLint("DefaultLocale")
	public String toString() {
		return String.format("(%d, %d)", row, col);
	}

	public Point create(int rowDelta, int colDelta){
		return new Point(row+rowDelta, col+colDelta);
	}

}
