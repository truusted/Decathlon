package com.example.decathlon.heptathlon;

import com.example.decathlon.common.CalcTrackAndField;

public class HeptShotPut {

	private int score;
	private double A = 56.0211;
	private double B = 1.5;
	private double C = 1.05;
	CalcTrackAndField calc = new CalcTrackAndField();

	public int calculateResult(double distance) {
		if (distance < 5 || distance > 100) {
			throw new IllegalArgumentException("Enter a Shot Put distance between 5 and 100 meters.");
		}
		score = calc.calculateField(A, B, C, distance);
		return score;
	}

}