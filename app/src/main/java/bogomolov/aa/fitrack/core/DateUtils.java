package bogomolov.aa.fitrack.core;

import androidx.fragment.app.FragmentManager;


import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import ru.slybeaver.slycalendarview.SlyCalendarDialog;

public class DateUtils {

    public static void selectDatesRange(FragmentManager fragmentManager, DatesSelector datesSelector){
        new SlyCalendarDialog()
                .setSingle(false)
                .setCallback(new SlyCalendarDialog.Callback() {
                    @Override
                    public void onCancelled() {

                    }

                    @Override
                    public void onDataSelected(Calendar firstDate, Calendar secondDate, int hours, int minutes) {
                        Date[] dates = new Date[2];
                        Calendar calendar = new GregorianCalendar();
                        calendar.setTime(firstDate.getTime());
                        calendar.set(Calendar.HOUR_OF_DAY, 0);
                        calendar.set(Calendar.MINUTE, 0);
                        calendar.set(Calendar.SECOND, 0);
                        dates[0] = calendar.getTime();
                        calendar.setTime(secondDate.getTime());
                        calendar.set(Calendar.HOUR_OF_DAY, 23);
                        calendar.set(Calendar.MINUTE, 59);
                        calendar.set(Calendar.SECOND, 59);
                        dates[1] = calendar.getTime();
                        datesSelector.onSelect(dates);
                    }
                })
                .show(fragmentManager, "TAG_SLYCALENDAR");
    }

    public static Date[] getMonthRange() {
        Date[] dateRange = new Date[2];
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(new Date());
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        dateRange[0] = calendar.getTime();
        calendar.add(Calendar.MONTH, 1);
        dateRange[1] = new Date(calendar.getTime().getTime() - 1);
        return dateRange;
    }

    public static Date[] getWeekRange() {
        Date[] dateRange = new Date[2];
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(new Date());
        calendar.set(Calendar.DAY_OF_WEEK, GregorianCalendar.MONDAY);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        dateRange[0] = calendar.getTime();
        calendar.add(Calendar.DAY_OF_YEAR, 7);
        dateRange[1] = new Date(calendar.getTime().getTime() - 1);
        return dateRange;
    }

    public static Date[] getTodayRange() {
        Date[] dateRange = new Date[2];
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(new Date());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        dateRange[0] = calendar.getTime();
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        dateRange[1] = new Date(calendar.getTime().getTime() - 1);
        return dateRange;
    }


    public interface DatesSelector {
        void onSelect(Date[] dates);
    }
}
