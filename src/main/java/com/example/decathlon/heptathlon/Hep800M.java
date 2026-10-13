package com.example.decathlon.heptathlon;

import com.example.decathlon.common.CalcTrackAndField;

public class Hep800M {

	private int score;
	private double A = 0.11193;
	private double B = 254;
	private double C = 1.88;
	CalcTrackAndField calc = new CalcTrackAndField();

	public int calculateResult(double runningTime) {
		if (runningTime < 70 || runningTime > 250.79) {
			throw new IllegalArgumentException("Enter an 800m time between 70 and 250.79 seconds.");
		}
		score = calc.calculateTrack(A, B, C, runningTime);
		return score;
	}

}