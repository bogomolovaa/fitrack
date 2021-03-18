package bogomolov.aa.fitrack.features.shared

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.AdapterView
import android.widget.Spinner

@SuppressLint("AppCompatCustomView")
class ReselectableSpinner(context: Context, attrs: AttributeSet) : Spinner(context, attrs) {
    internal var listener: OnItemSelectedListener? = null

    override fun setSelection(position: Int) {
        super.setSelection(position)
        listener?.onItemSelected(null, null, position, 0)
    }

    override fun setOnItemSelectedListener(listener: OnItemSelectedListener?) {
        this.listener = listener
    }
}

fun Spinner.onSelection(selected: (Int) -> Unit) {
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