package com.example.decathlon.deca;

import com.example.decathlon.common.CalcTrackAndField;

public class DecaDiscusThrow {

	private int score;
	private double A = 12.91;
	private double B = 4;
	private double C = 1.1;
	CalcTrackAndField calc = new CalcTrackAndField();

	public int calculateResult(double distance) {
		if (distance <= 0 || distance > 85) {
			throw new IllegalArgumentException("Enter a Discus Throw distance between 1 and 85 meters.");
		}
		score = calc.calculateField(A, B, C, distance);
		return score;
	}

}
