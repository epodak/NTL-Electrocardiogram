/*
 * Copyright (C) 2011 TopCoder Inc., All Rights Reserved.
 */
package com.nasa.ecg.views;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nasa.ecg.HomeActivity;
import com.nasa.ecg.R;

/**
 * 
 * This is the home element view.
 * 
 * @author mohamede1945
 * @version 1.0
 */
public class HomeElement extends LinearLayout {

	/** The gesture detector. */
	private final GestureDetector gestureDetector;

	/** The home activity. */
	private final HomeActivity homeActivity;

	/** The listener. */
	private OnClickListener listener;

	/**
	 * Instantiates a new home element.
	 *
	 * @param activity the activity
	 */
	public HomeElement(HomeActivity activity) {
		super(activity);

		homeActivity = activity;
		gestureDetector = new GestureDetector(activity,
				new HomeElementGestureListener());

		LayoutInflater inflater = (LayoutInflater) getContext()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.home_element, this);
	}

	/**
	 * Sets whether this view is selected or not.
	 * 
	 * @param selected
	 *            whether this view is selected or not.
	 */
	@Override
	public void setSelected(boolean selected) {
		super.setSelected(selected);

		if (selected) {
			findViewById(R.id.homeElementSelectedImage).setVisibility(
					View.VISIBLE);
			findViewById(R.id.homeElementFolderImage).setBackgroundResource(
					R.drawable.home_folder_selected);

			setItalic(R.id.homeElementBornValueText);
			setBoldItalic(R.id.homeElementBornText);
			setItalic(R.id.homeElementCodeValueText);
			setBoldItalic(R.id.homeElementCodeText);
			setItalic(R.id.homeElementLastECGValueText);
			setBoldItalic(R.id.homeElementLastECGText);

			TextView nameText = (TextView) findViewById(R.id.homeElementNameText);
			nameText.setTextColor(Color.WHITE);

		} else {
			findViewById(R.id.homeElementSelectedImage).setVisibility(
					View.INVISIBLE);
			findViewById(R.id.homeElementFolderImage).setBackgroundResource(
					R.drawable.home_folder);

			setNormal(R.id.homeElementBornValueText);
			setBold(R.id.homeElementBornText);
			setNormal(R.id.homeElementCodeValueText);
			setBold(R.id.homeElementCodeText);
			setNormal(R.id.homeElementLastECGValueText);
			setBold(R.id.homeElementLastECGText);

			TextView nameText = (TextView) findViewById(R.id.homeElementNameText);
			nameText.setTextColor(0xFF0F92CD);
		}
	}

	/**
	 * Sets the italic.
	 * 
	 * @param resId
	 *            the new italic
	 */
	private void setItalic(int resId) {
		TextView text = (TextView) findViewById(resId);

		text.setTypeface(text.getTypeface(), Typeface.ITALIC);
		text.setTextColor(Color.WHITE);
	}

	/**
	 * Sets the bold italic.
	 * 
	 * @param resId
	 *            the new bold italic
	 */
	private void setBoldItalic(int resId) {
		TextView text = (TextView) findViewById(resId);

		text.setTypeface(text.getTypeface(), Typeface.BOLD_ITALIC);
		text.setTextColor(Color.WHITE);
	}

	/**
	 * Sets the bold.
	 * 
	 * @param resId
	 *            the new bold
	 */
	private void setBold(int resId) {
		TextView text = (TextView) findViewById(resId);

		text.setTypeface(text.getTypeface(), Typeface.BOLD);
		text.setTextColor(Color.BLACK);
	}

	/**
	 * Sets the normal.
	 * 
	 * @param resId
	 *            the new normal
	 */
	private void setNormal(int resId) {
		TextView text = (TextView) findViewById(resId);

		text.setTypeface(text.getTypeface(), Typeface.NORMAL);
		text.setTextColor(Color.BLACK);
	}

	/**
	 * @see android.view.View#onTouchEvent(android.view.MotionEvent)
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {

		Log.d("onTouchEvent", event.toString());
		gestureDetector.onTouchEvent(event);

		 return true;
	}

	/**
	 * @see android.view.View#setOnClickListener(android.view.View.OnClickListener)
	 */
	@Override
	public void setOnClickListener(OnClickListener l) {
		listener = l;
	}

	/**
	 * The listener interface for receiving homeElementGesture events.
	 * The class that is interested in processing a homeElementGesture
	 * event implements this interface, and the object created
	 * with that class is registered with a component using the
	 * component's <code>addHomeElementGestureListener<code> method. When
	 * the homeElementGesture event occurs, that object's appropriate
	 * method is invoked.
	 *
	 * @see HomeElementGestureEvent
	 */
	class HomeElementGestureListener extends
			GestureDetector.SimpleOnGestureListener {

		/**
		 * @see android.view.GestureDetector.SimpleOnGestureListener#onDoubleTap(android.view.MotionEvent)
		 */
		@Override
		public boolean onDoubleTap(MotionEvent ev) {
			Log.d("onDoubleTap", ev.toString());
			homeActivity.open();

			return false;
		}

		/**
		 * @see android.view.GestureDetector.SimpleOnGestureListener#onSingleTapConfirmed(android.view.MotionEvent)
		 */
		@Override
		public boolean onSingleTapConfirmed(MotionEvent ev) {
			Log.d("onSingleTapConfirmed", ev.toString());
			if (listener != null) {
				listener.onClick(HomeElement.this);
			}
			return false;
		}
	}
}
