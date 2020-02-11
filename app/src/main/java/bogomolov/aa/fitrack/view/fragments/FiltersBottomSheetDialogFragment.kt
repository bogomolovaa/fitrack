package bogomolov.aa.fitrack.view.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider

import com.google.android.material.bottomsheet.BottomSheetDialogFragment

import javax.inject.Inject

import bogomolov.aa.fitrack.R
import bogomolov.aa.fitrack.core.*
import bogomolov.aa.fitrack.dagger.ViewModelFactory
import bogomolov.aa.fitrack.databinding.FiltersDialogBinding
import bogomolov.aa.fitrack.viewmodels.StatsViewModel
import dagger.android.support.AndroidSupportInjection

import bogomolov.aa.fitrack.core.getMonthRange
import bogomolov.aa.fitrack.core.getTodayRange
import bogomolov.aa.fitrack.core.getWeekRange
import bogomolov.aa.fitrack.viewmodels.StatsViewModel.Companion.FILTER_MONTH
import bogomolov.aa.fitrack.viewmodels.StatsViewModel.Companion.FILTER_SELECT
import bogomolov.aa.fitrack.viewmodels.StatsViewModel.Companion.FILTER_TODAY
import bogomolov.aa.fitrack.viewmodels.StatsViewModel.Companion.FILTER_WEEK
import bogomolov.aa.fitrack.viewmodels.StatsViewModel.Companion.TIME_STEP_DAY

class FiltersBottomSheetDialogFragment : BottomSheetDialogFragment() {
    private lateinit var viewModel: StatsViewModel
    private var spinnersCanClicked: Boolean = false
    @Inject
    internal lateinit var viewModelFactory: ViewModelFactory

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewModel = ViewModelProvider(parentFragment!!, viewModelFactory).get(StatsViewModel::class.java)
        val binding = DataBindingUtil.inflate<FiltersDialogBinding>(inflater, R.layout.filters_dialog, container, false)
        binding.lifecycleOwner = this
        val view = binding.root
        binding.viewModel = viewModel

        val periodSpinner = binding.statsSpinnerPeriod

        periodSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                if (!spinnersCanClicked) return
                when (position) {
                    FILTER_TODAY -> viewModel!!.setTimeFilter(getTodayRange(), position)
                    FILTER_WEEK -> viewModel!!.setTimeFilter(getWeekRange(), position)
                    FILTER_MONTH -> viewModel!!.setTimeFilter(getMonthRange(), position)
                    FILTER_SELECT -> selectDatesRange(childFragmentManager) { dates -> viewModel.setTimeFilter(dates, position) }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        val timeStepSpinner = binding.statsSpinnerTimeStep
        timeStepSpinner.adapter = object : ArrayAdapter<CharSequence>(context!!, R.layout.support_simple_spinner_dropdown_item, resources.getStringArray(R.array.stats_filter_time_step)) {
            override fun isEnabled(position: Int): Boolean {
                return if (periodSpinner.selectedItemPosition == FILTER_TODAY || periodSpinner.selectedItemPosition == FILTER_WEEK) {
                    position == TIME_STEP_DAY
                } else true
            }
        }

        activity!!.window.decorView.postDelayed({ spinnersCanClicked = true }, 500)
        periodSpinner.setSelection(viewModel.selectedTimeFilter, false)

        return view
    }

}
