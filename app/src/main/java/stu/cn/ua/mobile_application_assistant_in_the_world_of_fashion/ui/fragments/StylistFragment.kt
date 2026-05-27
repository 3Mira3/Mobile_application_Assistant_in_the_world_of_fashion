package stu.cn.ua.mobile_application_assistant_in_the_world_of_fashion.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import stu.cn.ua.mobile_application_assistant_in_the_world_of_fashion.R
import stu.cn.ua.mobile_application_assistant_in_the_world_of_fashion.data.FashionDataSource
import stu.cn.ua.mobile_application_assistant_in_the_world_of_fashion.data.Stylist
import stu.cn.ua.mobile_application_assistant_in_the_world_of_fashion.databinding.FragmentStylistBinding
import stu.cn.ua.mobile_application_assistant_in_the_world_of_fashion.ui.adapters.StylistAdapter

class StylistFragment : Fragment() {
    private var _binding: FragmentStylistBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: StylistAdapter
    private val stylists = FashionDataSource.stylists

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentStylistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupScrollToTop()
        updateFavoritesButton()

        // Always start from the top
        binding.recyclerStylists.post {
            binding.recyclerStylists.scrollToPosition(0)
            binding.recyclerStylists.scheduleLayoutAnimation()
            binding.fabScrollUp.hide()
        }

        binding.fabFavorites.setOnClickListener {
            toggleFavoritesOverlay()
        }

        binding.fabAllStylists.setOnClickListener {
            toggleAllStylistsOverlay()
        }

        // Animate title
        binding.textTitle.alpha = 0f
        binding.textTitle.translationY = -50f
        binding.textTitle.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(800)
            .start()
    }

    private fun toggleAllStylistsOverlay() {
        if (binding.cardAllStylistsOverlay.visibility == View.VISIBLE) {
            binding.cardAllStylistsOverlay.visibility = View.GONE
        } else {
            populateAllStylistNames()
            binding.cardAllStylistsOverlay.visibility = View.VISIBLE
            binding.cardFavoritesOverlay.visibility = View.GONE // Hide other one
        }
    }

    private fun populateAllStylistNames() {
        binding.containerAllStylistNames.removeAllViews()
        val comicSans = ResourcesCompat.getFont(requireContext(), R.font.comic_sans)
        
        stylists.forEachIndexed { index, stylist ->
            val textView = TextView(requireContext()).apply {
                text = "👤 ${getString(stylist.nameRes)}"
                textSize = 20f
                setTextColor(resources.getColor(R.color.secondary, null))
                setPadding(0, 12, 0, 12)
                typeface = comicSans
                setOnClickListener {
                    binding.recyclerStylists.smoothScrollToPosition(index)
                    binding.cardAllStylistsOverlay.visibility = View.GONE
                }
            }
            binding.containerAllStylistNames.addView(textView)
        }
    }

    private fun setupRecyclerView() {
        adapter = StylistAdapter(stylists, { stylist ->
            showFullScreenImage(stylist)
        }) {
            updateFavoritesButton()
            // No need to update names here, we update them when overlay opens
        }
        binding.recyclerStylists.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@StylistFragment.adapter
        }
    }

    private fun showFullScreenImage(stylist: Stylist) {
        val dialog = android.app.Dialog(requireContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        dialog.setContentView(R.layout.dialog_image_preview)
        
        val imageView = dialog.findViewById<android.widget.ImageView>(R.id.image_preview)
        val closeButton = dialog.findViewById<android.widget.ImageView>(R.id.image_close)
        
        imageView.setImageResource(stylist.imageRes)
        
        // Advanced pinch-to-zoom and panning implementation
        var scaleFactor = 1.0f
        var lastTouchX = 0f
        var lastTouchY = 0f
        
        val scaleDetector = android.view.ScaleGestureDetector(requireContext(), object : android.view.ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: android.view.ScaleGestureDetector): Boolean {
                scaleFactor *= detector.scaleFactor
                scaleFactor = scaleFactor.coerceIn(1.0f, 5.0f)
                imageView.scaleX = scaleFactor
                imageView.scaleY = scaleFactor
                return true
            }
        })

        imageView.setOnTouchListener { v, event ->
            scaleDetector.onTouchEvent(event)
            
            when (event.action) {
                android.view.MotionEvent.ACTION_DOWN -> {
                    lastTouchX = event.rawX
                    lastTouchY = event.rawY
                }
                android.view.MotionEvent.ACTION_MOVE -> {
                    if (scaleFactor > 1.0f) {
                        val dx = event.rawX - lastTouchX
                        val dy = event.rawY - lastTouchY
                        
                        v.translationX += dx
                        v.translationY += dy
                        
                        lastTouchX = event.rawX
                        lastTouchY = event.rawY
                    }
                }
            }
            true
        }
        
        closeButton.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun setupScrollToTop() {
        binding.recyclerStylists.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val offset = recyclerView.computeVerticalScrollOffset()
                if (offset > 600) {
                    binding.fabScrollUp.show()
                } else {
                    binding.fabScrollUp.hide()
                }
            }
        })

        binding.fabScrollUp.setOnClickListener {
            binding.recyclerStylists.smoothScrollToPosition(0)
        }
    }

    private fun updateFavoritesButton() {
        val hasLikes = stylists.any { it.isLiked }
        binding.fabFavorites.visibility = if (hasLikes) View.VISIBLE else View.GONE
        
        if (!hasLikes) {
            binding.cardFavoritesOverlay.visibility = View.GONE
        }
    }

    private fun populateFavoriteNames() {
        binding.containerFavoriteNames.removeAllViews()
        val comicSans = ResourcesCompat.getFont(requireContext(), R.font.comic_sans)
        
        stylists.forEachIndexed { index, stylist ->
            if (stylist.isLiked) {
                val textView = TextView(requireContext()).apply {
                    text = "❤ ${getString(stylist.nameRes)}"
                    textSize = 20f
                    setTextColor(resources.getColor(R.color.primary, null))
                    setPadding(0, 12, 0, 12)
                    typeface = comicSans
                    setOnClickListener {
                        binding.recyclerStylists.smoothScrollToPosition(index)
                        binding.cardFavoritesOverlay.visibility = View.GONE
                    }
                }
                binding.containerFavoriteNames.addView(textView)
            }
        }
    }

    private fun toggleFavoritesOverlay() {
        if (binding.cardFavoritesOverlay.visibility == View.VISIBLE) {
            binding.cardFavoritesOverlay.visibility = View.GONE
        } else {
            populateFavoriteNames()
            binding.cardFavoritesOverlay.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance() = StylistFragment()
    }
}
