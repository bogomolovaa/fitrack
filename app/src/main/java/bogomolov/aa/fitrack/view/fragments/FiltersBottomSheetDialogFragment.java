package bogomolov.aa.fitrack.view.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;

import javax.inject.Inject;

import bogomolov.aa.fitrack.R;
import bogomolov.aa.fitrack.core.DateUtils;
import bogomolov.aa.fitrack.dagger.ViewModelFactory;
import bogomolov.aa.fitrack.databinding.FiltersDialogBinding;
import bogomolov.aa.fitrack.viewmodels.StatsViewModel;
import dagger.android.support.AndroidSupportInjection;

import static bogomolov.aa.fitrack.core.DateUtils.getMonthRange;
import static bogomolov.aa.fitrack.core.DateUtils.getTodayRange;
import static bogomolov.aa.fitrack.core.DateUtils.getWeekRange;
import static bogomolov.aa.fitrack.viewmodels.StatsViewModel.FILTER_MONTH;
import static bogomolov.aa.fitrack.viewmodels.StatsViewModel.FILTER_SELECT;
import static bogomolov.aa.fitrack.viewmodels.StatsViewModel.FILTER_TODAY;
import static bogomolov.aa.fitrack.viewmodels.StatsViewModel.FILTER_WEEK;
import static bogomolov.aa.fitrack.viewmodels.StatsViewModel.TIME_STEP_DAY;

public class FiltersBottomSheetDialogFragment extends BottomSheetDialogFragment {
    private StatsViewModel viewModel;
    private boolean spinnersCanClicked;
    @Inject
    ViewModelFactory viewModelFactory;

    @Override
    public void onAttach(Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        viewModel = ViewModelProviders.of(getParentFragment(), viewModelFactory).get(StatsViewModel.class);
        FiltersDialogBinding binding = DataBindingUtil.inflate(inflater, R.layout.filters_dialog, container, false);
        binding.setLifecycleOwner(this);
        View view = binding.getRoot();
        binding.setViewModel(viewModel);

        Spinner periodSpinner = view.findViewById(R.id.stats_spinner_period);

        periodSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!spinnersCanClicked) return;
                switch (position) {
                    case FILTER_TODAY:
                        viewModel.setTimeFilter(getTodayRange(), position);
                        break;
                    case FILTER_WEEK:
                        viewModel.setTimeFilter(getWeekRange(), position);
                        break;
                    case FILTER_MONTH:
                        viewModel.setTimeFilter(getMonthRange(), position);
                        break;
                    case FILTER_SELECT:
                        DateUtils.selectDatesRange(getChildFragmentManager(), dates -> {
                            viewModel.setTimeFilter(dates, position);
                        });
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        Spinner timeStepSpinner = view.findViewById(R.id.stats_spinner_time_step);
        timeStepSpinner.setAdapter(new ArrayAdapter<CharSequence>(getContext(), R.layout.support_simple_spinner_dropdown_item, getResources().getStringArray(R.array.stats_filter_time_step)) {
            @Override
            public boolean isEnabled(int position) {
                if (periodSpinner.getSelectedItemPosition() == FILTER_TODAY || periodSpinner.getSelectedItemPosition() == FILTER_WEEK) {
                    return position == TIME_STEP_DAY;
                }
                return true;
            }
        });


        getActivity().getWindow().getDecorView().postDelayed(() -> spinnersCanClicked = true, 500);
        periodSpinner.setSelection(viewModel.selectedTimeFilter,false);

        return view;

    }

}
