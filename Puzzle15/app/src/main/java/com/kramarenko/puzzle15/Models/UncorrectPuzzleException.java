package com.kramarenko.puzzle15.Models;

class UncorrectedPuzzleException extends Exception {

	@Override
	public String getMessage() {
		return "Uncorrected puzzle!";
	}

}
