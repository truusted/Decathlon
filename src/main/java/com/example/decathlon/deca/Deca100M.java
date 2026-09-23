package com.example.decathlon.deca;

import com.example.decathlon.common.CalcTrackAndField;

public class Deca100M {

	private int score;
	private double A = 25.4347;
	private double B = 18;
	private double C = 1.81;
	CalcTrackAndField calc = new CalcTrackAndField();

	public int calculateResult(double runningTime) {
		if (runningTime < 5 || runningTime > 17.8) {
			throw new IllegalArgumentException("Enter a 100m time between 5 and 17.8 seconds.");
		}
		score = calc.calculateTrack(A, B, C, runningTime);
		return score;
	}

}
