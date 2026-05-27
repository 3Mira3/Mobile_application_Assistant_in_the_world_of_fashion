package stu.cn.ua.mobile_application_assistant_in_the_world_of_fashion.ui.adapters

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import stu.cn.ua.mobile_application_assistant_in_the_world_of_fashion.R
import stu.cn.ua.mobile_application_assistant_in_the_world_of_fashion.data.Stylist

class StylistAdapter(
    private var stylists: List<Stylist>,
    private val onImageClick: (Stylist) -> Unit,
    private val onLikeChanged: () -> Unit
) : RecyclerView.Adapter<StylistAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.image_stylist)
        val name: TextView = view.findViewById(R.id.text_name)
        val bio: TextView = view.findViewById(R.id.text_bio)
        val booking: TextView = view.findViewById(R.id.text_booking)
        val heart: ImageView = view.findViewById(R.id.image_heart)

        fun bind(stylist: Stylist) {
            val context = itemView.context
            name.text = context.getString(stylist.nameRes)
            bio.text = context.getString(stylist.bioRes)
            booking.text = context.getString(R.string.stylist_appointment)
            image.setImageResource(stylist.imageRes)
            
            updateHeart(stylist.isLiked)

            image.setOnClickListener { onImageClick(stylist) }

            heart.setOnClickListener {
                stylist.isLiked = !stylist.isLiked
                updateHeart(stylist.isLiked)
                onLikeChanged()
            }

            booking.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(stylist.bookingUrl))
                itemView.context.startActivity(intent)
            }
        }

        private fun updateHeart(isLiked: Boolean) {
            if (isLiked) {
                heart.setImageResource(android.R.drawable.btn_star_big_on)
                heart.setColorFilter(android.graphics.Color.RED)
            } else {
                heart.setImageResource(android.R.drawable.btn_star_big_off)
                heart.clearColorFilter()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_stylist, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(stylists[position])
    }

    override fun getItemCount() = stylists.size
}
