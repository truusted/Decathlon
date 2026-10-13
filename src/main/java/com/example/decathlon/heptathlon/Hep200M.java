package com.example.decathlon.heptathlon;

import com.example.decathlon.common.CalcTrackAndField;

public class Hep200M {

	private int score;
	private double A = 4.99087;
	private double B = 42.5;
	private double C = 1.81;
	CalcTrackAndField calc = new CalcTrackAndField();

	public int calculateResult(double runningTime) {
		if (runningTime < 14 || runningTime > 42.08) {
			throw new IllegalArgumentException("Enter a 200m time between 14 and 42.08 seconds.");
		}
		score = calc.calculateTrack(A, B, C, runningTime);
		return score;
	}

}