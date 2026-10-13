package com.example.decathlon.heptathlon;

import com.example.decathlon.common.CalcTrackAndField;

public class HeptLongJump {

	private int score;
	private double A = 0.188807;
	private double B = 210;
	private double C = 1.41;
	CalcTrackAndField calc = new CalcTrackAndField();

	public int calculateResult(double distance) {
		if (distance <= 0 || distance > 1000) {
			throw new IllegalArgumentException("Enter a Long Jump distance between 1 and 1000 centimeters.");
		}
		score = calc.calculateField(A, B, C, distance);
		return score;
	}

}