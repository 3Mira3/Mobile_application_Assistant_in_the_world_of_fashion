package stu.cn.ua.mobile_application_assistant_in_the_world_of_fashion.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import stu.cn.ua.mobile_application_assistant_in_the_world_of_fashion.R
import stu.cn.ua.mobile_application_assistant_in_the_world_of_fashion.data.FashionDataSource
import stu.cn.ua.mobile_application_assistant_in_the_world_of_fashion.data.FashionLook
import stu.cn.ua.mobile_application_assistant_in_the_world_of_fashion.databinding.FragmentHistoryBinding
import stu.cn.ua.mobile_application_assistant_in_the_world_of_fashion.ui.adapters.CenturyAdapter
import stu.cn.ua.mobile_application_assistant_in_the_world_of_fashion.ui.adapters.LookAdapter
import stu.cn.ua.mobile_application_assistant_in_the_world_of_fashion.ui.adapters.PartAdapter

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var centuryAdapter: CenturyAdapter
    private lateinit var partAdapter: PartAdapter
    private lateinit var partVerticalAdapter: PartAdapter
    private lateinit var lookAdapter: LookAdapter
    private var isPartsVertical = false

    private var selectedCenturyId = FashionDataSource.centuries[0].id
    private var selectedPartId = FashionDataSource.parts[0].id

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupDrawerListener()
        setupMenu()
        setupCenturies()
        setupParts()
        setupLooks()
        setupHoverDetector()
        setupPartsToggle()
        setupScrollToTop()
        
        filterData(animate = false)
        
        // Always start from the top
        binding.recyclerOutfits.post {
            binding.recyclerOutfits.scrollToPosition(0)
            binding.fabScrollUp.hide()
        }
        
        // Initial animation for the whole screen
        binding.appBar.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.fall_down))
        binding.recyclerParts.scheduleLayoutAnimation()
    }

    private fun setupDrawerListener() {
        binding.drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerOpened(drawerView: View) {
                // When drawer opens, animate the list of centuries
                binding.recyclerCenturies.scheduleLayoutAnimation()
            }
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}
            override fun onDrawerClosed(drawerView: View) {}
            override fun onDrawerStateChanged(newState: Int) {}
        })
    }

    private fun setupMenu() {
        binding.imageMenu.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupHoverDetector() {
        binding.viewEdgeDetector.apply {
            setOnHoverListener { _, event ->
                when (event.action) {
                    MotionEvent.ACTION_HOVER_ENTER, MotionEvent.ACTION_HOVER_MOVE -> {
                        if (!binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                            binding.drawerLayout.openDrawer(GravityCompat.START)
                        }
                        true
                    }
                    else -> false
                }
            }
            setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE) {
                    if (!binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                        binding.drawerLayout.openDrawer(GravityCompat.START)
                    }
                    true
                } else false
            }
        }
    }

    private fun setupCenturies() {
        centuryAdapter = CenturyAdapter(FashionDataSource.centuries) { century ->
            selectedCenturyId = century.id
            filterData()
        }
        binding.recyclerCenturies.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = centuryAdapter
        }
    }

    private fun setupParts() {
        val onPartClick: (stu.cn.ua.mobile_application_assistant_in_the_world_of_fashion.data.ClothingPart) -> Unit = { part ->
            selectedPartId = part.id
            val pos = partAdapter.updateSelectedId(part.id)
            partVerticalAdapter.updateSelectedId(part.id)
            
            // Auto-scroll the horizontal list to the selected item
            binding.recyclerParts.smoothScrollToPosition(pos)
            
            filterData()
        }

        partAdapter = PartAdapter(FashionDataSource.parts, onPartClick)
        binding.recyclerParts.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
            adapter = partAdapter
        }

        partVerticalAdapter = PartAdapter(FashionDataSource.parts, onPartClick)
        binding.recyclerPartsVertical.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = partVerticalAdapter
        }
    }

    private fun setupScrollToTop() {
        binding.recyclerOutfits.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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
            binding.recyclerOutfits.smoothScrollToPosition(0)
        }
    }

    private fun setupLooks() {
        lookAdapter = LookAdapter(emptyList()) { look ->
            showFullScreenImage(look)
        }
        binding.recyclerOutfits.apply {
            layoutManager = GridLayoutManager(requireContext(), 1)
            adapter = lookAdapter
        }
    }

    private fun setupPartsToggle() {
        binding.imagePartsListFixed.setOnClickListener {
            togglePartsOverlay()
        }
    }

    private fun togglePartsOverlay() {
        isPartsVertical = !isPartsVertical
        if (isPartsVertical) {
            binding.cardPartsOverlay.visibility = View.VISIBLE
            binding.cardPartsOverlay.alpha = 0f
            binding.cardPartsOverlay.animate().alpha(1f).setDuration(200).start()
            binding.imagePartsListFixed.animate().rotation(0f).start()
        } else {
            binding.cardPartsOverlay.animate().alpha(0f).setDuration(200).withEndAction {
                binding.cardPartsOverlay.visibility = View.GONE
            }.start()
            binding.imagePartsListFixed.animate().rotation(90f).start()
        }
    }

    private fun showFullScreenImage(look: FashionLook) {
        val dialog = android.app.Dialog(requireContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        dialog.setContentView(R.layout.dialog_image_preview)
        
        val imageView = dialog.findViewById<ImageView>(R.id.image_preview)
        val closeButton = dialog.findViewById<ImageView>(R.id.image_close)
        
        imageView.setImageResource(look.imageRes)
        
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

    private fun filterData(animate: Boolean = true) {
        val filtered = FashionDataSource.looks.filter { look ->
            look.centuryId == selectedCenturyId && (selectedPartId == "all" || look.partId == selectedPartId)
        }
        lookAdapter.updateData(filtered)
        
        if (animate) {
            binding.recyclerOutfits.scheduleLayoutAnimation()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
