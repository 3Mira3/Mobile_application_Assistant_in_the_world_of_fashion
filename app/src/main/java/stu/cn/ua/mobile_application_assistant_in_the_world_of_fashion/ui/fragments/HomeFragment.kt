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
import com.bumptech.glide.Glide
import stu.cn.ua.mobile_application_assistant_in_the_world_of_fashion.network.TrendFetcher
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!



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
                val trends = TrendFetcher.fetchTrends()
                if (trends.isNotEmpty()) {
                    val trend = trends.first()
                    if (trend.imageUrl != null) {
                        Glide.with(this@HomeFragment)
                            .load(trend.imageUrl)
                            .centerCrop()
                            .into(binding.ivTrendMain)
                    }
                    binding.tvTrendDesc.text = trend.title
                } else {
                    binding.tvTrendDesc.text = getString(R.string.home_trend_default)
                }
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
