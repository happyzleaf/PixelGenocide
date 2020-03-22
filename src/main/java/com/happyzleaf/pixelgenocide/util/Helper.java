package com.happyzleaf.pixelgenocide.util;

public class Helper {
	public static String toHuman(long seconds) {
		long hours = seconds / 3600;
		int minutes = (int) (seconds % 3600 / 60);
		int secs = (int) (seconds % 60);
		StringBuilder s = new StringBuilder();
		if (hours != 0) {
			s.append(hours).append('h');
		}
		if (minutes != 0) {
			s.append(minutes).append('m');
		}
		if (secs == 0 && s.length() > 0) {
			return s.toString();
		}
		s.append(secs);
		s.append('s');
		return s.toString();
	}
}
