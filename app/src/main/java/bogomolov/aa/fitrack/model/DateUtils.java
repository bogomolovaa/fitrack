package bogomolov.aa.fitrack.model;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.DatePicker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import bogomolov.aa.fitrack.view.activities.TracksListActivity;

public class DateUtils {

    public static void selectDatesRange(AppCompatActivity activity, DatesSelector datesSelector) {
        selectDatesRange(activity, new Date[2], datesSelector);
    }

    private static void selectDatesRange(final AppCompatActivity activity, final Date[] dates, final DatesSelector datesSelector) {
        new DatePickerFragment(activity, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                Calendar c = Calendar.getInstance();
                c.set(Calendar.YEAR, year);
                c.set(Calendar.MONTH, month);
                c.set(Calendar.DAY_OF_MONTH, day);
                if (dates[0] == null) {
                    dates[0] = c.getTime();
                    Log.i("test", "date[0] " + dates[0]);
                    selectDatesRange(activity,dates,datesSelector);
                } else {
                    dates[1] = c.getTime();
                    Log.i("test", "date[1] " + dates[1]);
                    datesSelector.onSelect(dates);
                }
            }
        }).show(activity.getSupportFragmentManager(), "datePicker");
    }

    public static Date[] getMonthRange() {
        Date[] dateRange = new Date[2];
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(new Date());
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        dateRange[0] = calendar.getTime();
        calendar.add(Calendar.MONTH, 1);
        dateRange[1] = calendar.getTime();
        return dateRange;
    }

    public static Date[] getWeekRange() {
        Date[] dateRange = new Date[2];
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(new Date());
        calendar.set(Calendar.DAY_OF_WEEK, GregorianCalendar.MONDAY);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        dateRange[0] = calendar.getTime();
        calendar.add(Calendar.DAY_OF_YEAR, 7);
        dateRange[1] = calendar.getTime();
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
        dateRange[1] = calendar.getTime();
        return dateRange;
    }

    public static class DatePickerFragment extends DialogFragment {
        private DatePickerDialog.OnDateSetListener listener;
        private Activity activity;

        public DatePickerFragment(Activity activity, DatePickerDialog.OnDateSetListener listener) {
            this.activity = activity;
            this.listener = listener;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            return new DatePickerDialog(activity, listener, year, month, day);
        }
    }

    public interface DatesSelector{
        void onSelect(Date[] dates);
    }
}
