/*
 * Copyright (C) 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package libcore.java.util;


import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import junit.framework.TestCase;

public class GregorianCalendarTest extends TestCase {

    private static final TimeZone LOS_ANGELES = TimeZone.getTimeZone("America/Los_Angeles");

    private static final TimeZone LONDON = TimeZone.getTimeZone("Europe/London");

    // Documented a previous difference in behavior between this and the RI, see
    // https://code.google.com/p/android/issues/detail?id=61993 for more details.
    // Switching to OpenJDK has fixed that issue and so this test has been changed to reflect
    // the correct behavior.
    public void test_computeFields_dayOfWeekAndWeekOfYearSet() {
        Calendar greg = new GregorianCalendar(LOS_ANGELES, Locale.ENGLISH);

        // Ensure we use different values to the default ones.
        int differentWeekOfYear = greg.get(Calendar.WEEK_OF_YEAR) == 1 ? 2 : 1;
        int differentDayOfWeek = greg.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY
                ? Calendar.TUESDAY : Calendar.MONDAY;

        // Setting WEEK_OF_YEAR and DAY_OF_WEEK with an intervening
        // call to computeFields will work.
        greg.set(Calendar.WEEK_OF_YEAR, differentWeekOfYear);
        assertEquals(differentWeekOfYear, greg.get(Calendar.WEEK_OF_YEAR));
        greg.set(Calendar.DAY_OF_WEEK, differentDayOfWeek);
        assertEquals(differentWeekOfYear, greg.get(Calendar.WEEK_OF_YEAR));

        // Setting WEEK_OF_YEAR after DAY_OF_WEEK with no intervening
        // call to computeFields will work.
        greg = new GregorianCalendar(LOS_ANGELES, Locale.ENGLISH);
        greg.set(Calendar.DAY_OF_WEEK, differentDayOfWeek);
        greg.set(Calendar.WEEK_OF_YEAR, differentWeekOfYear);
        assertEquals(differentWeekOfYear, greg.get(Calendar.WEEK_OF_YEAR));
        assertEquals(differentDayOfWeek, greg.get(Calendar.DAY_OF_WEEK));

        // Setting DAY_OF_WEEK after WEEK_OF_YEAR with no intervening
        // call to computeFields will work.
        greg = new GregorianCalendar(LOS_ANGELES, Locale.ENGLISH);
        greg.set(Calendar.WEEK_OF_YEAR, differentWeekOfYear);
        greg.set(Calendar.DAY_OF_WEEK, differentDayOfWeek);
        assertEquals(differentWeekOfYear, greg.get(Calendar.WEEK_OF_YEAR));
        assertEquals(differentDayOfWeek, greg.get(Calendar.DAY_OF_WEEK));
    }

    /**
     * Specialized tests for those fields affected by GregorianCalendar cut over date.
     *
     * <p>Expands on a regression test created for harmony-2947.
     */
    public void test_fieldsAffectedByGregorianCutOver() {

        Date date = new Date(Date.parse("Jan 1 00:00:01 GMT 2000"));
        assertEquals(946684801000L, date.getTime());

        GregorianCalendar gc;

        // Test in America/Los_Angeles
        gc = new GregorianCalendar(LOS_ANGELES, Locale.ENGLISH);
        gc.setGregorianChange(date);
        gc.setTime(date);

        // Check the date to ensure that it is 18th Dec 1999. The reason this is not 1st Jan 2000
        // is that the offset for Los Angeles is GMT-08:00. setGregorianChange() is interpreted as
        // a wall time, not an instant. So, the instant that corresponds to
        // "1st Jan 2000 00:00:01 GMT" is 8 hours before the Julian/Gregorian switch in LA. The day
        // before 1st Jan 2000 (Gregorian Calendar) would be 18th Dec 1999 in the Julian calendar.
        // The reason it is not the 31st Dec 1999 is simply a result of the discontinuity that
        // occurred when switching calendars. That happened for real when the calendars were
        // switched in 1582.
        //
        // A different year explains why we get very different results for the methods being tested.
        assertEquals(1999, gc.get(Calendar.YEAR));
        assertEquals(Calendar.DECEMBER, gc.get(Calendar.MONTH));
        assertEquals(18, gc.get(Calendar.DAY_OF_MONTH));

        assertEquals(50, gc.getActualMaximum(Calendar.WEEK_OF_YEAR));
        assertEquals(50, gc.getLeastMaximum(Calendar.WEEK_OF_YEAR));
        assertEquals(3, gc.getActualMaximum(Calendar.WEEK_OF_MONTH));
        assertEquals(3, gc.getLeastMaximum(Calendar.WEEK_OF_MONTH));
        assertEquals(18, gc.getActualMaximum(Calendar.DAY_OF_MONTH));
        assertEquals(18, gc.getLeastMaximum(Calendar.DAY_OF_MONTH));
        assertEquals(352, gc.getActualMaximum(Calendar.DAY_OF_YEAR));
        assertEquals(352, gc.getLeastMaximum(Calendar.DAY_OF_YEAR));
        assertEquals(3, gc.getActualMaximum(Calendar.DAY_OF_WEEK_IN_MONTH));
        assertEquals(3, gc.getLeastMaximum(Calendar.DAY_OF_WEEK_IN_MONTH));

        // Test in Europe/London
        gc = new GregorianCalendar(LONDON, Locale.ENGLISH);
        gc.setGregorianChange(date);
        gc.setTime(date);

        // Check the date is actually 1st Jan 2000.
        assertEquals(2000, gc.get(Calendar.YEAR));
        assertEquals(Calendar.JANUARY, gc.get(Calendar.MONTH));
        assertEquals(1, gc.get(Calendar.DAY_OF_MONTH));

        assertEquals(53, gc.getActualMaximum(Calendar.WEEK_OF_YEAR));
        assertEquals(52, gc.getLeastMaximum(Calendar.WEEK_OF_YEAR));
        assertEquals(5, gc.getActualMaximum(Calendar.WEEK_OF_MONTH));
        assertEquals(4, gc.getLeastMaximum(Calendar.WEEK_OF_MONTH));
        assertEquals(31, gc.getActualMaximum(Calendar.DAY_OF_MONTH));
        assertEquals(28, gc.getLeastMaximum(Calendar.DAY_OF_MONTH));
        assertEquals(366, gc.getActualMaximum(Calendar.DAY_OF_YEAR));
        assertEquals(365, gc.getLeastMaximum(Calendar.DAY_OF_YEAR));
        assertEquals(5, gc.getActualMaximum(Calendar.DAY_OF_WEEK_IN_MONTH));
        assertEquals(4, gc.getLeastMaximum(Calendar.DAY_OF_WEEK_IN_MONTH));
    }

    public void test_isWeekDateSupported() {
        assertTrue(new GregorianCalendar().isWeekDateSupported());
    }

    public void test_setWeekDate() {
        GregorianCalendar cal = new GregorianCalendar();

        // When first week should have at least 4 days
        cal.setMinimalDaysInFirstWeek(4);
        cal.setWeekDate(2016, 13, Calendar.TUESDAY);
        assertEquals(Calendar.TUESDAY, cal.get(Calendar.DAY_OF_WEEK));
        assertEquals(29, cal.get(Calendar.DAY_OF_MONTH));

        // When first week can have single day
        cal.setMinimalDaysInFirstWeek(1);
        cal.setWeekDate(2016, 13, Calendar.TUESDAY);
        assertEquals(Calendar.TUESDAY, cal.get(Calendar.DAY_OF_WEEK));
        assertEquals(22, cal.get(Calendar.DAY_OF_MONTH));

        try {
            cal.setWeekDate(2016, 13, Calendar.SATURDAY + 1);
            fail();
        } catch (IllegalArgumentException expected) {
        }

        try {
            cal.setWeekDate(2016, 13, Calendar.SUNDAY - 1);
            fail();
        } catch (IllegalArgumentException expected) {
        }

        // in non-lenient mode
        cal.setLenient(false);
        try {
            cal.setWeekDate(2016, 60, Calendar.SATURDAY);
            fail();
        } catch (IllegalArgumentException expected) {}

        try {
            cal.setWeekDate(-1, 60, Calendar.SATURDAY);
            fail();
        } catch (IllegalArgumentException expected) {}
    }

    public void test_getWeekYear() {
        GregorianCalendar cal = new GregorianCalendar();

        cal.set(2016, Calendar.MARCH, 29);
        assertEquals(2016, cal.getWeekYear());

        // With minimal days in first week is set to 4
        cal.setMinimalDaysInFirstWeek(4);
        cal.set(2016, Calendar.JANUARY, 1);
        assertEquals(2015, cal.getWeekYear());
        cal.set(2015, Calendar.DECEMBER, 31);
        assertEquals(2015, cal.getWeekYear());

        // With minimal days in first week is set to 1
        cal.setMinimalDaysInFirstWeek(1);
        cal.set(2016, Calendar.JANUARY, 1);
        assertEquals(2016, cal.getWeekYear());
        cal.set(2015, Calendar.DECEMBER, 31);
        assertEquals(2016, cal.getWeekYear());
    }

    public void test_getWeeksInWeekYear() {
        GregorianCalendar cal = new GregorianCalendar();

        // With minimal days in first week is set to 1
        cal.setMinimalDaysInFirstWeek(1);
        cal.set(2016, Calendar.JANUARY, 10);
        assertEquals(53, cal.getWeeksInWeekYear());

        // With minimal days in first week is set to 4
        cal.setMinimalDaysInFirstWeek(4);
        cal.set(2016, Calendar.JANUARY, 10);
        assertEquals(52, cal.getWeeksInWeekYear());
    }
}
