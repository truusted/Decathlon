package com.example.decathlon.deca;

import com.example.decathlon.common.CalcTrackAndField;

public class DecaHighJump {

	private int score;
	private double A = 0.8465;
	private double B = 75;
	private double C = 1.42;
	CalcTrackAndField calc = new CalcTrackAndField();

	public int calculateResult(double distance) {
		if (distance <= 0 || distance > 100) {
			throw new IllegalArgumentException("Enter a High Jump distance between 1 and 100 centimeters.");
		}
		score = calc.calculateField(A, B, C, distance);
		return score;
	}

}
