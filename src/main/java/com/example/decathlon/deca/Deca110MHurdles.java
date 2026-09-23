package com.example.decathlon.deca;

import com.example.decathlon.common.CalcTrackAndField;

public class Deca110MHurdles {

	private int score;
	private double A = 5.74352;
	private double B = 28.5;
	private double C = 1.92;
	CalcTrackAndField calc = new CalcTrackAndField();

	public int calculateResult(double runningTime) {
		if (runningTime < 10 || runningTime > 28.5) {
			throw new IllegalArgumentException("Enter a 110m Hurdles time between 10 and 28.5 seconds.");
		}
		score = calc.calculateTrack(A, B, C, runningTime);
		return score;
	}

}
