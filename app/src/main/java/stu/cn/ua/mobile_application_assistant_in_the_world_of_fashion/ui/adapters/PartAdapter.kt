package stu.cn.ua.mobile_application_assistant_in_the_world_of_fashion.ui.adapters

import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import stu.cn.ua.mobile_application_assistant_in_the_world_of_fashion.R
import stu.cn.ua.mobile_application_assistant_in_the_world_of_fashion.data.ClothingPart

class PartAdapter(
    private val parts: List<ClothingPart>,
    private val onPartClick: (ClothingPart) -> Unit
) : RecyclerView.Adapter<PartAdapter.ViewHolder>() {

    private var selectedPosition = 0

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.text_part_name)

        fun bind(part: ClothingPart, position: Int) {
            textView.text = itemView.context.getString(part.nameRes)
            
            if (position == selectedPosition) {
                textView.setBackgroundResource(R.drawable.bg_neon_card_selected)
                textView.setTextColor(ContextCompat.getColor(itemView.context, R.color.primary))
                textView.animate().translationZ(8f).setDuration(200).start()
            } else {
                textView.setBackgroundResource(R.drawable.bg_neon_card)
                textView.setTextColor(ContextCompat.getColor(itemView.context, R.color.primary))
                textView.animate().translationZ(0f).setDuration(200).start()
            }

            // Enhanced Interaction Animation
            itemView.setOnHoverListener { v, event ->
                when (event.action) {
                    MotionEvent.ACTION_HOVER_ENTER -> {
                        v.animate()
                            .scaleX(1.15f)
                            .scaleY(1.15f)
                            .translationZ(15f)
                            .setDuration(250)
                            .start()
                        true
                    }
                    MotionEvent.ACTION_HOVER_EXIT -> {
                        v.animate()
                            .scaleX(1.0f)
                            .scaleY(1.0f)
                            .translationZ(if (position == selectedPosition) 6f else 0f)
                            .setDuration(250)
                            .start()
                        true
                    }
                    else -> false
                }
            }

            itemView.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    val oldPos = selectedPosition
                    selectedPosition = adapterPosition
                    
                    it.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100).withEndAction {
                        it.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start()
                    }.start()

                    notifyItemChanged(oldPos)
                    notifyItemChanged(selectedPosition)
                    onPartClick(part)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_part, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(parts[position], position)
    }

    override fun getItemCount() = parts.size

    fun updateSelectedId(id: String): Int {
        val oldPos = selectedPosition
        selectedPosition = parts.indexOfFirst { it.id == id }
        if (selectedPosition == -1) selectedPosition = 0
        
        notifyItemChanged(oldPos)
        notifyItemChanged(selectedPosition)
        return selectedPosition
    }
}
