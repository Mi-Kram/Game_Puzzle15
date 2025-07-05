package com.kramarenko.puzzle15;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.kramarenko.puzzle15.Models.Puzzle;
import com.kramarenko.puzzle15.Models.PuzzleHelper;
import com.kramarenko.puzzle15.UI_Components.SquaredRelativeLayout;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

	SquaredRelativeLayout puzzleBoard;
	Puzzle puzzle;
	PuzzleHelper puzzleHelper;

	Button resetButton;
	Button newGameButton;
	Button solveButton;

	TextView timeView;
	Timer timeUpdater;
	View speedLayout;
	SeekBar speedBar;

	final int minSolveSpeed = 0;
	final int maxSolveSpeed = 500;
	int defaultSolveSpeed = 100;

	long solveStartTime = 0;
	class TimeUpdaterTask extends TimerTask {
		@Override
		public void run() {
			MainActivity.this.runOnUiThread(() -> setSolveTime((new Date().getTime() - solveStartTime) / 1000));
		}
	}

	private static final String PUZZLE_KEY = "puzzle";
	private static final String TIME_KEY = "time";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if(savedInstanceState == null || !savedInstanceState.containsKey(PUZZLE_KEY)){
			puzzle = new Puzzle(4);
		}
		else {
			puzzle = (Puzzle) savedInstanceState.getSerializable(PUZZLE_KEY);
			solveStartTime = savedInstanceState.getLong(TIME_KEY, 0);
		}

		puzzleBoard = findViewById(R.id.board);
		puzzleHelper = new PuzzleHelper(this, puzzleBoard, puzzle);
		puzzleHelper.setOnSolvedListener(this::onPuzzleSolved);
		puzzleHelper.setMoveDelay(100);
		puzzleHelper.setSolveDelay(defaultSolveSpeed);

		resetButton = findViewById(R.id.reset);
		newGameButton = findViewById(R.id.new_game);
		solveButton = findViewById(R.id.solve);

		resetButton.setOnClickListener(this::onResetClick);
		newGameButton.setOnClickListener(this::onNewGameClick);
		solveButton.setOnClickListener(this::onSolveClick);

		timeView = findViewById(R.id.time);
		speedLayout = findViewById(R.id.speed_layout);
		speedBar = findViewById(R.id.speed);
		speedBar.setMin(minSolveSpeed);
		speedBar.setMax(maxSolveSpeed);

		speedBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
				puzzleHelper.setSolveDelay(seekBar.getMax() - seekBar.getProgress());
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) { }

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) { }
		});

		if(!puzzle.isSolved()) {
			newGameButton.setVisibility(View.INVISIBLE);
			solveButton.setVisibility(View.VISIBLE);
			resetButton.setVisibility(View.VISIBLE);

			if (solveStartTime == 0) solveStartTime = new Date().getTime();
			timeUpdater = new Timer();
			timeUpdater.schedule(new TimeUpdaterTask(), 0, 1000);
		}
	}

	@Override
	protected void onSaveInstanceState(@NonNull Bundle state) {
		state.putSerializable(PUZZLE_KEY, puzzle);
		if (!puzzle.isSolved()) state.putLong(TIME_KEY, solveStartTime);
		super.onSaveInstanceState(state);
	}

	void onNewGameClick(View view) {
		setSolveTime(0);

		newGameButton.setVisibility(View.INVISIBLE);
		solveButton.setVisibility(View.VISIBLE);
		resetButton.setVisibility(View.VISIBLE);
		puzzleHelper.shuffle();

		if (timeUpdater != null) timeUpdater.cancel();
		timeUpdater = new Timer();
		solveStartTime = new Date().getTime();
		timeUpdater.schedule(new TimeUpdaterTask(), 1000, 1000);
	}

	void onResetClick(View view) {
		puzzleHelper.reset();
		onPuzzleSolved();
		setSolveTime(0);
	}

	void onSolveClick(View view) {
		if(!puzzleHelper.isSolved()){
			solveButton.setEnabled(false);

			speedBar.setProgress(maxSolveSpeed - defaultSolveSpeed);
			speedLayout.setVisibility(View.VISIBLE);
			puzzleHelper.setSolveDelay(defaultSolveSpeed);
			puzzleHelper.solve();
		}
	}

	@Override
	protected void onDestroy() {
		puzzleHelper.stopSolvingThread();
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	private void setPuzzleSize(@IntRange(from=3, to=10) int newPuzzleSize) {
		onResetClick(null);
		puzzleHelper.setPuzzleSize(newPuzzleSize);
	}

	@Override
	@SuppressLint("NonConstantResourceId")
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		switch (item.getItemId()){
			case R.id._3x3:
				setPuzzleSize(3);
				break;
			case R.id._4x4:
				setPuzzleSize(4);
				break;
			case R.id._5x5:
				setPuzzleSize(5);
				break;
			case R.id._6x6:
				setPuzzleSize(6);
				break;
			case R.id._7x7:
				setPuzzleSize(7);
				break;
			case R.id._8x8:
				setPuzzleSize(8);
				break;
			case R.id._9x9:
				setPuzzleSize(9);
				break;
			case R.id._10x10:
				setPuzzleSize(10);
				break;
			default: break;
		}

		return super.onOptionsItemSelected(item);
	}

	void onPuzzleSolved() {
		if (timeUpdater != null) {
			timeUpdater.cancel();
			timeUpdater = null;
		}

		solveStartTime = 0;
		speedLayout.setVisibility(View.INVISIBLE);
		solveButton.setEnabled(true);

		solveButton.setVisibility(View.INVISIBLE);
		resetButton.setVisibility(View.INVISIBLE);
		newGameButton.setVisibility(View.VISIBLE);
	}

	@SuppressLint("DefaultLocale")
	void setSolveTime(long timeInSeconds) {
		long minutes = timeInSeconds / 60;
		long seconds = timeInSeconds % 60;
		timeView.setText(String.format("%02d:%02d", minutes, seconds));
	}
}