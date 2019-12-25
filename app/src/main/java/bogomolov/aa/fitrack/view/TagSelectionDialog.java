package bogomolov.aa.fitrack.view;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProviders;

import javax.inject.Inject;

import bogomolov.aa.fitrack.R;
import bogomolov.aa.fitrack.dagger.ViewModelFactory;
import bogomolov.aa.fitrack.databinding.FragmentTagSelectionBinding;
import bogomolov.aa.fitrack.model.Tag;
import bogomolov.aa.fitrack.viewmodels.TagSelectionViewModel;
import dagger.android.support.AndroidSupportInjection;

public class TagSelectionDialog extends DialogFragment {
    private ActionMode actionMode;
    private Toolbar toolbar;
    private ListView listView;

    private Tag selectedTag;
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
        FragmentTagSelectionBinding viewBinding = DataBindingUtil.inflate(inflater,R.layout.fragment_tag_selection,container,false);
        viewBinding.setViewModel(viewModel);
        viewBinding.setLifecycleOwner(this);
        View view = viewBinding.getRoot();

        listView = view.findViewById(R.id.tag_list_view);
        ArrayAdapter<Tag> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, viewModel.getTags());
        listView.setAdapter(adapter);
        viewModel.tagsLiveData.observe(this, (tags) -> adapter.notifyDataSetChanged());

        toolbar = view.findViewById(R.id.tag_selection_toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (actionMode == null) {
                    actionMode = toolbar.startActionMode(callback);
                } else {
                    actionMode.finish();
                }
                return true;
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                selectedTag = (Tag) listView.getItemAtPosition(position);
                dismiss();
            }
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
            Tag tag = (Tag) listView.getSelectedItem();
            viewModel.deleteTag(tag);
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
