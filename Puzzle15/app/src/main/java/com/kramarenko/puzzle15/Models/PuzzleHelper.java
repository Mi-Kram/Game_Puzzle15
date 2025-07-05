package com.kramarenko.puzzle15.Models;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;

import com.google.android.material.imageview.ShapeableImageView;
import com.kramarenko.puzzle15.Events.OnSolveError;
import com.kramarenko.puzzle15.Events.OnSolvedEvent;
import com.kramarenko.puzzle15.R;
import com.kramarenko.puzzle15.UI_Components.SquaredRelativeLayout;

import java.util.ArrayList;

public class PuzzleHelper implements SquaredRelativeLayout.OnSizeChanged, OnSolvedEvent, OnSolveError {

	Context context;
	SquaredRelativeLayout board;
	Puzzle puzzle;
	LayoutInflater layoutInflater;
	@IntRange(from = 0) int moveDelay = 100;
	@IntRange(from = 0) int solveDelay = 100;
	int numItemSize = 0;
	OnSolvedEvent solvedEvent = null;
	transient boolean isSolving = false;
	transient SolveThread solveThread = null;

	public PuzzleHelper(
			@NonNull Context context,
			@NonNull SquaredRelativeLayout board,
			@NonNull Puzzle puzzle) {
		this.context = context;
		this.board = board;
		this.puzzle = puzzle;
		this.layoutInflater = LayoutInflater.from(context);

		this.board.setOnSizeChangedListener(this);
		invalidate();
	}

	public void setOnSolvedListener(OnSolvedEvent listener){
		this.solvedEvent = listener;
	}

	public void setMoveDelay(@IntRange(from = 0) int moveDelay) {
		this.moveDelay = moveDelay;
	}

	public void setSolveDelay(@IntRange(from = 0) int solveDelay){
		this.solveDelay = solveDelay;
	}

	private int getItemSize(){
		int boardSize = board.getSize();
		int boardPadding = board.getPaddingLeft() * 2;
		boardSize -= boardPadding;
		return Math.max(0, boardSize / puzzle.getSize());
	}

	public synchronized void invalidate() {
		numItemSize = getItemSize();
		if(numItemSize == 0) return;

		board.removeAllViews();

		int rowCount = puzzle.getSize();
		int textSize = 60;

		TextView tmpTextView = new TextView(context);
		tmpTextView.setText(String.valueOf(rowCount*rowCount-1));
		tmpTextView.setTextSize(textSize);
		tmpTextView.setTypeface(Typeface.DEFAULT_BOLD);
		tmpTextView.measure(0, 0);       // must call measure!

		while (tmpTextView.getMeasuredWidth()+10 > numItemSize ||
					tmpTextView.getMeasuredHeight()+10 > numItemSize){
			tmpTextView.setTextSize(--textSize);
			tmpTextView.measure(0, 0);       //must call measure!
		}

		for (int i = 0; i < rowCount; i++) {
			for (int j = 0; j < rowCount; j++) {
				int num = puzzle.get(i, j);
				if(num == 0) continue;

				View layout = layoutInflater.inflate(R.layout.num_item, board, false);
				RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(numItemSize, numItemSize);
				params.setMarginStart(j*numItemSize);
				params.topMargin = i*numItemSize;
				layout.setLayoutParams(params);

				ShapeableImageView background = layout.findViewById(R.id.background);
				background.setOnClickListener(this::onNumItemClick);

				TextView text = layout.findViewById(R.id.text);
				text.setText(String.valueOf(num));
				text.setTextSize(textSize);

				NumItemViewHolder viewHolder = new NumItemViewHolder(layout, background, text, num);
				layout.setTag(viewHolder);
				background.setTag(viewHolder);

				board.addView(layout);
			}
		}
	}

	public void move(NumItemViewHolder viewHolder){
		Point point = puzzle.getNumPoint(viewHolder.num);
		Point zeroPoint = puzzle.getZeroPoint();
		if(!puzzle.simpleMove(point)) return;

		View numView = viewHolder.layout;
		RelativeLayout.LayoutParams p = (RelativeLayout.LayoutParams) numView.getLayoutParams();
		int startLeft = p.leftMargin, startTop = p.topMargin;
		int endLeft = zeroPoint.col*numItemSize, endTop = zeroPoint.row*numItemSize;

		Animation anim = new Animation() {

			@Override
			protected void applyTransformation(float interpolatedTime, Transformation t) {
				RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) numView.getLayoutParams();
				params.leftMargin = startLeft + (int)((endLeft - startLeft)*interpolatedTime);
				params.topMargin = startTop + (int)((endTop - startTop)*interpolatedTime);
				numView.setLayoutParams(params);
			}
		};
		anim.setDuration(solveDelay); // in ms
		anim.setInterpolator(new DecelerateInterpolator());
		numView.startAnimation(anim);

		if(puzzle.isSolved() && solvedEvent != null) solvedEvent.onPuzzleSolved();
	}

	public void reset(){
		stopSolvingThread();
		puzzle.newPuzzle();
		invalidate();
	}

	public void shuffle(){
		puzzle.newPuzzle();
		puzzle.shuffle();
		invalidate();
	}

	public boolean isSolved(){
		return puzzle.isSolved();
	}

	public boolean solve() {
		if(solveThread != null) return true;

		numItemSize = getItemSize();
		if(numItemSize == 0) return false;

		int rowCount = puzzle.getSize();
		if(rowCount*rowCount-1 != board.getChildCount()) return false;
		isSolving = true;

		Point zeroPoint = puzzle.getZeroPoint();
		View[][] views = new View[rowCount][rowCount];

		views[zeroPoint.row][zeroPoint.col] = null;
		for (int i = 0; i < board.getChildCount(); i++) {
			View child = board.getChildAt(i);
			NumItemViewHolder viewHolder = (NumItemViewHolder) child.getTag();

			Point point = puzzle.getNumPoint(viewHolder.num);
			views[point.row][point.col] = child;
		}

		try {
			puzzle.solve();
		}
		catch (UncorrectedPuzzleException ex){
			Toast.makeText(context, "Uncorrected puzzle!", Toast.LENGTH_LONG).show();
			isSolving = false;
			reset();
			return false;
		}

		solveThread = new SolveThread(views, zeroPoint, puzzle.moves, this, this);
		solveThread.start();

		return true;
	}

	public void stopSolvingThread(){
		if(solveThread != null){
			isSolving = false;
			solveThread.interrupt();
			solveThread = null;
		}
	}

	public boolean isAutoSolving() {
		return isSolving;
	}

	public void setPuzzleSize(@IntRange(from=3, to=10) int newPuzzleSize){
		stopSolvingThread();
		puzzle.setSize(newPuzzleSize);
		invalidate();
	}

	public void onNumItemClick(View view) {
		if(isSolving) return;
		move((NumItemViewHolder) view.getTag());
	}

	@Override
	public void onSizeChanged(int size) {
		invalidate();
	}

	@Override
	public void onPuzzleSolved() {
		solveThread = null;
		isSolving = false;
		if(solvedEvent != null) solvedEvent.onPuzzleSolved();
	}

	@Override
	public void onSolveError() {
		solveThread = null;
		isSolving = false;
		reset();
		solvedEvent.onPuzzleSolved();
	}

	public class SolveThread extends Thread {

		Point zeroPoint;
		ArrayList<Point> moves;
		View[][] views;
		OnSolvedEvent onSolveListener;
		OnSolveError onSolveErrorListener;

		public SolveThread(
				@NonNull View[][] views,
				@NonNull Point zeroPoint,
				@NonNull ArrayList<Point> moves,
				OnSolvedEvent onSolveListener,
				OnSolveError onSolveErrorListener) {
			this.views = views;
			this.zeroPoint = zeroPoint;
			this.moves = moves;
			this.onSolveListener = onSolveListener;
			this.onSolveErrorListener = onSolveErrorListener;
		}

		@Override
		public void run() {
			try {
				for (Point point : moves) {
					Thread.sleep(Math.max(1, solveDelay));
					if(Thread.interrupted()) break;

					View view = views[point.row][point.col];
					views[zeroPoint.row][zeroPoint.col] = view;
					views[point.row][point.col] = null;

					((Activity)context).runOnUiThread(new moveNumInThread(view, zeroPoint));
					zeroPoint = point;
				}

				if(onSolveListener != null) {
					((Activity)context).runOnUiThread(() -> onSolveListener.onPuzzleSolved());
				}
			} catch (Exception ex) {
				if(onSolveErrorListener != null)
					((Activity)context).runOnUiThread(() -> onSolveErrorListener.onSolveError());
			}
		}

		private class moveNumInThread implements Runnable {

			View view;
			Point zero;

			public moveNumInThread(View view, Point zero) {
				this.view = view;
				this.zero = zero;
			}

			@Override
			public void run() {
				if (solveDelay < 30) {
					RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) view.getLayoutParams();
					params.leftMargin = zero.col*numItemSize;
					params.topMargin = zero.row*numItemSize;
					view.setLayoutParams(params);
					return;
				}

				RelativeLayout.LayoutParams p = (RelativeLayout.LayoutParams) view.getLayoutParams();
				int startLeft = p.leftMargin, startTop = p.topMargin;
				int endLeft = zero.col*numItemSize, endTop = zero.row*numItemSize;

				Animation anim = new Animation() {

					@Override
					protected void applyTransformation(float interpolatedTime, Transformation t) {
						RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) view.getLayoutParams();
						params.leftMargin = startLeft + (int)((endLeft - startLeft)*interpolatedTime);
						params.topMargin = startTop + (int)((endTop - startTop)*interpolatedTime);
						view.setLayoutParams(params);
					}
				};
				anim.setDuration(solveDelay); // in ms
				anim.setInterpolator(new DecelerateInterpolator());

				view.startAnimation(anim);
			}
		}

	}
}
