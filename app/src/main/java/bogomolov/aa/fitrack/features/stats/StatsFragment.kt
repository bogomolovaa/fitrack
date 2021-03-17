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
        viewModel.tracksLiveData.observe(viewLifecycleOwner) { updateView(it) }

        viewModel.selectedPeriod.observe(viewLifecycleOwner) {
            binding.statsTextSelectedPeriod.text = it
        }
        viewModel.trackLiveData.observe(viewLifecycleOwner) { track ->
            binding.statsTextDistance.text = "${track.distance.toInt()} m"
            binding.statsTextTime.text = track.getTimeString()
            binding.statsTextAvgSpeed.text = "${String.format("%.1f", track.getSpeed())}  km/h"
        }
        viewModel.updateView()
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.stats_filters_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.stats_filters)
            FiltersBottomSheetDialogFragment(viewModel).show(
                childFragmentManager,
                "Filters bottom sheet"
            )
        return true
    }

    private fun getParamValue(track: Track?, param: Int): Double {
        if (track == null) return 0.0
        when (param) {
            PARAM_DISTANCE -> return track.distance / 1000.0
            PARAM_SPEED -> return track.getSpeed()
            PARAM_TIME -> return (track.endTime - track.startTime) / (60 * 1000.0)
        }
        return 0.0
    }

    private fun updateView(tracks: List<Track>) {
        val datesRange = viewModel.datesRange
        val selectedParam = viewModel.selectedParam
        val selectedTimeStep = viewModel.selectedTimeStep
        val selectedTimeFilter = viewModel.selectedTimeFilter
        val sumTracks = ArrayList<Track>()
        val dates = ArrayList<Date>()
        val calendar = GregorianCalendar()
        calendar.time = datesRange[0]
        while (calendar.time.time <= datesRange[1].time) {
            val date = calendar.time
            dates.add(date)
            if (selectedTimeStep == TIME_STEP_DAY) calendar.add(Calendar.DAY_OF_YEAR, 1)
            if (selectedTimeStep == TIME_STEP_WEEK) calendar.add(Calendar.DAY_OF_YEAR, 7)
            val toDate = calendar.time
            val dayTracks = ArrayList<Track>()
            for (track in tracks)
                if (track.startTime >= date.time && track.startTime < toDate.time)
                    dayTracks.add(track)
            if (dayTracks.size > 0) sumTracks.add(sumTracks(dayTracks))
        }

        val entries = ArrayList<BarEntry>()
        val categories = ArrayList<String>()
        for (i in sumTracks.indices) {
            val sumTrack = sumTracks[i]
            entries.add(BarEntry(i.toFloat(), getParamValue(sumTrack, selectedParam).toFloat()))
            calendar.time = dates[i]
            if (selectedTimeFilter == FILTER_TODAY) {
                categories.add(requireContext().resources.getString(R.string.today))
            } else if (selectedTimeFilter == FILTER_WEEK) {
                categories.add(
                    DateFormatSymbols(resources.configuration.locale).shortWeekdays[calendar.get(
                        Calendar.DAY_OF_WEEK
                    )]
                )
            } else {
                if (selectedTimeStep == TIME_STEP_DAY) {
                    if (selectedTimeFilter == FILTER_MONTH) {
                        categories.add("" + calendar.get(Calendar.DAY_OF_MONTH))
                    } else {
                        categories.add("" + calendar.get(Calendar.DATE))
                    }
                }
                if (selectedTimeStep == TIME_STEP_WEEK) {
                    if (selectedTimeFilter == FILTER_MONTH) {
                        categories.add("" + calendar.get(Calendar.WEEK_OF_MONTH))
                    } else {
                        categories.add("" + calendar.get(Calendar.WEEK_OF_YEAR))
                    }
                }
            }
        }
        val set =
            BarDataSet(entries, resources.getStringArray(R.array.stats_param_units)[selectedParam])
        val data = BarData(set)
        val formatter = IndexAxisValueFormatter(categories)
        val xAxis = chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.valueFormatter = formatter
        chart.data = data
        chart.setFitBars(true)
        chart.invalidate()
    }
}