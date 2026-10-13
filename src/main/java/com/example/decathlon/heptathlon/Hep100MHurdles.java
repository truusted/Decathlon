package com.example.decathlon.heptathlon;

import com.example.decathlon.common.CalcTrackAndField;

public class Hep100MHurdles {

	private int score;
	private double A = 9.23076;
	private double B = 26.7;
	private double C = 1.835;
	CalcTrackAndField calc = new CalcTrackAndField();

	public int calculateResult(double runningTime) {
		if (runningTime < 5 || runningTime > 26.4) {
			throw new IllegalArgumentException("Enter a 100m Hurdles time between 5 and 26.4 seconds.");
		}
		score = calc.calculateTrack(A, B, C, runningTime);
		return score;
	}

}