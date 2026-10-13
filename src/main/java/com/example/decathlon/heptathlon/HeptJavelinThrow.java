package com.example.decathlon.heptathlon;

import com.example.decathlon.common.CalcTrackAndField;

public class HeptJavelinThrow {

	private int score;
	private double A = 15.9803;
	private double B = 3.8;
	private double C = 1.04;
	CalcTrackAndField calc = new CalcTrackAndField();

	public int calculateResult(double distance) {
		if (distance <= 0 || distance > 100) {
			throw new IllegalArgumentException("Enter a Javelin Throw distance between 1 and 100 meters.");
		}
		score = calc.calculateField(A, B, C, distance);
		return score;
	}

}