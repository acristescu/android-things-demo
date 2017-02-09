package com.example.thingspoc;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManagerService;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by acristescu on 31/01/2017.
 */
public class HomeActivity extends Activity {
	private static final String TAG = "HomeActivity";
	private static final String GPIO_PIN_NAME = "BCM4";
	private static final String UUID_KEY = "_UUID";
	private static final String PREFS_NAME = "MyPrefs";

	private Gpio mLedGpio;
	private Status mStatus = new Status();
	private DatabaseReference mCurrentStatusRef;
	private DatabaseReference mDesiredStatusRef;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		PeripheralManagerService service = new PeripheralManagerService();

		try {
			mLedGpio = service.openGpio(GPIO_PIN_NAME);
			mLedGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
		} catch (IOException e) {
			Log.e(TAG, "Error on PeripheralIO API", e);
		}
		FirebaseDatabase database = FirebaseDatabase.getInstance();
		mCurrentStatusRef = database.getReference(getDeviceId()).child("currentStatus");

		mStatus.setLedOn(getLed());
		mCurrentStatusRef.setValue(mStatus);

		mDesiredStatusRef = database.getReference(getDeviceId()).child("desiredStatus");
		mDesiredStatusRef.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(DataSnapshot dataSnapshot) {
				if(dataSnapshot.getValue() == null) {
					return;
				}
				mDesiredStatusRef.removeValue();
				handleNewState(dataSnapshot.getValue(Status.class));
			}

			@Override
			public void onCancelled(DatabaseError databaseError) {
				Log.e(TAG, "Error on Firebase read", databaseError.toException());
			}
		});
	}

	private String getDeviceId() {
		SharedPreferences prefs = getSharedPreferences(PREFS_NAME, 0);
		if(!prefs.contains(UUID_KEY)) {
			prefs.edit().putString(UUID_KEY, UUID.randomUUID().toString()).apply();
		}
		return prefs.getString(UUID_KEY, UUID.randomUUID().toString());
	}

	private void handleNewState(Status desiredStatus) {
		setLed(desiredStatus.isLedOn());
	}

	private void setLed(boolean newState) {
		try {
			mLedGpio.setValue(newState);
		} catch (IOException e) {
			Log.e(TAG, "Error on PeripheralIO API", e);
		}
		mStatus.setLedOn(newState);
		mCurrentStatusRef.setValue(mStatus);
	}

	private boolean getLed() {
		try {
			return mLedGpio.getValue();
		} catch (IOException e) {
			Log.e(TAG, "Error on PeripheralIO API", e);
		}
		return false;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (mLedGpio != null) {
			try {
				mLedGpio.close();
			} catch (IOException e) {
				Log.e(TAG, "Error on PeripheralIO API", e);
			}
		}
	}
}
