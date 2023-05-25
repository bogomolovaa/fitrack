package bogomolov.aa.fitrack.features.tracks.tags

import android.content.DialogInterface
import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.DialogFragment
import bogomolov.aa.fitrack.R
import bogomolov.aa.fitrack.databinding.FragmentTagSelectionBinding
import bogomolov.aa.fitrack.domain.model.Tag
import org.koin.androidx.viewmodel.ext.android.viewModel

class TagSelectionDialog(private val onTagSelection: (Tag?)->Unit) : DialogFragment() {
    private val viewModel: TagSelectionViewModel by viewModel()
    private var actionMode: ActionMode? = null
    private lateinit var toolbar: Toolbar
    private lateinit var listView: ListView
    private var selectedTag: Tag? = null
    private var selectedToDeleteTag: Tag? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentTagSelectionBinding.inflate(inflater, container, false)

        listView = binding.tagListView
        viewModel.tagsLiveData.observe(this) { tags ->
            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_list_item_1,
                tags.map { it.name })
            listView.adapter = adapter
        }

        toolbar = binding.tagSelectionToolbar
        toolbar.setTitle(R.string.title_select_tag)

        val closeButton = binding.tagsBackButton
        closeButton.setOnClickListener { dismiss() }

        listView.setOnItemLongClickListener { _, _, position, _ ->
            if (actionMode == null) {
                actionMode = toolbar.startActionMode(callback)
                selectedToDeleteTag = viewModel.tagsLiveData.value!![position]
            } else {
                actionMode!!.finish()
            }
            true
        }

        listView.setOnItemClickListener { _, _, position, _ ->
            if (actionMode == null) {
                selectedTag = viewModel.tagsLiveData.value?.get(position)
                dismiss()
            } else {
                selectedToDeleteTag = viewModel.tagsLiveData.value?.get(position)
            }
        }

        binding.tagNameEditLayout.setEndIconOnClickListener {
            val tagName = binding.tagNameEditText.text.toString()
            viewModel.onNewTag(tagName)
            binding.tagNameEditText.setText("")
        }

        return binding.root
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onTagSelection(selectedTag)
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        onTagSelection(null)
    }

    private val callback = object : ActionMode.Callback {

        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            mode.menuInflater.inflate(R.menu.tag_selection_menu, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu) = false

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            if (selectedToDeleteTag != null) viewModel.deleteTag(selectedToDeleteTag!!)
            actionMode?.finish()
            return true
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            actionMode = null
        }
    }
}