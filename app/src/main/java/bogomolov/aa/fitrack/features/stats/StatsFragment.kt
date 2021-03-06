package bogomolov.aa.fitrack.features.stats


import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import bogomolov.aa.fitrack.R
import bogomolov.aa.fitrack.databinding.FragmentStatsBinding
import bogomolov.aa.fitrack.di.ViewModelFactory
import bogomolov.aa.fitrack.domain.model.Track
import bogomolov.aa.fitrack.domain.model.sumTracks
import bogomolov.aa.fitrack.features.stats.StatsViewModel.Companion.FILTER_MONTH
import bogomolov.aa.fitrack.features.stats.StatsViewModel.Companion.FILTER_TODAY
import bogomolov.aa.fitrack.features.stats.StatsViewModel.Companion.FILTER_WEEK
import bogomolov.aa.fitrack.features.stats.StatsViewModel.Companion.PARAM_DISTANCE
import bogomolov.aa.fitrack.features.stats.StatsViewModel.Companion.PARAM_SPEED
import bogomolov.aa.fitrack.features.stats.StatsViewModel.Companion.PARAM_TIME
import bogomolov.aa.fitrack.features.stats.StatsViewModel.Companion.TIME_STEP_DAY
import bogomolov.aa.fitrack.features.stats.StatsViewModel.Companion.TIME_STEP_WEEK
import com.github.mikephil.charting.charts.BarChart
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
    private lateinit var chart: BarChart
    private lateinit var viewModel: StatsViewModel

    @Inject
    internal lateinit var viewModelFactory: ViewModelFactory

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        viewModel = ViewModelProvider(this, viewModelFactory).get(StatsViewModel::class.java)
        val binding = DataBindingUtil.inflate<FragmentStatsBinding>(inflater, R.layout.fragment_stats, container, false)
        binding.lifecycleOwner = this
        val view = binding.root
        binding.viewModel = viewModel

        chart = binding.chart

        val toolbar = binding.toolbarStats
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        setHasOptionsMenu(true)

        val navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
        NavigationUI.setupWithNavController(toolbar, navController)

        viewModel.tracksLiveData.observe(viewLifecycleOwner) { tracks ->
            updateView(viewModel.datesRange, tracks, viewModel.selectedParam, viewModel.selectedTimeStep, viewModel.selectedTimeFilter)
        }
        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.stats_filters_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.stats_filters -> FiltersBottomSheetDialogFragment().show(childFragmentManager, "Filters bottom sheet")
            else -> {
            }
        }
        return true
    }

    override fun onStart() {
        super.onStart()
        viewModel.updateView(true)
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

    private fun updateView(datesRange: Array<Date>, tracks: List<Track>, selectedParam: Int, selectedTimeStep: Int, selectedTimeFilter: Int) {
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
                categories.add(DateFormatSymbols(resources.configuration.locale).shortWeekdays[calendar.get(Calendar.DAY_OF_WEEK)])
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
        val set = BarDataSet(entries, resources.getStringArray(R.array.stats_param_units)[selectedParam])
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
