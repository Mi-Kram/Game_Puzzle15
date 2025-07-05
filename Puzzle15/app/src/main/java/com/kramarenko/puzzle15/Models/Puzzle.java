package com.kramarenko.puzzle15.Models;

import androidx.annotation.IntRange;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class Puzzle implements Serializable {

	@IntRange(from = 3, to = 10) protected int size;
	protected int[][] puzzle; // real puzzle
	protected int[][] puzzleLock; // puzzle for wave algorithm
	protected ArrayList<Point> moves; // steps to solve puzzle
	protected final Point zeroPoint = new Point(0, 0);


	public Puzzle(@IntRange(from = 3, to = 10) int size) {
		this.size = size;
		moves = new ArrayList<>();
		newPuzzle();
	}

	public Point getZeroPoint(){
		return new Point(zeroPoint);
	}

	public boolean simpleMove(Point point){
		if(!point.isNear(zeroPoint)) return false;

		set(zeroPoint, get(point));
		set(point, 0);
		zeroPoint.set(point);

		return true;
	}

	public void setSize(@IntRange(from = 3, to = 10) int size){
		this.size = size;
		newPuzzle();
	}

	public boolean isSolved(){
		int cnt = 0;
		for (int i = 0; i < size; i++)
			for (int j = 0; j < size; j++)
				if(get(i, j) != ++cnt && cnt != size*size)
					return false;

		return true;
	}

	public int getSize(){
		return size;
	}

	public void set(Point point, int value){
		puzzle[point.row][point.col] = value;
	}

	public int get(Point point){
		return puzzle[point.row][point.col];
	}

	public int get(int row, int col){
		return puzzle[row][col];
	}

	protected void lock(Point point){
		puzzleLock[point.row][point.col] = -1;
	}

	protected void lock(int row, int col){
		puzzleLock[row][col] = -1;
	}

	protected void unlock(Point point){
		puzzleLock[point.row][point.col] = 0;
	}

	protected void unlock(int row, int col){
		puzzleLock[row][col] = 0;
	}

	public void newPuzzle(){
		moves.clear();
		puzzle = new int[size][];
		puzzleLock = new int[size][];

		for (int i = 0; i < size; i++) {
			int[] puzzleRow = new int[size];
			int[] puzzleLockRow = new int[size];

			for(int j = 0; j < size; j++){
				puzzleRow[j] = i*size+j+1;
				puzzleLockRow[j] = 0;
			}

			puzzle[i] = puzzleRow;
			puzzleLock[i] = puzzleLockRow;
		}

		zeroPoint.set(size-1, size-1);
		set(zeroPoint, 0);
	}

	public void shuffle(){
		ThreadLocalRandom rand = ThreadLocalRandom.current();
		int maxNum = size*size;

		int n = size*size*30;
		for (int i = 0; i < n; i++) {
			int index = rand.nextInt(0, maxNum);
			Point point = getPoint(index);

			if(zeroPoint.equals(point) || (!zeroPoint.equalsRows(point) && !zeroPoint.equalsCols(point))){
				i--;
				continue;
			}

			if(zeroPoint.equalsRows(point)){
				int delta = zeroPoint.col > point.col ? -1 : 1;
				for ( ; !zeroPoint.equalsCols(point); zeroPoint.col += delta) {
					puzzle[point.row][zeroPoint.col] = puzzle[point.row][zeroPoint.col+delta];
				}
			}
			else if(zeroPoint.equalsCols(point)){
				int delta = zeroPoint.row > point.row ? -1 : 1;
				for ( ; !zeroPoint.equalsRows(point); zeroPoint.row += delta) {
					puzzle[zeroPoint.row][point.col] = puzzle[zeroPoint.row+delta][point.col];
				}
			}
		}
		set(zeroPoint, 0);
	}

	public Point getPoint(int index){
		int row = index / size;
		int col = index % size;
		return new Point(row, col);
	}

	public Point getNumPoint(int num){
		for (int i = 0; i < size; i++)
			for (int j = 0; j < size; j++)
				if(puzzle[i][j] == num)
					return new Point(i, j);

		return null;
	}

	public void solve() throws UncorrectedPuzzleException {
		moves.clear();
		Point startEmptyPosition = new Point(zeroPoint);

		for (int row = 0; row < size-2; row++) {
			simpleWay(row*size+row+1);
			lock(row, row);

			for (int col = row+1; col < size-2; col++){
				simpleWay(row*size+col+1);
				lock(row, col);
			}
			setRight(row);
			lock(row, size-2);
			lock(row, size-1);

			for (int col = row+1; col < size-2; col++) {
				simpleWay(col*size+row+1);
				lock(col, row);
			}
			setBottom(row);
			lock(size-1, row);
			lock(size-2, row);
		}

		setLastThree();
		removeRepeatMoves(startEmptyPosition);
	}

	private void removeRepeatMoves(Point empty) {
		Point prev = null, prev2 = null;

		for (int i = 0; i < moves.size(); i++) {
			if (i == 0) {
				prev2 = empty;
				prev = moves.get(0);
				continue;
			}

			if (prev2.equals(moves.get(i))) {
				moves.remove(i--);
				moves.remove(i--);

				if (i <= 0) i = -1;
				else {
					prev = prev2;
					prev2 = moves.get(i - 1);
				}

				continue;
			}

			prev2 = prev;
			prev = moves.get(i);
		}
	}

	protected void move(ArrayList<Point> points) {
		if(points.size() == 0) return;
		for (Point point : points) {
			moves.add(point);
			set(zeroPoint, get(point));
			zeroPoint.set(point);
		}
		set(zeroPoint, 0);
	}

	protected void move(Point point) {
		moves.add(point);
		set(zeroPoint, get(point));
		zeroPoint.set(point);
		set(zeroPoint, 0);
	}

	protected ArrayList<Point> getWay(Point fromPoint, Point destPoint) {
		if(fromPoint.equals(destPoint)) return new ArrayList<>();

		int[][] matrix = new int[size][];
		for (int i = 0; i < size; i++)
			matrix[i] = puzzleLock[i].clone();

		int curValue = 1;
		matrix[destPoint.row][destPoint.col] = curValue;

		ArrayList<Point> curPos = new ArrayList<>();
		curPos.add(destPoint);
		ArrayList<Point> nextPos = new ArrayList<>();

		while (curPos.size() > 0) {
			curValue += 1;

			for (Point point : curPos) {
				if(point.row>0 && matrix[point.row-1][point.col] == 0){
					nextPos.add(point.create(-1, 0));
					matrix[point.row-1][point.col] = curValue;
				}
				if(point.row+1<size && matrix[point.row+1][point.col] == 0){
					nextPos.add(point.create(1, 0));
					matrix[point.row+1][point.col] = curValue;
				}
				if(point.col>0 && matrix[point.row][point.col-1] == 0){
					nextPos.add(point.create(0, -1));
					matrix[point.row][point.col-1] = curValue;
				}
				if(point.col+1<size && matrix[point.row][point.col+1] == 0){
					nextPos.add(point.create(0, 1));
					matrix[point.row][point.col+1] = curValue;
				}
			}

			for (Point point : nextPos) {
				if(point.equals(fromPoint)){
					ArrayList<Point> result = new ArrayList<>();
					int value = curValue;

					while(value > 1){
						if(point.row>0 && matrix[point.row-1][point.col] == value-1)
							value = matrix[--point.row][point.col];
						else if(point.row+1<size && matrix[point.row+1][point.col] == value-1)
							value = matrix[++point.row][point.col];
						else if(point.col>0 && matrix[point.row][point.col-1] == value-1)
							value = matrix[point.row][--point.col];
						else if(point.col+1<size && matrix[point.row][point.col+1] == value-1)
							value = matrix[point.row][++point.col];
						else value = 0;

						result.add(new Point(point));
					}

					return result;
				}
			}

			curPos = nextPos;
			nextPos = new ArrayList<>();
		}
		return null;
	}

	protected void moveItem(int num, Point finalPoint) throws UncorrectedPuzzleException {
		Point numPoint = getNumPoint(num);
		if(numPoint.equals(finalPoint)) return;

		ArrayList<Point> numWay = getWay(numPoint, finalPoint);
		if(numWay == null) throw new UncorrectedPuzzleException();

		for (Point nPoint : numWay) {
			if(!zeroPoint.equals(nPoint)){
				lock(numPoint);
				ArrayList<Point> zeroWay = getWay(zeroPoint, nPoint);
				unlock(numPoint);
				if(zeroWay == null) throw new UncorrectedPuzzleException();

				move(zeroWay);
			}

			move(numPoint);
			numPoint = nPoint;
		}

		set(zeroPoint, 0);
	}

	protected void simpleWay(int num) throws UncorrectedPuzzleException {
		Point finalPoint = getPoint(num-1);
		moveItem(num, finalPoint);
	}

	protected void setRight(int rowIndex) throws UncorrectedPuzzleException {
		rightFirst(size*rowIndex+size-1);
		rightSecond(size*rowIndex+size);
	}

	protected void rightFirst(int num) throws UncorrectedPuzzleException {
		Point finalPoint = getPoint(num);
		moveItem(num, finalPoint);
	}

	protected void rightSecond(int num) throws UncorrectedPuzzleException {
		Point finalPoint = getPoint(num-1);
		Point numPoint = getNumPoint(num);

		boolean fail = numPoint.equals(finalPoint.row, finalPoint.col-1);
		if(fail){
			lock(finalPoint);
			moveItem(num, finalPoint.create(1, -1));
			unlock(finalPoint);
			numPoint = getNumPoint(num);
		}

		fail = numPoint.equals(finalPoint.row+1, finalPoint.col-1) &&
				zeroPoint.equals(finalPoint.row, finalPoint.col-1);

		if(fail){
			move(finalPoint.create(0, 0));
			move(finalPoint.create(1, 0));
			move(finalPoint.create(1, -1));
			move(finalPoint.create(2, -1));
			move(finalPoint.create(2, 0));
			move(finalPoint.create(1, 0));
			move(finalPoint.create(0, 0));
			move(finalPoint.create(0, -1));
		}

		lock(finalPoint);
		moveItem(num, finalPoint.create(1, 0));
		unlock(finalPoint);

		lock(finalPoint.row+1, finalPoint.col);
		moveItem(num-1, finalPoint.create(0, -1));
		unlock(finalPoint.row+1, finalPoint.col);

		moveItem(num, finalPoint);

		set(zeroPoint, 0);
	}

	protected void setBottom(int colIndex) throws UncorrectedPuzzleException {
		bottomFirst(size*(size-2)+colIndex+1);
		bottomSecond(size*(size-1)+colIndex+1);
	}

	protected void bottomFirst(int num) throws UncorrectedPuzzleException {
		Point finalPoint = getPoint(num+size-1);
		moveItem(num, finalPoint);
	}

	protected void bottomSecond(int num) throws UncorrectedPuzzleException {
		Point finalPoint = getPoint(num-1);
		Point numPoint = getNumPoint(num);

		boolean fail = numPoint.equals(finalPoint.row-1, finalPoint.col);
		if(fail){
			lock(finalPoint);
			moveItem(num, finalPoint.create(-1, 1));
			unlock(finalPoint);
			numPoint = getNumPoint(num);
		}

		fail = numPoint.equals(finalPoint.row-1, finalPoint.col+1) &&
				zeroPoint.equals(finalPoint.row-1, finalPoint.col);

		if(fail){
			move(finalPoint.create(0, 0));
			move(finalPoint.create(0, 1));
			move(finalPoint.create(-1, 1));
			move(finalPoint.create(-1, 2));
			move(finalPoint.create(0, 2));
			move(finalPoint.create(0, 1));
			move(finalPoint.create(0, 0));
			move(finalPoint.create(-1, 0));
		}

		lock(finalPoint);
		moveItem(num, finalPoint.create(0, 1));
		unlock(finalPoint);

		lock(finalPoint.row, finalPoint.col+1);
		moveItem(num-size, finalPoint.create(-1, 0));
		unlock(finalPoint.row, finalPoint.col+1);

		moveItem(num, finalPoint);

		set(zeroPoint, 0);
	}

	protected void setLastThree() throws UncorrectedPuzzleException {
		int minNum = size*size-size-1;
		int minNextNum = size*size-size;
		int lastNum = size*size-1;

		Point lastFinalPoint = getPoint(size*size-2);
		Point minFinalPoint = lastFinalPoint.create(-1, 0);
		Point minNextFinalPoint = minFinalPoint.create(0, 1);

		Point minPoint = getNumPoint(minNum);
		Point minNextPoint = getNumPoint(minNextNum);
		Point lastPoint = getNumPoint(lastNum);

		if(minFinalPoint.equals(minPoint)) {
			lock(minFinalPoint);
			setLastTwo(minNextNum, lastNum);
			return;
		}

		if(minNextFinalPoint.equals(minNextPoint)) {
			lock(minNextFinalPoint);
			setLastTwo(minNum, lastNum);
			return;
		}

		if(lastFinalPoint.equals(lastPoint)) {
			lock(lastFinalPoint);
			setLastTwo(minNum, minNextNum);
			return;
		}

		if(zeroPoint.equals(size-1, size-1)) {
			if(zeroPoint.isNear(lastPoint)){
				move(zeroPoint.create(-1, 0));
				move(zeroPoint.create(0, -1));
				move(zeroPoint.create(1, 0));
				move(zeroPoint.create(0, 1));
			}
			else{
				move(zeroPoint.create(0, -1));
				move(zeroPoint.create(-1, 0));
				move(zeroPoint.create(0, 1));
				move(zeroPoint.create(1, 0));
			}
		}
		else if(zeroPoint.equals(size-2, size-1)){
			if(zeroPoint.isNear(minNextPoint)){
				move(zeroPoint.create(0, -1));
				move(zeroPoint.create(1, 0));
				move(zeroPoint.create(0, 1));
			}
			else {
				move(zeroPoint.create(1, 0));
				move(zeroPoint.create(0, -1));
				move(zeroPoint.create(-1, 0));
				move(zeroPoint.create(0, 1));
				move(zeroPoint.create(1, 0));
			}
		}
		else if(zeroPoint.equals(size-2, size-2)){
			throw new UncorrectedPuzzleException();
		}
		else if(zeroPoint.equals(size-1, size-2)){
			if(zeroPoint.isNear(lastPoint)){
				move(zeroPoint.create(-1, 0));
				move(zeroPoint.create(0, 1));
				move(zeroPoint.create(1, 0));
			}
			else {
				move(zeroPoint.create(0, 1));
				move(zeroPoint.create(-1, 0));
				move(zeroPoint.create(0, -1));
				move(zeroPoint.create(1, 0));
				move(zeroPoint.create(0, 1));
			}
		}

		minPoint = getNumPoint(minNum);
		minNextPoint = getNumPoint(minNextNum);
		lastPoint = getNumPoint(lastNum);

		if(!minFinalPoint.equals(minPoint) ||
				!minNextFinalPoint.equals(minNextPoint) ||
				!lastFinalPoint.equals(lastPoint))
			throw new UncorrectedPuzzleException();

		lock(minFinalPoint);
		lock(minNextFinalPoint);
		lock(lastFinalPoint);
	}

	protected void setLastTwo(int num1, int num2) throws UncorrectedPuzzleException {
		Point num1FinalPoint = getPoint(num1-1);
		Point num2FinalPoint = getPoint(num2-1);

		Point num1Point = getNumPoint(num1);
		Point num2Point = getNumPoint(num2);

		if(num1FinalPoint.equals(num1Point) && num2FinalPoint.equals(num2Point)){
			lock(num1FinalPoint);
			lock(num2FinalPoint);
			return;
		}

		if(num1FinalPoint.equals(num1Point)){
			lock(num1FinalPoint);
			moveItem(num2, num2FinalPoint);
			lock(num2FinalPoint);
			return;
		}

		if(num2FinalPoint.equals(num2Point)){
			lock(num2FinalPoint);
			moveItem(num1, num1FinalPoint);
			lock(num1FinalPoint);
			return;
		}

		if(num1Point.isNear(zeroPoint)){
			moveItem(num1, num1FinalPoint);
			lock(num1FinalPoint);
			moveItem(num2, num2FinalPoint);
			lock(num2FinalPoint);
			return;
		}

		if(num2Point.isNear(zeroPoint)){
			moveItem(num2, num2FinalPoint);
			lock(num2FinalPoint);
			moveItem(num1, num1FinalPoint);
			lock(num1FinalPoint);
			return;
		}

		throw new UncorrectedPuzzleException();
	}

}
