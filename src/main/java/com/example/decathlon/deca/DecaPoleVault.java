package com.example.decathlon.deca;

import com.example.decathlon.common.CalcTrackAndField;

public class DecaPoleVault {

	private int score;
	private double A = 0.2797;
	private double B = 100;
	private double C = 1.35;
	CalcTrackAndField calc = new CalcTrackAndField();

	public int calculateResult(double distance) {
		if (distance < 2 || distance > 1000) {
			throw new IllegalArgumentException("Enter a Pole Vault distance between 2 and 1000 centimeters.");
		}
		score = calc.calculateField(A, B, C, distance);
		return score;
	}

}
