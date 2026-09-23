package com.example.decathlon.deca;

import com.example.decathlon.common.CalcTrackAndField;

public class DecaLongJump {

	private int score;
	private double A = 0.13454;
	private double B = 220;
	private double C = 1.4;
	CalcTrackAndField calc = new CalcTrackAndField();

	public int calculateResult(double distance) {
		if (distance < 250 || distance > 1000) {
			throw new IllegalArgumentException("Enter a Long Jump distance between 250 and 1000 centimeters.");
		}
		score = calc.calculateField(A, B, C, distance);
		return score;
	}

}
