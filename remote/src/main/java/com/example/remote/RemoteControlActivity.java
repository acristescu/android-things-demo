package com.example.remote;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.google.firebase.database.FirebaseDatabase;

import butterknife.BindView;
import butterknife.ButterKnife;

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
