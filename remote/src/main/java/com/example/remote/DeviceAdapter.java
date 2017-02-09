package com.example.remote;

import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by acristescu on 01/02/2017.
 */

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
