package bogomolov.aa.fitrack.view.fragments

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.ActionMode
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.ListView
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe

import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

import javax.inject.Inject

import bogomolov.aa.fitrack.R
import bogomolov.aa.fitrack.dagger.ViewModelFactory
import bogomolov.aa.fitrack.databinding.FragmentTagSelectionBinding
import bogomolov.aa.fitrack.core.model.Tag
import bogomolov.aa.fitrack.view.TagResultListener
import bogomolov.aa.fitrack.viewmodels.TagSelectionViewModel
import dagger.android.support.AndroidSupportInjection

class TagSelectionDialog : DialogFragment() {
    private var actionMode: ActionMode? = null
    private lateinit var toolbar: Toolbar
    private lateinit var listView: ListView

    private var selectedTag: Tag? = null
    private var selectedToDeleteTag: Tag? = null
    var tagResultListener: TagResultListener? = null

    private lateinit var viewModel: TagSelectionViewModel

    @Inject
    internal lateinit  var viewModelFactory: ViewModelFactory


    private val callback = object : ActionMode.Callback {

        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            mode.menuInflater.inflate(R.menu.tag_selection_menu, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            return false
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            if (selectedToDeleteTag != null) viewModel.deleteTag(selectedToDeleteTag!!)
            actionMode!!.finish()
            return true
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            actionMode = null
        }

    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        viewModel = ViewModelProvider(this, viewModelFactory).get(TagSelectionViewModel::class.java)
        val binding = DataBindingUtil.inflate<FragmentTagSelectionBinding>(inflater, R.layout.fragment_tag_selection, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        val view = binding.root


        listView = binding.tagListView
        viewModel.tagsLiveData.observe(this) { tags ->
            val adapter = ArrayAdapter<Tag>(context!!, android.R.layout.simple_list_item_1, tags)
            listView.adapter = adapter
        }

        toolbar = binding.tagSelectionToolbar
        toolbar.setTitle(R.string.title_select_tag)

        val closeButton = binding.tagsBackButton
        closeButton.setOnClickListener { v -> dismiss() }

        listView.setOnItemLongClickListener { adapterView: AdapterView<*>, v: View, position: Int, l: Long ->
            if (actionMode == null) {
                actionMode = toolbar.startActionMode(callback)
                selectedToDeleteTag = viewModel.tagsLiveData.value!![position]
            } else {
                actionMode!!.finish()
            }
            true
        }

        listView.setOnItemClickListener { adapterView: AdapterView<*>, v: View, position: Int, l: Long ->
            if (actionMode == null) {
                selectedTag = listView.getItemAtPosition(position) as Tag
                dismiss()
            } else {
                selectedToDeleteTag = viewModel.tagsLiveData.value!![position]
            }
        }


        val textInputAddTag = binding.tagNameEditLayout
        val textAddTag = binding.tagNameEditText
        textInputAddTag.setEndIconOnClickListener { v ->
            viewModel.onNewTag()
            textAddTag.setText("")
        }

        return view
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if (tagResultListener != null) tagResultListener!!.onTagSelectionResult(selectedTag)
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        if (tagResultListener != null) tagResultListener!!.onTagSelectionResult(null)
    }

}
