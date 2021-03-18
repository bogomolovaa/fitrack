package bogomolov.aa.fitrack.features.stats

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import bogomolov.aa.fitrack.R
import bogomolov.aa.fitrack.databinding.FragmentStatsBinding
import bogomolov.aa.fitrack.di.ViewModelFactory
import bogomolov.aa.fitrack.domain.model.Track
import bogomolov.aa.fitrack.domain.model.sumTracks
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import dagger.android.support.AndroidSupportInjection
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class StatsFragment : Fragment() {
    @Inject
    internal lateinit var viewModelFactory: ViewModelFactory
    private val viewModel: StatsViewModel by viewModels { viewModelFactory }
    private lateinit var chart: BarChart
    private lateinit var binding: FragmentStatsBinding

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStatsBinding.inflate(inflater, container, false)
        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)
        setHasOptionsMenu(true)
        val navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
        NavigationUI.setupWithNavController(binding.toolbar, navController)

        chart = binding.chart
        chart.description = Description().apply { text = "" }
        viewModel.tracksLiveData.observe(viewLifecycleOwner) { updateChart() }
        viewModel.sumTrackLiveData.observe(viewLifecycleOwner) { track ->
            binding.statsTextDistance.text = "${track.distance.toInt()} m"
            binding.statsTextTime.text = track.getTimeString()
            binding.statsTextAvgSpeed.text = "${String.format("%.1f", track.getSpeed())}  km/h"
            val startDateString = dateToString(viewModel.datesRange[0])
            val endDateString = dateToString(viewModel.datesRange[0])
            binding.statsTextSelectedPeriod.text = "$startDateString - $endDateString"
        }
        return binding.root
    }

    @SuppressLint("SimpleDateFormat")
    private fun dateToString(date: Date) =
        SimpleDateFormat("dd.MM.yyyy HH:mm").format(date)

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.stats_filters_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.stats_filters)
            FiltersBottomSheetDialogFragment(this, viewModel).show(childFragmentManager, "")
        return true
    }

    private fun getParamValue(track: Track?, param: Int): Double {
        if (track == null) return 0.0
        return when (param) {
            PARAM_DISTANCE -> return track.distance / 1000.0
            PARAM_SPEED -> return track.getSpeed()
            PARAM_TIME -> return (track.endTime - track.startTime) / (60 * 1000.0)
            else -> 0.0
        }
    }

    fun updateChart() {
        val tracks = viewModel.tracksLiveData.value ?: return
        val dates = getDates(viewModel.timeStep, viewModel.datesRange)
        val sumTracks = dates.zipWithNext { date1, date2 ->
            sumTracks(tracks.filter { it.startTime >= date1.time && it.startTime < date2.time })
        }
        dates.removeLast()
        val categories = getCategories(dates, viewModel.timeFilter, viewModel.timeStep)
        val entries = sumTracks.withIndex().map {
            BarEntry(it.index.toFloat(), getParamValue(it.value, viewModel.param).toFloat())
        }
        val unit = resources.getStringArray(R.array.stats_param_units)[viewModel.param]
        with(chart.xAxis) {
            position = XAxis.XAxisPosition.BOTTOM
            granularity = 1f
            valueFormatter = IndexAxisValueFormatter(categories)
        }
        with(chart) {
            data = BarData(BarDataSet(entries, unit))
            setFitBars(true)
            invalidate()
        }
    }

    private fun getDates(timeStep: Int, datesRange: Array<Date>): MutableList<Date> {
        val dates = ArrayList<Date>()
        val calendar = GregorianCalendar()
        calendar.time = datesRange[0]
        while (calendar.time <= datesRange[1]) {
            dates.add(calendar.time)
            if (timeStep == TIME_STEP_DAY) calendar.add(Calendar.DAY_OF_YEAR, 1)
            if (timeStep == TIME_STEP_WEEK) calendar.add(Calendar.DAY_OF_YEAR, 7)
        }
        dates.add(calendar.time)
        return dates
    }

    private fun getCategories(dates: List<Date>, timeFilter: Int, timeStep: Int): List<String> {
        val calendar = GregorianCalendar()
        val categories = ArrayList<String>()
        for (date in dates) {
            calendar.time = date
            val category = when (timeFilter) {
                FILTER_TODAY -> requireContext().resources.getString(R.string.today)
                FILTER_WEEK -> DateFormatSymbols(resources.configuration.locale)
                    .shortWeekdays[calendar.get(Calendar.DAY_OF_WEEK)]
                else -> {
                    val field = when {
                        timeFilter == FILTER_MONTH && timeStep == TIME_STEP_DAY -> Calendar.DAY_OF_MONTH
                        timeFilter == FILTER_MONTH && timeStep == TIME_STEP_WEEK -> Calendar.WEEK_OF_MONTH
                        timeFilter != FILTER_MONTH && timeStep == TIME_STEP_DAY -> Calendar.DATE
                        timeFilter != FILTER_MONTH && timeStep == TIME_STEP_WEEK -> Calendar.WEEK_OF_YEAR
                        else -> Calendar.DATE
                    }
                    calendar.get(field).toString()
                }
            }
            categories.add(category)
        }
        return categories
    }
}