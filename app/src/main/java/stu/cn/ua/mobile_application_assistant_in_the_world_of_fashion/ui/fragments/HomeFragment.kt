package stu.cn.ua.mobile_application_assistant_in_the_world_of_fashion.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import android.widget.Toast
import stu.cn.ua.mobile_application_assistant_in_the_world_of_fashion.R
import stu.cn.ua.mobile_application_assistant_in_the_world_of_fashion.databinding.FragmentHomeBinding
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.bumptech.glide.Glide
import stu.cn.ua.mobile_application_assistant_in_the_world_of_fashion.network.UnsplashService

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val unsplashService = Retrofit.Builder()
        .baseUrl("https://api.unsplash.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(UnsplashService::class.java)

    private val UNSPLASH_KEY = "JnDZL7ShcsOEjN-FnI5MkDsnJLxLKu-IEvtwa03xSog"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupNavigation()
        loadMainTrend()
    }

    private fun loadMainTrend() {
        lifecycleScope.launch {
            try {
                val photo = unsplashService.getRandomFashionPhoto()
                Glide.with(this@HomeFragment)
                    .load(photo.urls.regular)
                    .centerCrop()
                    .into(binding.ivTrendMain)
                
                binding.tvTrendDesc.text = photo.alt_description?.replaceFirstChar { it.uppercase() } ?: getString(R.string.home_trend_default)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), getString(R.string.trends_load_error, e.message), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setupNavigation() {
        binding.cardFeatured.setOnClickListener {
            TrendsDialogFragment().show(childFragmentManager, "trends")
        }
        
        binding.cardHistory.setOnClickListener {
            findNavController().navigate(R.id.navigation_history)
        }
        binding.cardMixer.setOnClickListener {
            findNavController().navigate(R.id.navigation_mixer)
        }
        binding.cardStylists.setOnClickListener {
            findNavController().navigate(R.id.navigation_stylists)
        }
        binding.cardColors.setOnClickListener {
            findNavController().navigate(R.id.navigation_colors)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
