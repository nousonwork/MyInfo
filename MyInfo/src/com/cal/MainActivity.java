package com.cal;


import com.cal.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
	//	Intent ivSvc= new Intent(getInstance(), MyInfoService.class);
		//getInstance().startService(ivSvc);
	}

	public MainActivity getInstance() {
		return this;
	}

	
}
