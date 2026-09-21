package com.example.decathlon.deca;

import com.example.decathlon.common.CalcTrackAndField;

public class Deca400M {

	private int score;
	private double A = 1.53775;
	private double B = 82;
	private double C = 1.81;
	CalcTrackAndField calc = new CalcTrackAndField();

	public int calculateResult(double runningTime) {
		if (runningTime < 20 || runningTime > 100) {
			throw new IllegalArgumentException("Enter a 400m time between 20 and 100 seconds.");
		}
		score = calc.calculateTrack(A, B, C, runningTime);
		return score;
	}

}
