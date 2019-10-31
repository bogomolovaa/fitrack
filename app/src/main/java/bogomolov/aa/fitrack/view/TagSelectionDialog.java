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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;

import java.util.ArrayList;
import java.util.List;

import bogomolov.aa.fitrack.R;

public class TagSelectionDialog extends DialogFragment {
    private ActionMode actionMode;
    private Toolbar toolbar;


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().setTitle("Title!");
        View view = inflater.inflate(R.layout.fragment_tag_selection, null);
        final ListView listView = view.findViewById(R.id.tag_list_view);
        final List<String> tags = new ArrayList<>();
        for (int i = 1; i <= 3; i++) tags.add("" + i);
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_single_choice, tags);
        listView.setAdapter(adapter);
        final EditText tagNameEditText = view.findViewById(R.id.tag_name_edit_text);
        Button addButton = view.findViewById(R.id.tag_add_button);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String tagName = tagNameEditText.getText().toString();
                Log.i("test", "add tag " + tagName);
                tags.add(tagName);
                adapter.notifyDataSetChanged();
            }
        });
        toolbar = view.findViewById(R.id.tag_selection_toolbar);

        /*
        toolbar.setNavigationIcon(android.R.drawable.);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        */


        listView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (actionMode == null) {
                    actionMode = toolbar.startActionMode(callback);
                } else {
                    actionMode.finish();
                }
                int selected = listView.getSelectedItemPosition();
                Log.i("test", "long selected " + selected);
                return true;
            }
        });

        listView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int selected = listView.getSelectedItemPosition();
                Log.i("test", "selected " + selected);
                dismiss();
            }
        });
        return view;
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
            Log.d("test", "item " + item.getTitle());


            return false;
        }

        public void onDestroyActionMode(ActionMode mode) {
            actionMode = null;
        }

    };


    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        Log.d("test", "Dialog 1: onDismiss");
    }

    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        Log.d("test", "Dialog 1: onCancel");
    }

}
