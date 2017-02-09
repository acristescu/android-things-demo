package com.example.remote;

/**
 * Created by acristescu on 01/02/2017.
 */

public class Device {
	private Status currentStatus;
	private Status desiredStatus;

	public Status getDesiredStatus() {
		return desiredStatus;
	}

	public void setDesiredStatus(Status desiredStatus) {
		this.desiredStatus = desiredStatus;
	}

	public Status getCurrentStatus() {
		return currentStatus;
	}

	public void setCurrentStatus(Status currentStatus) {
		this.currentStatus = currentStatus;
	}
}
