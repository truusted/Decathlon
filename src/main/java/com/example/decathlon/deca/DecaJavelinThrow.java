package com.example.decathlon.deca;

import com.example.decathlon.common.CalcTrackAndField;

public class DecaJavelinThrow {

	private int score;
	private double A = 10.14;
	private double B = 7;
	private double C = 1.08;
	CalcTrackAndField calc = new CalcTrackAndField();

	public int calculateResult(double distance) {
		if (distance <= 0 || distance > 110) {
			throw new IllegalArgumentException("Enter a Javelin Throw distance between 1 and 110 meters.");
		}
		score = calc.calculateField(A, B, C, distance);
		return score;
	}

}
