package bogomolov.aa.fitrack.domain

import androidx.fragment.app.FragmentManager
import ru.slybeaver.slycalendarview.SlyCalendarDialog
import java.util.*


fun selectDatesRange(fm: FragmentManager, selector: (Array<Date>) -> Unit) {
    SlyCalendarDialog()
            .setSingle(false)
            .setCallback(object : SlyCalendarDialog.Callback {
                override fun onCancelled() {
                }

                override fun onDataSelected(firstDate: Calendar?, secondDate: Calendar?, hours: Int, minutes: Int) {
                    if (firstDate != null && secondDate != null) {
                        val calendar = GregorianCalendar()
                        calendar.time = firstDate.time
                        calendar.set(Calendar.HOUR_OF_DAY, 0)
                        calendar.set(Calendar.MINUTE, 0)
                        calendar.set(Calendar.SECOND, 0)
                        val date1 = calendar.time
                        calendar.time = secondDate.time
                        calendar.set(Calendar.HOUR_OF_DAY, 23)
                        calendar.set(Calendar.MINUTE, 59)
                        calendar.set(Calendar.SECOND, 59)
                        val date2 = calendar.time
                        selector(arrayOf(date1, date2))
                    }
                }
            }).show(fm, "TAG_SLYCALENDAR")
}

fun getMonthRange(): Array<Date> {
    val calendar = GregorianCalendar()
    calendar.time = Date()
    calendar.set(Calendar.DAY_OF_MONTH, 1)
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    val date1 = calendar.time
    calendar.add(Calendar.MONTH, 1)
    val date2 = Date(calendar.time.time - 1);
    return arrayOf(date1, date2)
}

fun getWeekRange(): Array<Date> {
    val calendar = GregorianCalendar()
    calendar.time = Date()
    calendar.set(Calendar.DAY_OF_WEEK, GregorianCalendar.MONDAY)
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    val date1 = calendar.time
    calendar.add(Calendar.DAY_OF_YEAR, 7)
    val date2 = Date(calendar.time.time - 1);
    return arrayOf(date1, date2)
}

fun getTodayRange(): Array<Date> {
    val calendar = GregorianCalendar()
    calendar.time = Date()
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    val date1 = calendar.time
    calendar.add(Calendar.DAY_OF_YEAR, 1)
    val date2 = Date(calendar.time.time - 1);
    return arrayOf(date1, date2)
}