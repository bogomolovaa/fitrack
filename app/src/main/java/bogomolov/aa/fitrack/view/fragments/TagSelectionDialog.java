package bogomolov.aa.fitrack.view.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import javax.inject.Inject;

import bogomolov.aa.fitrack.R;
import bogomolov.aa.fitrack.dagger.ViewModelFactory;
import bogomolov.aa.fitrack.databinding.FragmentTagSelectionBinding;
import bogomolov.aa.fitrack.core.model.Tag;
import bogomolov.aa.fitrack.view.TagResultListener;
import bogomolov.aa.fitrack.viewmodels.TagSelectionViewModel;
import dagger.android.support.AndroidSupportInjection;

public class TagSelectionDialog extends DialogFragment {
    private ActionMode actionMode;
    private Toolbar toolbar;
    private ListView listView;

    private Tag selectedTag;
    private Tag selectedToDeleteTag;
    private TagResultListener tagResultListener;

    private TagSelectionViewModel viewModel;

    @Inject
    ViewModelFactory viewModelFactory;

    @Override
    public void onAttach(@NonNull Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(TagSelectionViewModel.class);
        FragmentTagSelectionBinding viewBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_tag_selection, container, false);
        viewBinding.setViewModel(viewModel);
        viewBinding.setLifecycleOwner(this);
        View view = viewBinding.getRoot();


        listView = view.findViewById(R.id.tag_list_view);
        viewModel.tagsLiveData.observe(this, (tags) -> {
            ArrayAdapter<Tag> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, tags);
            listView.setAdapter(adapter);
        });

        toolbar = view.findViewById(R.id.tag_selection_toolbar);
        toolbar.setTitle(R.string.title_select_tag);

        ImageView closeButton = view.findViewById(R.id.tags_back_button);
        closeButton.setOnClickListener(v -> dismiss());

        listView.setOnItemLongClickListener((AdapterView<?> adapterView, View v, int position, long l) -> {
            if (actionMode == null) {
                actionMode = toolbar.startActionMode(callback);
                selectedToDeleteTag = viewModel.tagsLiveData.getValue().get(position);
            } else {
                actionMode.finish();
            }
            return true;
        });

        listView.setOnItemClickListener((AdapterView<?> adapterView, View v, int position, long l) -> {
            if (actionMode == null) {
                selectedTag = (Tag) listView.getItemAtPosition(position);
                dismiss();
            } else {
                selectedToDeleteTag = viewModel.tagsLiveData.getValue().get(position);
            }
        });


        TextInputLayout textInputAddTag = view.findViewById(R.id.tag_name_edit_layout);
        TextInputEditText textAddTag = view.findViewById(R.id.tag_name_edit_text);
        textInputAddTag.setEndIconOnClickListener(v -> {
            viewModel.onNewTag();
            textAddTag.setText("");
        });

        return view;
    }

    public void setTagResultListener(TagResultListener tagResultListener) {
        this.tagResultListener = tagResultListener;
    }


    private ActionMode.Callback callback = new ActionMode.Callback() {

        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.tag_selection_menu, menu);
            return true;
        }

        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            if (selectedToDeleteTag != null) viewModel.deleteTag(selectedToDeleteTag);
            actionMode.finish();
            return true;
        }

        public void onDestroyActionMode(ActionMode mode) {
            actionMode = null;
        }

    };


    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (tagResultListener != null) tagResultListener.onTagSelectionResult(selectedTag);
    }

    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        if (tagResultListener != null) tagResultListener.onTagSelectionResult(null);
    }

}
