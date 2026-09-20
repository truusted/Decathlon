package com.example.decathlon.deca;

import com.example.decathlon.common.CalcTrackAndField;

public class DecaShotPut {

	private int score;
	private double A = 51.39;
	private double B = 1.5;
	private double C = 1.05;
	CalcTrackAndField calc = new CalcTrackAndField();

	public int calculateResult(double distance) {
		if (distance <= 0 || distance > 30) {
			throw new IllegalArgumentException("Enter a Shot Put distance between 1 and 30 meters.");
		}
		score = calc.calculateField(A, B, C, distance);
		return score;
	}

}
