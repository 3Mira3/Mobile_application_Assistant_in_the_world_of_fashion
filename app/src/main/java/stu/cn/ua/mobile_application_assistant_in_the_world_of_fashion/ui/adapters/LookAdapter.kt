package stu.cn.ua.mobile_application_assistant_in_the_world_of_fashion.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import stu.cn.ua.mobile_application_assistant_in_the_world_of_fashion.R
import stu.cn.ua.mobile_application_assistant_in_the_world_of_fashion.data.FashionLook

class LookAdapter(
    private var looks: List<FashionLook>,
    private val onImageClick: (FashionLook) -> Unit
) : RecyclerView.Adapter<LookAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.image_look)
        val title: TextView = view.findViewById(R.id.text_title)
        val description: TextView = view.findViewById(R.id.text_description)
        val fullDescription: TextView = view.findViewById(R.id.text_full_description)

        fun bind(look: FashionLook) {
            val context = itemView.context
            title.text = context.getString(look.titleRes)
            description.text = context.getString(look.descriptionRes)
            fullDescription.text = if (look.fullDescriptionRes != 0) context.getString(look.fullDescriptionRes) else ""
            
            // Handle expansion state
            fullDescription.visibility = if (look.isExpanded) View.VISIBLE else View.GONE
            description.visibility = if (look.isExpanded) View.GONE else View.VISIBLE
            
            // Use Glide to load local drawable resource
            Glide.with(itemView.context)
                .load(look.imageRes)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_report_image)
                .centerCrop()
                .into(image)
            
            image.setOnClickListener { onImageClick(look) }
            
            itemView.setOnClickListener {
                look.isExpanded = !look.isExpanded
                notifyItemChanged(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_fashion_look, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(looks[position])
    }

    override fun getItemCount() = looks.size

    fun updateData(newLooks: List<FashionLook>) {
        looks = newLooks
        notifyDataSetChanged()
    }
}
