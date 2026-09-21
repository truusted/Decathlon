package com.example.decathlon.deca;

import com.example.decathlon.common.CalcTrackAndField;

public class Deca1500M {

	private int score;
	private double A = 0.03768;
	private double B = 480;
	private double C = 18.5;
	CalcTrackAndField calc = new CalcTrackAndField();

	public int calculateResult(double runningTime) {
		if (runningTime < 2 || runningTime > 7) {
			throw new IllegalArgumentException("Enter a 1500m time between 2 and 7 (minutes.seconds).");
		}
		score = calc.calculateTrack(A, B, C, runningTime);
		return score;
	}

}
