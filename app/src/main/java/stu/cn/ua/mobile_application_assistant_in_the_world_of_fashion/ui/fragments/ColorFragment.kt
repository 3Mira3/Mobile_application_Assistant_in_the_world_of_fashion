package stu.cn.ua.mobile_application_assistant_in_the_world_of_fashion.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import stu.cn.ua.mobile_application_assistant_in_the_world_of_fashion.R
import stu.cn.ua.mobile_application_assistant_in_the_world_of_fashion.databinding.FragmentColorBinding

class ColorFragment : Fragment() {
    private var _binding: FragmentColorBinding? = null
    private val binding get() = _binding!!

    companion object {
        private var lastVerdict: String? = null
        private var lastExplanation: String? = null
        private var lastAdvice: String? = null
        private val savedSelectedNames = mutableSetOf<String>()

        @JvmStatic
        fun newInstance() = ColorFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentColorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.scrollView.post {
            binding.scrollView.scrollTo(0, 0)
        }

        // Restore last results button if data exists but card is hidden
        if (lastVerdict != null) {
            binding.btnShowLastResult.visibility = View.VISIBLE
            binding.textResultVerdict.text = lastVerdict
            binding.textResultExplanation.text = lastExplanation
            val lastAdviceStr = lastAdvice ?: ""
            val formattedAdvice = lastAdviceStr
                .replace(Regex("\\*\\*(.*?)\\*\\*"), "<b>$1</b>")
                .replace("\n", "<br>")
            binding.textResultAdvice.text = androidx.core.text.HtmlCompat.fromHtml(formattedAdvice, androidx.core.text.HtmlCompat.FROM_HTML_MODE_COMPACT)
        }

        binding.btnShowLastResult.setOnClickListener {
            binding.cardDiagnosticResult.visibility = View.VISIBLE
            binding.btnShowLastResult.visibility = View.GONE
        }

        binding.btnCloseResults.setOnClickListener {
            binding.cardDiagnosticResult.visibility = View.GONE
            binding.btnShowLastResult.visibility = View.VISIBLE
            // We KEEP the data in memory, just hide the UI
        }

        // Set the generated image
        binding.imagePalette.setImageResource(R.drawable.color_wheel_fashion)

        // Title animation
        binding.textTitle.alpha = 0f
        binding.textTitle.translationY = -50f
        binding.textTitle.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(800)
            .start()

        // Full screen click
        binding.cardPalette.setOnClickListener {
            showFullScreenImage()
        }

        // Toggle Theory (Custom Button)
        binding.btnToggleTheory.setOnClickListener {
            if (binding.layoutTheory.visibility == View.VISIBLE) {
                binding.layoutTheory.visibility = View.GONE
                binding.textBtnTheory.text = "Показати теорію"
                binding.iconTheory.setImageResource(android.R.drawable.ic_secure)
            } else {
                binding.layoutTheory.visibility = View.VISIBLE
                binding.textBtnTheory.text = "Приховати теорію"
                binding.iconTheory.setImageResource(android.R.drawable.ic_menu_view)
            }
        }

        colorizeText()
        setupColorDiagnostic()
    }

    private val selectedColors = mutableSetOf<FashionColor>()
    private val allColors = listOf(
        FashionColor("red", "#FF0000", 0, R.string.color_name_red, R.string.color_tip_red),
        FashionColor("orange", "#FF8C00", 30, R.string.color_name_orange, R.string.color_tip_orange),
        FashionColor("yellow", "#FFD700", 60, R.string.color_name_yellow, R.string.color_tip_yellow),
        FashionColor("lime", "#ADFF2F", 90, R.string.color_name_lime, R.string.color_tip_lime),
        FashionColor("green", "#008000", 120, R.string.color_name_green, R.string.color_tip_green),
        FashionColor("emerald", "#50C878", 150, R.string.color_name_emerald, R.string.color_tip_emerald),
        FashionColor("cyan", "#00BFFF", 180, R.string.color_name_cyan, R.string.color_tip_cyan),
        FashionColor("blue", "#0000FF", 210, R.string.color_name_blue, R.string.color_tip_blue),
        FashionColor("indigo", "#4B0082", 240, R.string.color_name_indigo, R.string.color_tip_indigo),
        FashionColor("violet", "#800080", 270, R.string.color_name_violet, R.string.color_tip_violet),
        FashionColor("magenta", "#FF00FF", 300, R.string.color_name_magenta, R.string.color_tip_magenta),
        FashionColor("pink", "#FF69B4", 330, R.string.color_name_pink, R.string.color_tip_pink),
        FashionColor("white", "#FFFFFF", -1, R.string.color_name_white, R.string.color_tip_white),
        FashionColor("black", "#000000", -2, R.string.color_name_black, R.string.color_tip_black),
        FashionColor("grey", "#808080", -3, R.string.color_name_grey, R.string.color_tip_grey),
        FashionColor("brown", "#8B4513", -4, R.string.color_name_brown, R.string.color_tip_brown),
        FashionColor("beige", "#F5F5DC", -5, R.string.color_name_beige, R.string.color_tip_beige),
        FashionColor("coffee", "#6F4E37", -6, R.string.color_name_coffee, R.string.color_tip_coffee)
    )

    private fun setupColorDiagnostic() {
        allColors.forEach { color ->
            val colorView = createColorCircle(color)
            binding.gridColors.addView(colorView)
        }

        binding.btnRunDiagnostic.setOnClickListener {
            runDiagnostic()
        }
    }

    private fun createColorCircle(color: FashionColor): View {
        val size = (48 * resources.displayMetrics.density).toInt()
        val margin = (8 * resources.displayMetrics.density).toInt()
        
        val card = com.google.android.material.card.MaterialCardView(requireContext()).apply {
            layoutParams = ViewGroup.MarginLayoutParams(size, size).apply {
                setMargins(margin, margin, margin, margin)
            }
            radius = size / 2f
            setCardBackgroundColor(android.graphics.Color.parseColor(color.hex))
            
            // RESTORE SELECTION
            if (savedSelectedNames.contains(color.id)) {
                selectedColors.add(color)
                strokeWidth = (4 * resources.displayMetrics.density).toInt()
            } else {
                strokeWidth = 0
            }

            strokeColor = android.graphics.Color.parseColor("#BC13FE")
            tag = color.id 
            isClickable = true
            isFocusable = true
            
            setOnClickListener {
                toggleSelection(this, color)
            }
            
            setOnLongClickListener {
                showShadesPicker(color, this)
                true
            }
        }
        return card
    }

    private fun toggleSelection(card: com.google.android.material.card.MaterialCardView, color: FashionColor) {
        val existing = selectedColors.find { it.id == color.id }
        if (existing != null) {
            selectedColors.remove(existing)
            savedSelectedNames.remove(color.id)
            card.strokeWidth = 0
        } else {
            if (selectedColors.size < 3) {
                selectedColors.add(color)
                savedSelectedNames.add(color.id)
                card.strokeWidth = (4 * resources.displayMetrics.density).toInt()
            }
        }
    }

    private fun showShadesPicker(baseColor: FashionColor, targetCard: com.google.android.material.card.MaterialCardView) {
        val bottomSheet = com.google.android.material.bottomsheet.BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.dialog_shades_picker, null)
        val container = view.findViewById<android.widget.LinearLayout>(R.id.container_shades)
        
        val shades = generateShades(baseColor.hex)
        shades.forEach { hex ->
            val shadeView = createColorCircle(baseColor.copy(hex = hex)).apply {
                setOnClickListener {
                    val newColor = baseColor.copy(hex = hex)
                    targetCard.setCardBackgroundColor(android.graphics.Color.parseColor(hex))
                    
                    // Auto-select if not selected
                    if (!selectedColors.any { it.id == baseColor.id }) {
                        toggleSelection(targetCard, newColor)
                    } else {
                        // Update current selection with new shade
                        selectedColors.removeIf { it.id == baseColor.id }
                        selectedColors.add(newColor)
                        targetCard.strokeWidth = (4 * resources.displayMetrics.density).toInt()
                    }
                    bottomSheet.dismiss()
                }
            }
            container.addView(shadeView)
        }
        
        bottomSheet.setContentView(view)
        bottomSheet.show()
    }

    private fun generateShades(baseHex: String): List<String> {
        val color = android.graphics.Color.parseColor(baseHex)
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(color, hsv)
        
        return listOf(
            colorToHex(android.graphics.Color.HSVToColor(floatArrayOf(hsv[0], hsv[1] * 0.2f, 0.98f))),
            colorToHex(android.graphics.Color.HSVToColor(floatArrayOf(hsv[0], hsv[1] * 0.5f, 0.90f))),
            baseHex,
            colorToHex(android.graphics.Color.HSVToColor(floatArrayOf(hsv[0], hsv[1], 0.7f))),
            colorToHex(android.graphics.Color.HSVToColor(floatArrayOf(hsv[0], hsv[1], 0.3f)))
        )
    }

    private fun colorToHex(color: Int): String {
        return String.format("#%06X", 0xFFFFFF and color)
    }

    private fun runDiagnostic() {
        if (selectedColors.size < 2) {
            android.widget.Toast.makeText(requireContext(), getString(R.string.color_diag_min_colors), android.widget.Toast.LENGTH_SHORT).show()
            return
        }

        val colors = selectedColors.toList()
        val angles = colors.filter { it.angle >= 0 }.map { it.angle }.sorted()
        
        var verdict = ""
        var explanation = ""
        var advice = StringBuilder()

        val names = colors.map { it.id }
        val hasBlack = names.contains("black")
        val hasGrey = names.contains("grey")
        val hasWhite = names.contains("white")

        when {
            hasBlack && hasGrey && colors.size == 2 -> {
                verdict = getString(R.string.color_verdict_urban_gothic)
                explanation = getString(R.string.color_exp_urban_gothic)
                advice.append(getString(R.string.color_adv_urban_gothic))
            }
            hasWhite && names.contains("pink") && colors.size == 2 -> {
                verdict = getString(R.string.color_verdict_soft_fresh)
                explanation = getString(R.string.color_exp_soft_fresh)
                advice.append(getString(R.string.color_adv_soft_fresh))
            }
            colors.any { it.angle < 0 } && colors.size == 2 -> {
                val neutral = colors.find { it.angle < 0 }!!
                val main = colors.find { it.angle >= 0 }
                if (main != null) {
                    verdict = getString(R.string.color_verdict_base_contrast)
                    explanation = getString(R.string.color_exp_base_contrast, getString(neutral.nameRes), getString(main.nameRes))
                    advice.append(getString(R.string.color_adv_base_contrast, getString(main.nameRes)))
                } else {
                    verdict = getString(R.string.color_verdict_monochrome)
                    explanation = getString(R.string.color_exp_monochrome)
                    advice.append(getString(R.string.color_adv_monochrome))
                }
            }
            angles.size == 2 -> {
                val diff = Math.abs(angles[0] - angles[1])
                val normDiff = if (diff > 180) 360 - diff else diff
                
                when {
                    normDiff <= 30 -> {
                        verdict = getString(R.string.color_verdict_analogous)
                        explanation = getString(R.string.color_exp_analogous)
                    }
                    normDiff in 150..180 -> {
                        verdict = getString(R.string.color_verdict_contrast)
                        explanation = getString(R.string.color_exp_contrast)
                    }
                    normDiff in 60..90 -> {
                        verdict = getString(R.string.color_verdict_risky)
                        explanation = getString(R.string.color_exp_risky)
                        advice.append(getString(R.string.color_adv_risky) + "\n\n")
                    }
                    else -> {
                        verdict = getString(R.string.color_verdict_balanced)
                        explanation = getString(R.string.color_exp_balanced)
                    }
                }
            }
            colors.size == 3 -> {
                verdict = getString(R.string.color_verdict_triad)
                explanation = getString(R.string.color_exp_triad)
            }
            else -> {
                verdict = getString(R.string.color_verdict_creative)
                explanation = getString(R.string.color_exp_creative)
                advice.append(getString(R.string.color_adv_creative))
            }
        }

        // Analyze shades intensity
        val intensityAdvice = analyzeShadesIntensity(colors)
        if (intensityAdvice.isNotEmpty()) {
            advice.append("\n\n" + getString(R.string.color_shades_notice) + intensityAdvice)
        }

        advice.append(getString(R.string.color_adv_wardobe_header))
        colors.forEach { color ->
            val colorName = getString(color.nameRes)
            val clothingTip = getString(color.tipRes)
            val recommendStr = getString(R.string.color_recommend)
            advice.append("- **$colorName**: $recommendStr $clothingTip.\n")
        }

        lastVerdict = verdict
        lastExplanation = explanation
        lastAdvice = advice.toString()
        
        binding.btnShowLastResult.visibility = View.GONE
        binding.cardDiagnosticResult.visibility = View.VISIBLE
        binding.textResultVerdict.text = verdict
        binding.textResultExplanation.text = explanation
        
        val adviceStr = advice.toString()
            .replace(Regex("\\*\\*(.*?)\\*\\*"), "<b>$1</b>")
            .replace("\n", "<br>")
        val spannedText = androidx.core.text.HtmlCompat.fromHtml(adviceStr, androidx.core.text.HtmlCompat.FROM_HTML_MODE_COMPACT)
        
        val spannableBuilder = android.text.SpannableStringBuilder(spannedText)
        val clothingItems = listOf(
            ClothingItem("блуз", R.string.clothing_bluzka_name, R.string.clothing_bluzka_desc_ua, "https://images.unsplash.com/photo-1551163943-3f6a855d1153?q=80&w=600&auto=format&fit=crop"),
            ClothingItem("куртк", R.string.clothing_kurtka_name, R.string.clothing_kurtka_desc_ua, "https://images.unsplash.com/photo-1551028719-0141bf9d2067?q=80&w=600&auto=format&fit=crop"),
            ClothingItem("кед", R.string.clothing_kedі_name, R.string.clothing_kedі_desc_ua, "https://images.unsplash.com/photo-1525966222134-fcfa99b8ae77?q=80&w=600&auto=format&fit=crop"),
            ClothingItem("лофер", R.string.clothing_loфері_name, R.string.clothing_loфері_desc_ua, "https://images.unsplash.com/photo-1549298916-b41d501d3772?q=80&w=600&auto=format&fit=crop")
        )

        val fullString = spannableBuilder.toString().lowercase()
        clothingItems.forEach { item ->
            var startIndex = fullString.indexOf(item.keyword)
            while (startIndex >= 0) {
                // Find end of word
                var endIndex = startIndex + item.keyword.length
                while (endIndex < fullString.length && fullString[endIndex].isLetter()) {
                    endIndex++
                }
                
                val clickableSpan = object : android.text.style.ClickableSpan() {
                    override fun onClick(widget: View) {
                        showClothingPopup(item)
                    }
                    override fun updateDrawState(ds: android.text.TextPaint) {
                        super.updateDrawState(ds)
                        ds.color = android.graphics.Color.parseColor("#BC13FE")
                        ds.isUnderlineText = true
                        ds.typeface = android.graphics.Typeface.DEFAULT_BOLD
                    }
                }
                spannableBuilder.setSpan(clickableSpan, startIndex, endIndex, android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                startIndex = fullString.indexOf(item.keyword, endIndex)
            }
        }

        binding.textResultAdvice.text = spannableBuilder
        binding.textResultAdvice.movementMethod = android.text.method.LinkMovementMethod.getInstance()
        
        // Soft scroll
        binding.scrollView.post {
            binding.scrollView.smoothScrollBy(0, 400)
        }
    }



    private fun generateAccessoriesAdvice(colors: List<FashionColor>, intensity: String): String {
        val advice = StringBuilder()
        val names = colors.map { it.id }
        val isPastel = intensity.contains("пастельну")
        val isDark = intensity.contains("Глибокі")
        
        // Footwear based on style
        when {
            isPastel -> advice.append("Взуття: Білі кеди, мінімалістичні сандалі або світлі лофери. ")
            isDark -> advice.append("Взуття: Шкіряні туфлі-човники, чорні оксфорди або структуровані ботильйони. ")
            else -> advice.append("Взуття: Кеди для кежуал-образу або класичні лофери для більш стриманого вигляду. ")
        }
        
        // Bags and accessories (Inclusive)
        advice.append("\nАксесуари: ")
        if (names.contains("brown") || names.contains("coffee") || names.contains("beige")) {
            advice.append("Шкіряний ремінь та сумка в тон (месенджер або тоут). ")
        } else {
            advice.append("Сумка-кросбоді або міський рюкзак. ")
        }
        
        // Gender neutral touches
        advice.append("Додайте годинник з металевим браслетом або мінімалістичну біжутерію. ")
        
        if (names.contains("black")) {
            advice.append("Для гостроти образу можна додати сонцезахисні окуляри в грубій оправі.")
        }
        
        return advice.toString()
    }

    private fun analyzeShadesIntensity(colors: List<FashionColor>): String {
        var lightCount = 0
        var darkCount = 0
        
        colors.forEach { color ->
            val c = android.graphics.Color.parseColor(color.hex)
            val brightness = (0.299 * android.graphics.Color.red(c) + 
                            0.587 * android.graphics.Color.green(c) + 
                            0.114 * android.graphics.Color.blue(c)) / 255
            
            if (brightness > 0.8) lightCount++
            if (brightness < 0.3) darkCount++
        }
        
        return when {
            lightCount == colors.size -> getString(R.string.color_shades_pastel)
            darkCount == colors.size -> getString(R.string.color_shades_dark)
            lightCount > 0 && darkCount > 0 -> getString(R.string.color_shades_contrast)
            else -> ""
        }
    }

    data class FashionColor(val id: String, val hex: String, val angle: Int, val nameRes: Int, val tipRes: Int)

    private fun colorizeText() {
        val colorMap = mapOf(
            getString(R.string.color_name_red) to "#FF0000",
            getString(R.string.color_name_yellow) to "#FFD700",
            getString(R.string.color_name_blue) to "#0000FF",
            getString(R.string.color_name_orange) to "#FFA500",
            getString(R.string.color_name_green) to "#008000",
            getString(R.string.color_name_violet) to "#800080"
        )

        val textView = binding.textEssenceContent
        val fullText = textView.text.toString()
        val spannable = android.text.SpannableString(fullText)

        colorMap.forEach { (word, colorStr) ->
            var startIndex = fullText.indexOf(word, ignoreCase = true)
            while (startIndex >= 0) {
                val endIndex = startIndex + word.length
                spannable.setSpan(
                    android.text.style.ForegroundColorSpan(android.graphics.Color.parseColor(colorStr)),
                    startIndex,
                    endIndex,
                    android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                spannable.setSpan(
                    android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
                    startIndex,
                    endIndex,
                    android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                startIndex = fullText.indexOf(word, startIndex + 1, ignoreCase = true)
            }
        }
        textView.text = spannable
    }

    private fun showFullScreenImage() {
        val dialog = android.app.Dialog(requireContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        dialog.setContentView(R.layout.dialog_image_preview)
        
        val imageView = dialog.findViewById<android.widget.ImageView>(R.id.image_preview)
        val closeButton = dialog.findViewById<android.widget.ImageView>(R.id.image_close)
        
        imageView.setImageResource(R.drawable.color_wheel_fashion)
        
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
        
        // Close on background click
        val rootLayout = dialog.findViewById<android.view.View>(R.id.root_image_preview)
        rootLayout?.setOnClickListener {
            dialog.dismiss()
        }
        
        closeButton.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    data class ClothingItem(val keyword: String, val nameRes: Int, val descRes: Int, val imageUrl: String)

    private fun showClothingPopup(item: ClothingItem) {
        val dialog = android.app.Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_clothing_popup)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val tvName = dialog.findViewById<android.widget.TextView>(R.id.tvClothingName)
        val tvDesc = dialog.findViewById<android.widget.TextView>(R.id.tvClothingDescription)
        val ivImage = dialog.findViewById<android.widget.ImageView>(R.id.ivClothingImage)

        tvName.text = getString(item.nameRes)
        tvDesc.text = getString(item.descRes)

        com.bumptech.glide.Glide.with(this)
            .load(item.imageUrl)
            .centerCrop()
            .into(ivImage)

        // Close when clicking outside or anywhere in the dialog root for simplicity if it fills screen
        val rootLayout = dialog.findViewById<android.view.View>(android.R.id.content)
        rootLayout?.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}
