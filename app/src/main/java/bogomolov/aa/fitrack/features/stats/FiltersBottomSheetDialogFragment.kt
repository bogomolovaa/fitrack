package bogomolov.aa.fitrack.features.stats

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import bogomolov.aa.fitrack.R
import bogomolov.aa.fitrack.databinding.FiltersDialogBinding
import bogomolov.aa.fitrack.domain.getMonthRange
import bogomolov.aa.fitrack.domain.getTodayRange
import bogomolov.aa.fitrack.domain.getWeekRange
import bogomolov.aa.fitrack.domain.selectDatesRange
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.android.support.AndroidSupportInjection

class FiltersBottomSheetDialogFragment(
    private var viewModel: StatsViewModel
) : BottomSheetDialogFragment() {
    private var spinnersCanClicked: Boolean = false

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FiltersDialogBinding.inflate(inflater, container, false)
        val periodSpinner = binding.statsSpinnerPeriod
        periodSpinner.onSelection { id ->
            if (spinnersCanClicked)
                when (id) {
                    FILTER_TODAY -> viewModel.setTimeFilter(getTodayRange(), id)
                    FILTER_WEEK -> viewModel.setTimeFilter(getWeekRange(), id)
                    FILTER_MONTH -> viewModel.setTimeFilter(getMonthRange(), id)
                    FILTER_SELECT -> selectDatesRange(childFragmentManager) { dates ->
                        viewModel.setTimeFilter(dates, id)
                    }
                }
        }

        viewModel.tagEntries.observe(viewLifecycleOwner) {
            binding.statsSpinnerTimeStep.adapter =
                ArrayAdapter<CharSequence>(
                    requireContext(), R.layout.support_simple_spinner_dropdown_item, it
                )
        }

        binding.statsSpinnerTimeStep.adapter =
            object : ArrayAdapter<CharSequence>(
                requireContext(),
                R.layout.support_simple_spinner_dropdown_item,
                resources.getStringArray(R.array.stats_filter_time_step)
            ) {
                override fun isEnabled(position: Int): Boolean {
                    return if (periodSpinner.selectedItemPosition == FILTER_TODAY || periodSpinner.selectedItemPosition == FILTER_WEEK) {
                        position == TIME_STEP_DAY
                    } else true
                }
            }

        viewModel.tagEntries.observe(viewLifecycleOwner) {
            binding.statsSpinnerTag.adapter = ArrayAdapter<CharSequence>(
                requireContext(),
                R.layout.support_simple_spinner_dropdown_item,
                it
            )
        }

        binding.statsSpinnerParam.onSelection { viewModel.setParam(it) }
        binding.statsSpinnerTimeStep.onSelection { viewModel.setTimeStep(it) }
        binding.statsSpinnerTag.onSelection { viewModel.setTag(it) }

        requireActivity().window.decorView.postDelayed(
            { spinnersCanClicked = true }, 500
        )
        periodSpinner.setSelection(viewModel.selectedTimeFilter, false)

        return binding.root
    }

    private fun Spinner.onSelection(selected: (Int) -> Unit) {
        onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selected(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
    }

}
