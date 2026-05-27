package stu.cn.ua.mobile_application_assistant_in_the_world_of_fashion.ui.fragments

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import stu.cn.ua.mobile_application_assistant_in_the_world_of_fashion.R
import stu.cn.ua.mobile_application_assistant_in_the_world_of_fashion.databinding.FragmentMixerBinding
import stu.cn.ua.mobile_application_assistant_in_the_world_of_fashion.ui.adapters.MixerImageAdapter
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import stu.cn.ua.mobile_application_assistant_in_the_world_of_fashion.ai.FashionAI
import stu.cn.ua.mobile_application_assistant_in_the_world_of_fashion.network.WeatherService
import stu.cn.ua.mobile_application_assistant_in_the_world_of_fashion.network.WeatherData
import stu.cn.ua.mobile_application_assistant_in_the_world_of_fashion.network.PexelsService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import stu.cn.ua.mobile_application_assistant_in_the_world_of_fashion.LocaleHelper

class MixerFragment : Fragment() {

    private var _binding: FragmentMixerBinding? = null
    private val binding get() = _binding!!

    private val upperImages = mutableListOf<Uri>()
    private val lowerImages = mutableListOf<Uri>()

    private lateinit var upperAdapter: MixerImageAdapter
    private lateinit var lowerAdapter: MixerImageAdapter
    private lateinit var fashionAi: FashionAI
    private val weatherService = WeatherService()

    private val pexelsService = Retrofit.Builder()
        .baseUrl("https://api.pexels.com/v1/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(PexelsService::class.java)

    private val PEXELS_KEY = "cAtmAqzCpcHOjzRLSNSpc7uFTe7BPWEFTaWnbVBMiuuIEw5Hawy46n5X"

    // Image Pickers
    private val pickUpperMedia = registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(5)) { uris ->
        if (uris.isNotEmpty()) {
            upperImages.addAll(uris)
            upperAdapter.notifyDataSetChanged()
        }
    }

    private val pickLowerMedia = registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(5)) { uris ->
        if (uris.isNotEmpty()) {
            lowerImages.addAll(uris)
            lowerAdapter.notifyDataSetChanged()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMixerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fashionAi = FashionAI(requireContext())
        setupRecyclerViews()
        setupClickListeners()
    }

    private fun setupRecyclerViews() {
        upperAdapter = MixerImageAdapter(upperImages) { position ->
            upperImages.removeAt(position)
            upperAdapter.notifyItemRemoved(position)
        }
        binding.rvUpper.adapter = upperAdapter

        lowerAdapter = MixerImageAdapter(lowerImages) { position ->
            lowerImages.removeAt(position)
            lowerAdapter.notifyItemRemoved(position)
        }
        binding.rvLower.adapter = lowerAdapter
    }

    private fun setupClickListeners() {
        binding.btnAddUpper.setOnClickListener {
            pickUpperMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.btnAddLower.setOnClickListener {
            pickLowerMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.btnMixCard.setOnClickListener {
            if (upperImages.isEmpty() || lowerImages.isEmpty()) {
                Toast.makeText(requireContext(), getString(R.string.toast_add_photos), Toast.LENGTH_SHORT).show()
            } else {
                startAiProcessing()
            }
        }

        binding.btnSmartMix.setOnClickListener {
            if (checkProfileLocation()) {
                startSmartMixProcessing()
            }
        }
    }

    private fun checkProfileLocation(): Boolean {
        val prefs = requireContext().getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
        val city = prefs.getString("saved_city", "")
        val country = prefs.getString("saved_country", "")

        if (city.isNullOrEmpty() || country.isNullOrEmpty()) {
            AlertDialog.Builder(requireContext())
                .setTitle("Профіль не заповнено")
                .setMessage("Будь ласка, вкажіть ваше місто та країну в налаштуваннях профілю для точного підбору образу.")
                .setPositiveButton("До налаштувань") { _, _ ->
                    // Navigate to profile (Assuming standard ID)
                    activity?.findViewById<View>(R.id.tab_profile)?.performClick()
                }
                .setNegativeButton("Пізніше", null)
                .show()
            return false
        }
        return true
    }

    private fun startAiProcessing() {
        binding.overlayAi.visibility = View.VISIBLE
        
        lifecycleScope.launch {
            try {
                // Real Image Analysis using Palette and TFLite
                if (upperImages.isEmpty() || lowerImages.isEmpty()) return@launch

                val randomUpperIndex = (0 until upperImages.size).random()
                val randomLowerIndex = (0 until lowerImages.size).random()
                
                val upperUri = upperImages[randomUpperIndex]
                val lowerUri = lowerImages[randomLowerIndex]
                
                // Analyze images
                val upperLabel = fashionAi.classifyClothing(upperUri)
                val lowerLabel = fashionAi.classifyClothing(lowerUri)
                
                // Artificial delay for effect
                delay(2000)
                
                binding.overlayAi.visibility = View.GONE
                showResult(upperUri, lowerUri, upperLabel, lowerLabel)
            } catch (e: Exception) {
                binding.overlayAi.visibility = View.GONE
                Toast.makeText(requireContext(), "Помилка при обробці фото: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startSmartMixProcessing() {
        // Validation: Ensure all parameters are selected
        if (binding.chipGroupSeason.checkedChipId == View.NO_ID ||
            binding.chipGroupDestination.checkedChipId == View.NO_ID ||
            binding.chipGroupMood.checkedChipId == View.NO_ID) {
            Toast.makeText(requireContext(), getString(R.string.toast_select_params), Toast.LENGTH_SHORT).show()
            return
        }

        val prefs = requireContext().getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
        val city = prefs.getString("saved_city", "Київ") ?: "Київ"
        
        binding.overlayAi.visibility = View.VISIBLE
        
        lifecycleScope.launch {
            try {
                val lang = LocaleHelper.getSavedLanguage(requireContext())
                val weather = weatherService.getWeather(city, lang)
                
                // Build Fashion Query for Pexels
                val gender = if (binding.chipMale.isChecked) "man" else "woman"
                val season = when {
                    binding.chipWinter.isChecked -> "winter"
                    binding.chipSpring.isChecked -> "spring"
                    binding.chipSummer.isChecked -> "summer"
                    else -> "autumn"
                }
                val occasion = when {
                    binding.chipWork.isChecked -> "office business"
                    binding.chipParty.isChecked -> {
                        if (gender == "woman") "elegant evening dress party luxury"
                        else "luxury suit tuxedo party formal"
                    }
                    binding.chipWalk.isChecked -> "streetwear casual comfy look walk"
                    binding.chipGym.isChecked -> "sport fitness"
                    binding.chipMeeting.isChecked -> "formal business meeting"
                    else -> "casual"
                }
                val query = "$gender fashion outfit $season $occasion style"
                
                val pexelsResponse = pexelsService.searchPhotos(PEXELS_KEY, query)
                val photoUrl = pexelsResponse.photos.randomOrNull()?.src?.large
                
                binding.overlayAi.visibility = View.GONE
                showSmartResult(weather, photoUrl)
            } catch (e: Exception) {
                binding.overlayAi.visibility = View.GONE
                val lang = LocaleHelper.getSavedLanguage(requireContext())
                showSmartResult(WeatherData(15, if (lang == "uk") "Хмарно" else "Cloudy"), null)
            }
        }
    }

    private fun showSmartResult(weather: WeatherData, imageUrl: String?) {
        val prefs = requireContext().getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
        val city = prefs.getString("saved_city", "Kyiv")
        
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_smart_mix_result, null)
        val ivResult = dialogView.findViewById<android.widget.ImageView>(R.id.iv_smart_result)
        val tvLocation = dialogView.findViewById<android.widget.TextView>(R.id.tv_smart_location)
        val tvMood = dialogView.findViewById<android.widget.TextView>(R.id.tv_smart_mood_msg)
        val tvDetails = dialogView.findViewById<android.widget.TextView>(R.id.tv_smart_details)
        
        if (imageUrl != null) {
            Glide.with(this)
                .load(imageUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .into(ivResult)
        } else {
            ivResult.setImageResource(R.drawable.look_cafe)
        }

        tvLocation.text = "📍 $city: ${weather.temp}°C, ${weather.description}"
        
        val isGoodMood = binding.chipMoodGood.isChecked
        val moodMessageRes = if (isGoodMood) {
            listOf(R.string.smart_result_msg_good_1, R.string.smart_result_msg_good_2, R.string.smart_result_msg_good_3).random()
        } else {
            listOf(R.string.smart_result_msg_bad_1, R.string.smart_result_msg_bad_2, R.string.smart_result_msg_bad_3).random()
        }
        tvMood.text = getString(moodMessageRes)
        
        val adviceRes = when {
            weather.temp < 0 -> R.string.advice_freezing
            weather.temp < 10 -> R.string.advice_cold
            weather.temp < 20 -> R.string.advice_mild
            weather.temp < 30 -> R.string.advice_warm
            else -> R.string.advice_hot
        }
        val advice = getString(adviceRes)
        
        tvDetails.text = "$advice\n\n${getString(R.string.smart_result_footer, city, weather.description)}"

        AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setPositiveButton(R.string.smart_result_cool) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun showResult(upperUri: Uri, lowerUri: Uri, upperLabel: String, lowerLabel: String) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_mixer_result, null)
        val ivUpper = dialogView.findViewById<android.widget.ImageView>(R.id.iv_result_upper)
        val ivLower = dialogView.findViewById<android.widget.ImageView>(R.id.iv_result_lower)
        val tvUpperLabel = dialogView.findViewById<android.widget.TextView>(R.id.tv_result_upper_label)
        val tvLowerLabel = dialogView.findViewById<android.widget.TextView>(R.id.tv_result_lower_label)

        ivUpper.setImageURI(upperUri)
        ivLower.setImageURI(lowerUri)
        tvUpperLabel.text = upperLabel
        tvLowerLabel.text = lowerLabel

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setPositiveButton(R.string.smart_result_cool) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance() = MixerFragment()
    }
}
