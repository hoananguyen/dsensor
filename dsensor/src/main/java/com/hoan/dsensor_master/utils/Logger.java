package com.hoan.dsensor_master.utils;

import android.util.Log;

import com.hoan.dsensor_master.DProcessedSensorEvent;
import com.hoan.dsensor_master.DSensorEvent;
import com.hoan.dsensor_master.DSensorEventProcessor;
import com.hoan.dsensor_master.DSensorManager;

import java.util.Arrays;
import java.util.List;

/**
 * Convenient class for logging
 * Created by Hoan on 1/30/2016.
 */
public class Logger {
    private static final boolean WRITE_TO_FILE = false;

	private static final List<String> DEBUG_CLASSES = Arrays.asList(
			//"DirectionHistory",
			//DMath.class.getSimpleName(),
			//DProcessedSensorEvent.class.getSimpleName(),
			//DSensorEvent.class.getSimpleName(),
			//DSensorEventProcessor.class.getSimpleName(),
         	//DSensorManager.class.getSimpleName(),
			//"WorldHistory"
    );

	private Logger() {

	}

	public static synchronized void d(String tag, String msg)
	{
		if (DEBUG_CLASSES.contains(tag))
		{
			Log.e(tag, msg);
			if (WRITE_TO_FILE)
			{
				// TODO write to file.
			}
		}
	}
}
