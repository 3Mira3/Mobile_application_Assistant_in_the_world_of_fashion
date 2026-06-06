package stu.cn.ua.mobile_application_assistant_in_the_world_of_fashion.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import stu.cn.ua.mobile_application_assistant_in_the_world_of_fashion.databinding.ItemTrendBinding
import android.widget.Toast
import stu.cn.ua.mobile_application_assistant_in_the_world_of_fashion.R
import stu.cn.ua.mobile_application_assistant_in_the_world_of_fashion.network.TrendFetcher
import stu.cn.ua.mobile_application_assistant_in_the_world_of_fashion.network.TrendItem

class TrendsDialogFragment : BottomSheetDialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.dialog_trends, container, false)
        val rv = root.findViewById<RecyclerView>(R.id.rv_trends)
        rv.layoutManager = LinearLayoutManager(context)

        lifecycleScope.launch {
            while (true) {
                try {
                    val trends = TrendFetcher.fetchTrends()
                    if (trends.isEmpty() && rv.adapter == null) {
                        Toast.makeText(requireContext(), getString(R.string.trends_not_found), Toast.LENGTH_SHORT).show()
                    }
                    if (trends.isNotEmpty()) {
                        rv.adapter = TrendsAdapter(trends)
                    }
                } catch (e: Exception) {
                    if (rv.adapter == null) {
                        Toast.makeText(requireContext(), getString(R.string.trends_load_error, e.message), Toast.LENGTH_LONG).show()
                    }
                }
                kotlinx.coroutines.delay(60000) // auto update every 60 seconds
            }
        }
        return root
    }

    inner class TrendsAdapter(private val trends: List<TrendItem>) : RecyclerView.Adapter<TrendsAdapter.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemTrendBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val trend = trends[position]
            if (trend.imageUrl != null) {
                Glide.with(holder.binding.ivTrend)
                    .load(trend.imageUrl)
                    .centerCrop()
                    .into(holder.binding.ivTrend)
            }
            
            holder.binding.tvTrendDesc.text = trend.title
            holder.binding.tvTrendAuthor.text = trend.date
        }

        override fun getItemCount() = trends.size

        inner class ViewHolder(val binding: ItemTrendBinding) : RecyclerView.ViewHolder(binding.root)
    }
}
