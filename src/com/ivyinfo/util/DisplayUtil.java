package com.ivyinfo.util;

import android.content.Context;

public class DisplayUtil {
	/**
	 * 将px值转换为dip或dp值，保证尺寸大小不变
	 * 
	 * @param pxValue
	 * @return
	 */
	public static int px2dip(Context cxt, float pxValue) {
		final float scale = cxt.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}

	/**
	 * 将dip或dp值转换为px值，保证尺寸大小不变
	 * 
	 * @param dipValue
	 * @return
	 */
	public static int dip2px(Context cxt, float dipValue) {
		final float scale = cxt.getResources().getDisplayMetrics().density;
		return (int) (dipValue * scale + 0.5f);
	}

}
