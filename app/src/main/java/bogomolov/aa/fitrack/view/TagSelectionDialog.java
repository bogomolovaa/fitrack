package bogomolov.aa.fitrack.view;

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
import android.widget.EditText;
import android.widget.ListView;

import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;

import java.util.ArrayList;
import java.util.List;

import bogomolov.aa.fitrack.R;
import bogomolov.aa.fitrack.model.DbProvider;
import bogomolov.aa.fitrack.model.Tag;

public class TagSelectionDialog extends DialogFragment {
    private ActionMode actionMode;
    private Toolbar toolbar;
    private ListView listView;
    private DbProvider dbProvider;
    private Tag selectedTag;
    private TagResultListener tagResultListener;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tag_selection, null);
        dbProvider = new DbProvider(false);
        listView = view.findViewById(R.id.tag_list_view);
        List<Tag> tags = new ArrayList<>(dbProvider.getTags());
        ArrayAdapter<Tag> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, tags);
        listView.setAdapter(adapter);
        EditText tagNameEditText = view.findViewById(R.id.tag_name_edit_text);
        AppCompatImageButton addButton = view.findViewById(R.id.tag_add_button);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String tagName = tagNameEditText.getText().toString();
                Tag tag = new Tag(tagName);
                tag = dbProvider.addTag(tag);
                tags.add(tag);
                adapter.notifyDataSetChanged();
            }
        });
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        dbProvider.close();
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
            dbProvider.deleteTag(tag);
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
