# Android Things PoC
This is a proof-of-concept app showcasing the capabilities of the [Android Things](https://developer.android.com/things/index.html)
platform.

[TOC]

## What is the _Android Things_ platform?
The Android Things platform is an operating system from Google intended to be used on IoT devices.
It is in essence a stripped-down version of Android that can run on a variety of platforms (such as
`Raspberry Pi 3` or `Intel Edison`). This is the second attempt from Google to propose such a system,
the first (largely failed) being launched at the end of 2015 under the name of Brillo.

It is targeted towards more powerful IoT devices, offering the ability to integrate a variety of
powerful Android libraries and services. It allows developers to focus more on the application part
of the stack and not on building custom kernels for their hardware.

One of the selling points of the platform is the ability to develop apps for `Android Things` using
the same toolchain and libraries as for Android phone apps. Android developers will feel right at home
leveraging the now quite mature `Android Studio` IDE and robust libraries such as `Retrofit2` or
`Firebase Database`.

Another selling point is the promise that updates to the platform can be pushed over-the-air through
Google's infrastructure.

### I/O APIs
In addition to the normal Android API, the `Android Things` offers a few APIs that are aimed at communicating
with custom hardware that may be present:

**Peripheral I/O API**

The Peripheral I/O APIs let your apps communicate with sensors and actuators using industry standard protocols and interfaces. The following interfaces are supported: GPIO, PWM, I2C, SPI, UART.

See the official [Peripheral I/O API Guides](https://developer.android.com/things/sdk/pio/index.html) for more information on how to use the APIs.

**User Driver API**

User drivers extend existing Android framework services and allow apps to inject hardware events into the framework that other apps can access using the standard Android APIs.

See the [User Driver API Guides](https://developer.android.com/things/sdk/drivers/index.html) for more information on how to use the APIs.

### Missing APIs
While most of the normal Android API is there, there are a few things that are missing:

* [Common intents](https://developer.android.com/guide/components/intents-common.html) are not supported
* 'Content' APIs are not supported:

    * CalendarContract
    * ContactsContract
    * DocumentsContract
    * DownloadManager
    * MediaStore
    * Settings
    * Telephony
    * UserDictionary
    * VoicemailContract

* Displays are optional. Although you can create UIs using the exact same APIs that you use for phones,
a display is no longer _required_.
* Notifications are not supported.
* Permissions are always granted without any user input.
* Only a subset of the Google Services are supported. As a general rule, APIs that require user input
or authentication credentials aren't available to apps. The following table breaks down API support
in Android Things:

| Supported APIs                    | Unavailable APIs |
|-----------------------------------|------------------|
| Cast                              | AdMob
| Drive                             | Android Pay
| Firebase Analytics                | Firebase App Indexing
| Firebase Cloud Messaging (FCM)    | Firebase Authentication
| Firebase Crash Reporting          | Firebase Dynamic Links
| Firebase Realtime Database        | Firebase Invites
| Firebase Remote Config            | Firebase Notifications
| Firebase Storage                  | Maps
| Fit                               | Play Games
| Instance ID                       | Search
| Location                          | Sign-In
| Nearby
| Places
| Mobile Vision

### Supported Hardware
At the time of this writing (Feb 2017), 3 platforms are currently supported, with two more announced:

* [Intel® Edison](https://software.intel.com/en-us/iot/android-things)
* [NXP Pico i.MX6UL](http://www.nxp.com/AndroidThingsGS)
* [Raspberry Pi 3](https://www.raspberrypi.org/products/raspberry-pi-3-model-b/)
* Intel® Joule™ 570x _(announced)_
* NXP Argon i.MX6UL _(announced)_

## This project
This project is a proof-of-concept that aims to establish two-way-communications between one or
more IoT devices and the cloud. The device will be able to publish its state and communicate state
changes. A remote control app (which is a simple Android phone app) can be used to read the state of
all the devices in the fleet as well as change the state of any of them. For the sake of speed of
development, `Firebase` will be used to store the state of the devices, as well as push status changes
to them.

### The hardware

* Raspberry Pi 3 (with USB cable and charger)
* Micro SD card (with adapter)
* Ethernet patch cable (note: required until WiFi is setup)
* 1 LED
* Monitor and HDMI cable (optional, but quite useful)

### Installing Android things
The steps below are detailed in the official [guide](https://developer.android.com/things/hardware/raspberrypi.html).
This is just a summary:

1. Download the Raspberry .img file from this link [https://developer.android.com/things/preview/download.html]().
Unzip it using [The Unarchiver](http://unarchiver.c3.cx/unarchiver) (the Mac doesn't like the archive
format)
2. Follow [the official Raspberry guide](https://www.raspberrypi.org/documentation/installation/installing-images/mac.md)
to install the image onto the SD card. The first method worked just fine.
3. Insert the SD card into the appropriate slot. Connect the Ethernet cable, HDMI cable and lastly the
power to the Raspberry Pi.
4. Connect the LED between pins 6 and 7 on the board, making sure the correct LED wire connects to `Ground`.
Refer to this diagram: ![](https://developer.android.com/things/images/pinout-raspberrypi.png)
> **Note:** If you're having trouble determining which LED connect is the `Ground` one, first connect
it between pins 1 and 6. If the LED does not light up, then switch the connections between them. Once
the LED lights up, just move the connection from pin 1 to pin 7.
5. Use the monitor to determine when the device has booted up. You should se the device's IP address.
Make note of it.
> **Note:** If not using the monitor, you can try using `Android.local` instead of the IP address for the
 next commands (might work, depending on your network configuration). If that doesn't work, you need
 a monitor.
6. Connect `adb` to the device with the command:
```sh
$ adb connect <ip-address>
connected to <ip-address>:5555
```

You are now ready to go, however, you may wish to setup WiFi so that you are not limited by the
Ethernet cable. Please note I was not able to connect to my work network that required both a username
and a password, but my home network (that only requires a passphrase worked just fine). To connect to
WiFi:

1. Send an intent to the Wi-Fi service that includes the SSID and passcode of your local network:

        #!sh
        $ adb shell am startservice \
            -n com.google.wifisetup/.WifiSetupService \
            -a WifiSetupService.Connect \
            -e ssid <Network_SSID> \
            -e passphrase <Network_Passcode>
> **Note:** You can remove the passphrase argument if your network doesn't require a passcode.

2. Verify that the connection was successful through logcat:
        
        #!sh
        $ adb logcat -d | grep Wifi
        ...
        V WifiWatcher: Network state changed to CONNECTED
        V WifiWatcher: SSID changed: ...
        I WifiConfigurator: Successfully connected to ...

3. Test that you can access a remote IP address:
        
        #!sh
        $ adb shell ping 8.8.8.8
        PING 8.8.8.8 (8.8.8.8) 56(84) bytes of data.
        64 bytes from 8.8.8.8: icmp_seq=1 ttl=57 time=6.67 ms
        64 bytes from 8.8.8.8: icmp_seq=2 ttl=57 time=55.5 ms
        64 bytes from 8.8.8.8: icmp_seq=3 ttl=57 time=23.0 ms
        64 bytes from 8.8.8.8: icmp_seq=4 ttl=57 time=245 ms

### The software
Things apps use the same structure as those designed for phones and tablets. You will need to have
the following installed:

* Android Studio
* Android SDK 7 (API 24) or newer
* Android tools 24 or newer

### Creating the project
In android studio, choose `File -> New -> Project`, name it and give it a base package. Choose _phone
and tablet_ as your platform and make sure you target API 24 or newer. Don't auto generate any
activity.

Then, go into [build.gradle](/app/build.gradle) and instruct Android to expect the Things API to be
present on the device:
```groovy
dependencies {
    ...
    provided 'com.google.android.things:androidthings:0.1-devpreview'
}
```
> **Note:** _provided_ means the library is present on the device and should not be compiled into the apk.

Next, add the things shared library entry to your app's manifest file:
```xml
<application ...>
  <uses-library android:name="com.google.android.things"/>
  ...
</application>
```

The last step is to declare a home `Activity`. The concept of an activity should be familiar to
Android developers. It offers lifecycle management and the ability to provide an (optional) UI to
the user. Unlike the phone and tablets though, in Android Things you must have a single entry point
Activity. You specify this activity by creating an intent filter with the following parameters:

* Action: ACTION_MAIN
* Category: CATEGORY_DEFAULT
* Category: IOT_LAUNCHER

For ease of development, this same activity should include a _CATEGORY_LAUNCHER_ intent filter so
Android Studio can launch it as the default activity when deploying or debugging.

At this point, the Android Manifest should look something like:
```xml
<application
    android:label="@string/app_name">
    <uses-library android:name="com.google.android.things"/>
    <activity android:name=".HomeActivity">
        <!-- Launch activity as default from Android Studio -->
        <intent-filter>
            <action android:name="android.intent.action.MAIN"/>
            <category android:name="android.intent.category.LAUNCHER"/>
        </intent-filter>

        <!-- Launch activity automatically on boot -->
        <intent-filter>
            <action android:name="android.intent.action.MAIN"/>
            <category android:name="android.intent.category.IOT_LAUNCHER"/>
            <category android:name="android.intent.category.DEFAULT"/>
        </intent-filter>
    </activity>
</application>
```

### Accessing peripherals
In order to control the LED, we're going to use the basic Peripheral I/O APIs to discover and communicate
with General Purpose Input Ouput (GPIO) ports.

The system service responsible for managing peripheral connections is PeripheralManagerService. You
can use this service to list the available ports for all known peripheral types.

The following code writes the list of available GPIO ports to logcat:

```java
public class HomeActivity extends Activity {
    private static final String TAG = "HomeActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PeripheralManagerService service = new PeripheralManagerService();
        Log.d(TAG, "Available GPIO: " + service.getGpioList());
    }
}
```

### Controlling the LED
In order to toggle the LED on or off we have to get hold of the appropriate GPIO object from the list
that is outputted in the previous step. The proper GPIO depends on which physical pins you used to
connect the LED to. The pin diagram presented [above](#installing-android-things) details which
GPIO object we need.
> **Note:** The rest of this post assumes that the LED is connected between 6 (or another ground pin)
and 7 ()which corresponds to BCM4)

```java
public class HomeActivity extends Activity {
	private static final String TAG = "HomeActivity";
	private static final String GPIO_PIN_NAME = "BCM4";

	private Gpio mLedGpio;

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
    }
...
}
```
Once the appropriate object is identified we can read or set the state of the pin (and hence the LED)
with the following code:
```java
	private void setLed(boolean newState) {
		try {
			mLedGpio.setValue(newState);
		} catch (IOException e) {
			Log.e(TAG, "Error on PeripheralIO API", e);
		}
	}

	private boolean getLed() {
		try {
			return mLedGpio.getValue();
		} catch (IOException e) {
			Log.e(TAG, "Error on PeripheralIO API", e);
		}
		return false;
	}
```
Closing the GPIO object is done on the `onDestroy()` lifecycle method:
```java
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
```

Test it out by adding `setLed(true);` at the end of the `onCreate()` method and pressing the _Run_
button in _Android Studio_ and selecting the Raspberry Pi in the devices list.

### Bringing in Firebase
Now that we are able to control the led we will add _Firebase Database_ capabilities to the app and
use them to publish the LED's state on the cloud. To enable Firebase Realtime Database for your project:

1. Install the [Firebase Android SDK](https://firebase.google.com/docs/android/setup) into your app
project.
2. In the [Firebase console](https://firebase.google.com/console/), select Import Google Project to
import the Google Cloud project you created for Cloud Vision into Firebase.
3. Download and install the google-services.json file as described in the instructions.
4. Add the Firebase Realtime Database dependency to your app-level `build.gradle` file:
```groovy
dependencies {
    ...

    compile 'com.google.firebase:firebase-core:9.6.1'
    compile 'com.google.firebase:firebase-database:9.6.1'
}
```

You now need to specify who can read and write to your Firebase Realtime Database. To configure your
Firebase database access rules:

1. In the [Firebase console](https://firebase.google.com/console/), on the page for your project,
click _Database_.
2. Click Rules, and update the database rules to allow public read/write access:

            {
              "rules": {
                ".read": true,
                ".write": true
              }
            }

3. Click _Publish_.
> **Note:** For more information on setting database rules, see [Getting Started with Database Rules](https://firebase.google.com/docs/database/security/quickstart).

### Generating a device ID
We will generate a UUID unique identifier for each device we deploy the app to. This UUID will be
saved in the `SharedPreferences` and reused on subsequent launches. Add the following to `HomeActivity`:

```java
	private static final String UUID_KEY = "_UUID";
	private static final String PREFS_NAME = "MyPrefs";

	private String getDeviceId() {
		SharedPreferences prefs = getSharedPreferences(PREFS_NAME, 0);
		if(!prefs.contains(UUID_KEY)) {
			prefs.edit().putString(UUID_KEY, UUID.randomUUID().toString()).apply();
		}
		return prefs.getString(UUID_KEY, UUID.randomUUID().toString());
	}
```

### Database structure
Firebase is a non-relational database, in which data is represented in a tree structure. For this
application, we will have the following structure:

> _root_
>
>    - _device_ID_1_
>      + currentStatus
>          * ledOn _(boolean)_
>      + desiredStatus
>          * ledOn _(boolean)_
>    - _device_ID_2_
>      + currentStatus
>          * ledOn _(boolean)_
>      + desiredStatus
>          * ledOn _(boolean)_

### Publishing the status
We will now add code that saves the current status of LED in the Firebase database. This is the object
we will be saving:
```java
public class Status {
	private boolean ledOn;

	public boolean isLedOn() {
		return ledOn;
	}

	public void setLedOn(boolean ledOn) {
		this.ledOn = ledOn;
	}

}
```
And the code to save the status:
```java
public class HomeActivity extends Activity {
//...
	private Status mStatus = new Status();
	private DatabaseReference mCurrentStatusRef;
	private DatabaseReference mDesiredStatusRef;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

//...
		FirebaseDatabase database = FirebaseDatabase.getInstance();
		mCurrentStatusRef = database.getReference(getDeviceId()).child("currentStatus");

		mStatus.setLedOn(getLed());
		mCurrentStatusRef.setValue(mStatus);
	}

// ...
}
```
Also modify the `setLed()` method to now update the database:
```java
	private void setLed(boolean newState) {
		try {
			mLedGpio.setValue(newState);
		} catch (IOException e) {
			Log.e(TAG, "Error on PeripheralIO API", e);
		}
		mStatus.setLedOn(newState);
		mCurrentStatusRef.setValue(mStatus);
	}
```
Run the project. You should now see the data in the Firebase Console.

### Responding to requests
Now that we can publish the status of the LED we want to be able to listen to requests to toggle
the status of the led. We do this by associating a new status object with each device called `desiredState`.
Whenever we discover such an object exist we apply it and then delete it from the database.
```java
	@Override
	protected void onCreate(Bundle savedInstanceState) {
//...
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

	private void handleNewState(Status desiredStatus) {
		setLed(desiredStatus.isLedOn());
	}

}
```
To test this, you can go to the `Firebase Console` and create a `desiredStatus` object with the desired
`ledOn` value. The app should pick it up, update the LED, update the database `currentStatus` and delete
the `desiredStatus` object almost instantly.

## Building a remote control app
The next step would be to create a remote control app using Firebase. Technically, this app can use
any of the platforms supported by Firebase (for example it could be a Web App), however, we're going
to be building an Android that will allow you to control the led using an android phone. It will
consist of a single screen containing a list of the known devices, each showing the status of their
LED. Clicking on a device will cause the led of the device to change state.


### Creating another module
Although not very common, Android Studio and Gradle allow you to create several distinct modules in
the same project. We're going to use that feature to create the remote control app.

1. Click _File -> New -> New module..._
2. Choose _Phone/Tablet_
3. Name the new application `remote`
4. Choose an empty Activity and name it RemoteControlActivity.

### Adding Firebase to the remote module
**Note:** The following steps are quite similar to the ones you did previousely, however you must
repeat them for each module, since each module has a different package and hence a different JSON
file.
1. Install the [Firebase Android SDK](https://firebase.google.com/docs/android/setup) into your app
project.
2. In the [Firebase console](https://firebase.google.com/console/), select Import Google Project to
import the Google Cloud project you created for Cloud Vision into Firebase.
3. Download and install the google-services.json file as described in the instructions.
4. Add the Firebase Realtime Database dependency to your app-level `build.gradle` file:
```groovy
dependencies {
//    ...

    compile 'com.google.firebase:firebase-core:9.6.1'
    compile 'com.google.firebase:firebase-database:9.6.1'
    compile 'com.firebaseui:firebase-ui-database:0.5.3'
}
```

### Model classes
The remote app exchanges the following two model classes with Firebase Database:
```java
public class Status {
	private boolean ledOn;

	public boolean isLedOn() {
		return ledOn;
	}

	public void setLedOn(boolean ledOn) {
		this.ledOn = ledOn;
	}

}
```
```java
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
```

### Firebase UI Adapter
The Firebase UI library offers an implementation of RecyclerView adapter that responds immediately
to changes in the Firebase Database. This makes it easier to display Firbase Data since we don't have
to listen to the events ourselves.
```java
public class DeviceAdapter extends FirebaseRecyclerAdapter<Device, DeviceAdapter.DeviceViewHolder> {
	public static class DeviceViewHolder extends RecyclerView.ViewHolder {
		@BindView(R.id.indicator) AppCompatImageView indicator;
		@BindView(R.id.text) TextView text;

		public DeviceViewHolder(View v) {
			super(v);
			ButterKnife.bind(this, v);
		}
	}

	public DeviceAdapter(DatabaseReference reference) {
		super(Device.class, R.layout.item_device, DeviceViewHolder.class, reference);
	}

	@Override
	protected void populateViewHolder(DeviceViewHolder viewHolder, final Device device, final int position) {
		if(device.getCurrentStatus() != null && device.getCurrentStatus().isLedOn()) {
			viewHolder.indicator.setImageResource(R.drawable.ic_led_on);
		} else {
			viewHolder.indicator.setImageResource(R.drawable.ic_led_off);
		}
		viewHolder.text.setText(String.format(Locale.getDefault(), "Device %d", position));
		viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Status newStatus = new Status();
				newStatus.setLedOn(!device.getCurrentStatus().isLedOn());
				device.setDesiredStatus(newStatus);

				getRef(position).setValue(device);
			}
		});
	}

}
```

### The remote control activity
The last piece of the puzzle is implementing the Activity stuff. Since all the logic is handled by
the adapter, the code here is quite trivial

```java
public class RemoteControlActivity extends AppCompatActivity {
	@BindView(R.id.recycler) RecyclerView mRecycler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_remote_control);

		ButterKnife.bind(this);

		mRecycler.setLayoutManager(new LinearLayoutManager(this));
		mRecycler.setAdapter(new DeviceAdapter(FirebaseDatabase.getInstance().getReference()));
		mRecycler.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
	}

}
```

### Bringing it all together
You can run the remote control app by selecting the _remote_ module in the module drop-down (to the
left of the _Run_ button) and then pressing the _Run_ button in Android Studio. Select a connected
Android phone or emulator and wait for the app to be deployed. Once it starts, you should see a single
item in the list, corresponding to the device. The status of the LED should be reflected in the app.

Tapping the device in the app should toggle the state of the led. Please note there is a small delay
(depending on your network latency) between the moment you tap and the moment the icon changes. For
me it was ~100ms. This is due to the fact that the icon does not change until Firebase notifies the
app the status changed, and this takes two roundtrips + the processing time in the remote, Firebase
and the Raspberry Pi. The response time is quite small all things considering.

As a last test, start the remote app on two different phones (or a phone and an emulator). Notice
that tapping the device on one is almost instantly reflected on the other (the event is broadcast).

