package org.sunspotworld;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class DateUtils {
    public static String toDateString(long time) {
        TimeZone jst = new TimeZone() {
            public int getOffset(int era, int year, int month, int day,
                int dayOfWeek, int millis) {
                return 9 * 3600 * 1000;
            }
            public int getRawOffset() {
                return 9 * 3600 * 1000;
            }
            public boolean useDaylightTime() {
                return false;
            }
        };
        Calendar cal = Calendar.getInstance(jst);
        cal.setTime(new Date(time));
        int yy = cal.get(Calendar.YEAR);
        int mm = cal.get(Calendar.MONTH) + 1;
        int dd = cal.get(Calendar.DAY_OF_MONTH);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int min = cal.get(Calendar.MINUTE);
        int sec = cal.get(Calendar.SECOND);
        int milli = cal.get(Calendar.MILLISECOND);
        return to2Digit(yy) + "/" + to2Digit(mm) + "/" + to2Digit(dd) + " " +
            to2Digit(hour) + ":" + to2Digit(min) + ":" + to2Digit(sec) + "(" + milli + ")";
    }

    private static String to2Digit(int n) {
        String s = "0" + n;
        return s.substring(s.length() - 2);
    }
}
