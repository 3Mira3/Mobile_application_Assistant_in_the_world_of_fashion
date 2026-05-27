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
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import stu.cn.ua.mobile_application_assistant_in_the_world_of_fashion.databinding.ItemTrendBinding
import stu.cn.ua.mobile_application_assistant_in_the_world_of_fashion.network.UnsplashPhoto
import stu.cn.ua.mobile_application_assistant_in_the_world_of_fashion.network.UnsplashService
import android.widget.Toast
import stu.cn.ua.mobile_application_assistant_in_the_world_of_fashion.R

class TrendsDialogFragment : BottomSheetDialogFragment() {

    private val unsplashService = Retrofit.Builder()
        .baseUrl("https://api.unsplash.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(UnsplashService::class.java)

    private val UNSPLASH_KEY = "JnDZL7ShcsOEjN-FnI5MkDsnJLxLKu-IEvtwa03xSog"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.dialog_trends, container, false)
        val rv = root.findViewById<RecyclerView>(R.id.rv_trends)
        rv.layoutManager = LinearLayoutManager(context)

        lifecycleScope.launch {
            try {
                val response = unsplashService.searchFashionPhotos()
                if (response.results.isEmpty()) {
                    Toast.makeText(requireContext(), getString(R.string.trends_not_found), Toast.LENGTH_SHORT).show()
                }
                rv.adapter = TrendsAdapter(response.results)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), getString(R.string.trends_load_error, e.message), Toast.LENGTH_LONG).show()
            }
        }
        return root
    }

    inner class TrendsAdapter(private val photos: List<UnsplashPhoto>) : RecyclerView.Adapter<TrendsAdapter.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemTrendBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val photo = photos[position]
            Glide.with(holder.binding.ivTrend)
                .load(photo.urls.regular)
                .centerCrop()
                .into(holder.binding.ivTrend)
            
            holder.binding.tvTrendDesc.text = photo.alt_description?.replaceFirstChar { it.uppercase() } ?: getString(R.string.trends_inspiration)
            holder.binding.tvTrendAuthor.text = getString(R.string.trends_author_prefix, photo.user.name)
        }

        override fun getItemCount() = photos.size

        inner class ViewHolder(val binding: ItemTrendBinding) : RecyclerView.ViewHolder(binding.root)
    }
}
