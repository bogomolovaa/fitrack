package bogomolov.aa.fitrack.model;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.DatePicker;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DateUtils {

    public static void selectDatesRange(FragmentManager fragmentManager, Context context, DatesSelector datesSelector) {
        selectDatesRange(fragmentManager, context, new Date[2], datesSelector);
    }

    private static void selectDatesRange(FragmentManager fragmentManager, Context context, final Date[] dates, final DatesSelector datesSelector) {
        new DatePickerFragment(context, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                Calendar c = Calendar.getInstance();
                c.set(Calendar.YEAR, year);
                c.set(Calendar.MONTH, month);
                c.set(Calendar.DAY_OF_MONTH, day);
                c.set(Calendar.HOUR_OF_DAY, 0);
                c.set(Calendar.MINUTE, 0);
                c.set(Calendar.SECOND, 0);
                c.set(Calendar.MILLISECOND, 0);
                if (dates[0] == null) {
                    dates[0] = c.getTime();
                    selectDatesRange(fragmentManager, context, dates, datesSelector);
                } else {
                    dates[1] = c.getTime();
                    datesSelector.onSelect(dates);
                }
            }
        }).show(fragmentManager, "datePicker");
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

    public static class DatePickerFragment extends DialogFragment {
        private DatePickerDialog.OnDateSetListener listener;
        private Context context;

        public DatePickerFragment(Context context, DatePickerDialog.OnDateSetListener listener) {
            this.context = context;
            this.listener = listener;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            return new DatePickerDialog(context, listener, year, month, day);
        }
    }

    public interface DatesSelector {
        void onSelect(Date[] dates);
    }
}
