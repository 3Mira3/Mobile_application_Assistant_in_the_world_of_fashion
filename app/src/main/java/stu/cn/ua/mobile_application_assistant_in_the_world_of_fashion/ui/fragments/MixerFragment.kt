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
import stu.cn.ua.mobile_application_assistant_in_the_world_of_fashion.BuildConfig

/* ===== GEMINI CHAT IMPORTS — ЗАКОМЕНТОВАНО =====
import android.os.Handler
import android.os.Looper
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import stu.cn.ua.mobile_application_assistant_in_the_world_of_fashion.ui.adapters.ChatAdapter
import stu.cn.ua.mobile_application_assistant_in_the_world_of_fashion.ui.adapters.ChatMessage
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import androidx.drawerlayout.widget.DrawerLayout
import androidx.core.view.GravityCompat
import stu.cn.ua.mobile_application_assistant_in_the_world_of_fashion.ui.adapters.ChatHistoryAdapter
import stu.cn.ua.mobile_application_assistant_in_the_world_of_fashion.ui.adapters.ChatHistoryItem
import org.json.JSONObject
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import android.util.Base64
===== */

class MixerFragment : Fragment() {

    private var _binding: FragmentMixerBinding? = null
    private val binding get() = _binding!!

    private val upperImages = mutableListOf<Uri>()
    private val lowerImages = mutableListOf<Uri>()

    /* ===== GEMINI CHAT FIELDS — ЗАКОМЕНТОВАНО =====
    private val GEMINI_API_KEY = BuildConfig.GEMINI_API_KEY

    private val chatMessages = mutableListOf<ChatMessage>()
    private lateinit var chatAdapter: ChatAdapter
    private var generativeModel: GenerativeModel? = null

    private val chatHistoryList = mutableListOf<ChatHistoryItem>()
    private lateinit var chatHistoryAdapter: ChatHistoryAdapter
    private var currentChatId: String? = null

    private val hideWidgetHandler = Handler(Looper.getMainLooper())
    private val hideWidgetRunnable = Runnable {
        if (_binding != null && binding.chatContainer.visibility != View.VISIBLE) {
            binding.cardChatWidget.animate()
                .alpha(0f)
                .translationX(100f)
                .setDuration(300)
                .withEndAction {
                    if (_binding != null) {
                        binding.cardChatWidget.visibility = View.GONE
                    }
                }
                .start()
        }
    }
    ===== */

    private lateinit var upperAdapter: MixerImageAdapter
    private lateinit var lowerAdapter: MixerImageAdapter
    private lateinit var fashionAi: FashionAI
    private val weatherService = WeatherService()

    private val pexelsService = Retrofit.Builder()
        .baseUrl("https://api.pexels.com/v1/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(PexelsService::class.java)

    private val PEXELS_KEY = BuildConfig.PEXELS_API_KEY

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
        // setupAiChat() // GEMINI CHAT — ЗАКОМЕНТОВАНО
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
        // hideWidgetHandler.removeCallbacks(hideWidgetRunnable) // GEMINI CHAT — ЗАКОМЕНТОВАНО
        _binding = null
    }

    /* ===== GEMINI CHAT METHODS — ЗАКОМЕНТОВАНО =====
    private fun setupAiChat() {
        val lang = LocaleHelper.getSavedLanguage(requireContext())

        // Initialize Gemini SDK if API key is provided
        if (GEMINI_API_KEY.isNotEmpty()) {
            try {
                val systemInstructionText = if (lang == "uk") {
                    "Ти — професійний стиліст додатку \"Помічник у світі моди\". Твоє завдання — допомагати користувачеві підбирати гардероб, аналізувати стилі, поєднувати кольори та давати поради щодо одягу на основі погоди чи подій. Якщо користувач ставить запитання, які взагалі не стосуються моди, стилю, одягу чи брендів, ти маєш ввічливо відмовити і сказати, що вмієш консультувати лише у сфері моди. Відповідай коротко і дружньо українською мовою."
                } else {
                    "You are a professional stylist in the \"Fashion World Assistant\" app. Your task is to help the user choose a wardrobe, analyze styles, match colors, and give clothing advice based on the weather or events. If the user asks questions that are completely unrelated to fashion, style, clothing, or brands, you must politely decline and state that you can only advise in the fashion domain. Respond briefly and friendly in English."
                }
                generativeModel = GenerativeModel(
                    modelName = "gemini-2.0-flash",
                    apiKey = GEMINI_API_KEY,
                    systemInstruction = content {
                        text(systemInstructionText)
                    }
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Initialize Chat Adapter
        chatAdapter = ChatAdapter(chatMessages)
        binding.rvChatMessages.layoutManager = LinearLayoutManager(requireContext())
        binding.rvChatMessages.adapter = chatAdapter

        // Initialize History Adapter
        chatHistoryAdapter = ChatHistoryAdapter(
            chatHistoryList,
            currentChatId,
            onChatSelected = { historyItem ->
                selectChat(historyItem.id)
            },
            onChatDeleted = { historyItem ->
                deleteChat(historyItem.id)
            }
        )
        binding.rvChatHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.rvChatHistory.adapter = chatHistoryAdapter

        // Load history metadata and load the active/latest chat
        loadHistoryIndex()

        // Banner button click
        binding.btnStartAiChat.setOnClickListener {
            openChatContainer()
        }

        // Floating widget click
        binding.cardChatWidget.setOnClickListener {
            openChatContainer()
        }

        // Minimize chat click
        binding.btnChatMinimize.setOnClickListener {
            binding.chatContainer.visibility = View.GONE
            binding.cardChatWidget.visibility = View.VISIBLE
            binding.cardChatWidget.alpha = 1f
            binding.cardChatWidget.translationX = 0f
            resetHideWidgetTimer()
        }

        // Close chat click
        binding.btnChatClose.setOnClickListener {
            binding.chatContainer.visibility = View.GONE
            binding.cardChatWidget.visibility = View.GONE
            hideWidgetHandler.removeCallbacks(hideWidgetRunnable)
        }

        // Scroll listener for floating widget
        binding.mixerScrollView.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { _, _, _, _, _ ->
            if (binding.chatContainer.visibility != View.VISIBLE) {
                showChatWidgetWithAnimation()
                resetHideWidgetTimer()
            }
        })

        // Send message click
        binding.btnChatSend.setOnClickListener {
            val text = binding.etChatInput.text.toString().trim()
            if (text.isNotEmpty()) {
                sendMessage(text)
            }
        }

        // Hamburger menu click
        binding.btnChatMenu.setOnClickListener {
            binding.chatContainer.openDrawer(GravityCompat.START)
        }

        // New Chat button click in sidebar
        binding.btnChatNew.setOnClickListener {
            createNewChat()
        }
    }

    private fun openChatContainer() {
        binding.chatContainer.visibility = View.VISIBLE
        binding.cardChatWidget.visibility = View.GONE
        hideWidgetHandler.removeCallbacks(hideWidgetRunnable)
        if (chatMessages.isNotEmpty()) {
            binding.rvChatMessages.scrollToPosition(chatMessages.size - 1)
        }
    }

    private fun showChatWidgetWithAnimation() {
        if (binding.cardChatWidget.visibility != View.VISIBLE) {
            binding.cardChatWidget.visibility = View.VISIBLE
            binding.cardChatWidget.alpha = 0f
            binding.cardChatWidget.translationX = 100f
            binding.cardChatWidget.animate()
                .alpha(1f)
                .translationX(0f)
                .setDuration(300)
                .start()
        }
    }

    private fun resetHideWidgetTimer() {
        hideWidgetHandler.removeCallbacks(hideWidgetRunnable)
        hideWidgetHandler.postDelayed(hideWidgetRunnable, 5000)
    }

    private fun sendMessage(text: String) {
        binding.etChatInput.setText("")

        val userMsg = ChatMessage(text = text, isUser = true)
        chatMessages.add(userMsg)
        chatAdapter.notifyItemInserted(chatMessages.size - 1)
        binding.rvChatMessages.scrollToPosition(chatMessages.size - 1)

        // Update chat title in sidebar if it's the first user message
        val activeChat = chatHistoryList.find { it.id == currentChatId }
        val lang = LocaleHelper.getSavedLanguage(requireContext())
        val defaultNewChatTitle = if (lang == "uk") "Новий чат" else "New Chat"
        if (activeChat != null && activeChat.title == defaultNewChatTitle) {
            val newTitle = if (text.length > 25) text.substring(0, 22) + "..." else text
            val updatedChat = activeChat.copy(title = newTitle)
            val index = chatHistoryList.indexOf(activeChat)
            if (index != -1) {
                chatHistoryList[index] = updatedChat
                saveHistoryIndex()
                chatHistoryAdapter.updateData(chatHistoryList, currentChatId)
            }
        }

        saveActiveChatMessages()

        val botLoadingMsg = ChatMessage(text = "...", isUser = false)
        chatMessages.add(botLoadingMsg)
        val botPosition = chatMessages.size - 1
        chatAdapter.notifyItemInserted(botPosition)
        binding.rvChatMessages.scrollToPosition(botPosition)

        lifecycleScope.launch {
            try {
                val isImageRequest = listOf("покажи", "намалюй", "згенеруй", "generate", "show", "draw", "create image", "image").any { text.lowercase().contains(it) }
                
                if (isImageRequest) {
                    if (GEMINI_API_KEY.isNotEmpty()) {
                        // Generate image via Imagen
                        val imageUri = generateImage(text)
                        if (imageUri != null) {
                            val description = getAiResponse("Згенеруй короткий текстовий опис образу для: $text")
                            if (botPosition < chatMessages.size) {
                                chatMessages[botPosition] = ChatMessage(text = description, isUser = false, imageUri = imageUri)
                                chatAdapter.notifyItemChanged(botPosition)
                                binding.rvChatMessages.scrollToPosition(botPosition)
                                saveActiveChatMessages()
                            }
                        } else {
                            if (botPosition < chatMessages.size) {
                                val errorMsg = if (lang == "uk") "Не вдалося згенерувати зображення." else "Failed to generate image."
                                chatMessages[botPosition] = ChatMessage(text = errorMsg, isUser = false)
                                chatAdapter.notifyItemChanged(botPosition)
                                binding.rvChatMessages.scrollToPosition(botPosition)
                            }
                        }
                    } else {
                        // Mock offline image generation
                        delay(1500)
                        val textResponse = if (lang == "uk") "Ось ваш згенерований образ!" else "Here is your generated look!"
                        val mockResId = when {
                            text.lowercase().contains("робот") || text.lowercase().contains("work") -> R.drawable.look_work
                            text.lowercase().contains("спорт") || text.lowercase().contains("gym") -> R.drawable.look_gym
                            text.lowercase().contains("зустріч") || text.lowercase().contains("meeting") -> R.drawable.look_meeting
                            else -> R.drawable.look_cafe
                        }
                        val imageUri = "android.resource://${requireContext().packageName}/$mockResId"
                        if (botPosition < chatMessages.size) {
                            chatMessages[botPosition] = ChatMessage(text = textResponse, isUser = false, imageUri = imageUri)
                            chatAdapter.notifyItemChanged(botPosition)
                            binding.rvChatMessages.scrollToPosition(botPosition)
                            saveActiveChatMessages()
                        }
                    }
                } else {
                    val responseText = getAiResponse(text)
                    if (botPosition < chatMessages.size) {
                        chatMessages[botPosition] = ChatMessage(text = responseText, isUser = false)
                        chatAdapter.notifyItemChanged(botPosition)
                        binding.rvChatMessages.scrollToPosition(botPosition)
                        saveActiveChatMessages()
                    }
                }
            } catch (e: Exception) {
                if (botPosition < chatMessages.size) {
                    val errorPrefix = if (lang == "uk") "Вибачте, виникла помилка: " else "Sorry, an error occurred: "
                    chatMessages[botPosition] = ChatMessage(text = errorPrefix + e.message, isUser = false)
                    chatAdapter.notifyItemChanged(botPosition)
                    binding.rvChatMessages.scrollToPosition(botPosition)
                }
            }
        }
    }

    private suspend fun getAiResponse(userMessage: String): String = withContext(Dispatchers.IO) {
        val model = generativeModel
        val lang = LocaleHelper.getSavedLanguage(requireContext())
        if (model != null) {
            try {
                // Send message to actual Gemini API
                val response = model.startChat().sendMessage(userMessage)
                return@withContext response.text ?: (if (lang == "uk") "Я не зміг сформулювати відповідь." else "I couldn't formulate a response.")
            } catch (e: Exception) {
                return@withContext (if (lang == "uk") "Помилка запиту до Gemini: " else "Gemini request error: ") + (e.localizedMessage ?: e.message)
            }
        } else {
            // Intelligent Mock offline stylist response
            delay(1500) // Simulate network delay
            val msgLower = userMessage.lowercase()
            
            if (lang == "uk") {
                // Check if it's unrelated to fashion
                val isUnrelated = listOf("рецепт", "код", "програмув", "математик", "рівняння", "історія україни", "піца", "куховар", "новини", "політик", "війна", "біолог", "хімі").any { msgLower.contains(it) }
                if (isUnrelated) {
                    return@withContext "Я — твій персональний ШІ-стиліст додатка \"Помічник у світі моди\". Я можу відповідати лише на запитання, які стосуються одягу, вибору стилю, кольорової гармонії та модних порад. Спробуйте запитати про підбір образу!"
                }
                return@withContext when {
                    msgLower.contains("привіт") || msgLower.contains("добрий день") || msgLower.contains("вітаю") ->
                        "Вітаю! Радий допомогти з вибором образу. Опишіть вашу подію (наприклад, зустріч, прогулянка чи вечірка), і я підкажу найкращі комбінації кольорів та одягу!"
                    msgLower.contains("сукн") || msgLower.contains("платт") ->
                        "Для особливих подій чи вечірок раджу звернути увагу на довгі силуетні сукні глибоких кольорів (наприклад, королівський синій або смарагдовий). Для повсякденного стилю чудово підійде сукня-міді з бавовни чи льону у поєднанні з білими кедами."
                    msgLower.contains("колір") || msgLower.contains("поєднан") || msgLower.contains("гам") ->
                        "Чудовий вибір кольорової гами є основою стилю! Рекомендую використовувати аналогічну гармонію (кольори, що розташовані поруч на колі Іттена) для стриманого образу, або контрастні комбінації (наприклад, фіолетовий та жовтий) для яскравих виходів."
                    msgLower.contains("взутт") || msgLower.contains("черевик") || msgLower.contains("кед") ->
                        "Взуття задає тон усьому образу! У 2026 році на піку популярності залишається зручне та мінімалістичне взуття: класичні білі кеди під сукні та спідниці, а також грубі лофери під класичні штани чи джинси прямого крою."
                    msgLower.contains("холод") || msgLower.contains("осінь") || msgLower.contains("зима") || msgLower.contains("погод") ->
                        "У прохолодну погоду головне — багатошаровість. Спробуйте поєднати вовняний светр поверх світлої сорочки, додати пальто крою оверсайз та стильний об'ємний шарф. Це не лише тепло, але й дуже модно!"
                    else ->
                        "Цікаве запитання! Для цього образу я б порадив поєднати класичні елементи (наприклад, блейзер нейтрального кольору) з більш спортивними чи кежуал деталями. Не бійтеся експериментувати та додавати аксесуари!"
                }
            } else {
                // Check if it's unrelated to fashion in English
                val isUnrelated = listOf("recipe", "code", "program", "math", "equation", "history", "pizza", "cook", "news", "politic", "war", "biology", "chemistry").any { msgLower.contains(it) }
                if (isUnrelated) {
                    return@withContext "I am your personal AI Stylist in the \"Fashion World Assistant\" app. I can only answer questions related to clothing, style choices, color harmony, and fashion tips. Try asking about outfit selection!"
                }
                return@withContext when {
                    msgLower.contains("hello") || msgLower.contains("hi") || msgLower.contains("greet") ->
                        "Hello! Glad to help you select a look. Describe your event (e.g., meeting, walk, or party), and I will suggest the best color and clothing combinations!"
                    msgLower.contains("dress") || msgLower.contains("skirt") ->
                        "For special events or parties, I recommend long silhouette dresses in deep colors (like royal blue or emerald). For daily casual style, a cotton or linen midi dress paired with white sneakers is wonderful."
                    msgLower.contains("color") || msgLower.contains("combine") || msgLower.contains("match") ->
                        "A great color scheme is the foundation of style! I recommend using analogous harmony (colors next to each other on the color wheel) for a subtle look, or contrasting combinations (like purple and yellow) for bold appearances."
                    msgLower.contains("shoe") || msgLower.contains("sneaker") || msgLower.contains("boot") ->
                        "Shoes set the tone for the entire outfit! In 2026, comfortable and minimalistic footwear remains extremely popular: classic white sneakers under dresses and skirts, or chunky loafers with classic pants or straight-cut jeans."
                    msgLower.contains("cold") || msgLower.contains("autumn") || msgLower.contains("winter") || msgLower.contains("weather") ->
                        "In cool weather, layering is key. Try combining a wool sweater over a light shirt, adding an oversized coat and a stylish chunky scarf. It's not only warm but very trendy!"
                    else ->
                        "Interesting question! For this look, I would advise combining classic elements (such as a neutral blazer) with more sporty or casual details. Don't be afraid to experiment and add accessories!"
                }
            }
        }
    }

    private suspend fun generateImage(prompt: String): String? = withContext(Dispatchers.IO) {
        try {
            val client = OkHttpClient()
            val json = JSONObject().apply {
                val instances = org.json.JSONArray().apply {
                    put(JSONObject().apply { put("prompt", prompt) })
                }
                put("instances", instances)
                
                val parameters = JSONObject().apply {
                    put("sampleCount", 1)
                    put("aspectRatio", "1:1")
                    put("outputMimeType", "image/jpeg")
                }
                put("parameters", parameters)
            }
            val requestBody = json.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/imagen-4.0-generate-001:predict?key=$GEMINI_API_KEY")
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext null
                val responseBody = response.body?.string() ?: return@withContext null
                val jsonResponse = JSONObject(responseBody)
                val predictions = jsonResponse.optJSONArray("predictions") ?: return@withContext null
                if (predictions.length() == 0) return@withContext null
                val base64Bytes = predictions.getJSONObject(0).getString("bytesBase64Encoded")
                val imageBytes = Base64.decode(base64Bytes, Base64.DEFAULT)

                val filename = "generated_${System.currentTimeMillis()}.jpg"
                val file = File(requireContext().filesDir, filename)
                file.writeBytes(imageBytes)
                return@withContext Uri.fromFile(file).toString()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext null
        }
    }

    private fun getIndexFile(): File {
        return File(requireContext().filesDir, "chat_index.json")
    }

    private fun getChatFile(id: String): File {
        return File(requireContext().filesDir, "chat_$id.json")
    }

    private fun loadHistoryIndex() {
        chatHistoryList.clear()
        val indexFile = getIndexFile()
        if (indexFile.exists()) {
            try {
                val json = indexFile.readText()
                val type = object : TypeToken<List<ChatHistoryItem>>() {}.type
                val loadedList: List<ChatHistoryItem>? = Gson().fromJson(json, type)
                if (loadedList != null) {
                    chatHistoryList.addAll(loadedList)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        if (chatHistoryList.isEmpty()) {
            createNewChat()
        } else {
            val latestChat = chatHistoryList.maxByOrNull { it.timestamp }
            if (latestChat != null) {
                selectChat(latestChat.id)
            } else {
                createNewChat()
            }
        }
    }

    private fun saveHistoryIndex() {
        try {
            val json = Gson().toJson(chatHistoryList)
            getIndexFile().writeText(json)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createNewChat() {
        val id = System.currentTimeMillis().toString()
        val lang = LocaleHelper.getSavedLanguage(requireContext())
        val title = if (lang == "uk") "Новий чат" else "New Chat"
        val item = ChatHistoryItem(id = id, title = title, timestamp = System.currentTimeMillis())
        
        chatHistoryList.add(0, item)
        saveHistoryIndex()

        val welcomeMsgText = if (lang == "uk") {
            "Привіт! Я твій персональний AI-стиліст. Можу порадити поєднання одягу, розповісти про тренди або згенерувати образ. Спробуй написати: \"Згенеруй вечірній образ\""
        } else {
            "Hello! I am your personal AI Stylist. I can recommend outfit pairings, tell you about trends, or generate look images. Try writing: \"Generate a evening outfit\""
        }
        val welcomeMsg = ChatMessage(text = welcomeMsgText, isUser = false)
        val initialMessages = listOf(welcomeMsg)

        try {
            val json = Gson().toJson(initialMessages)
            getChatFile(id).writeText(json)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        selectChat(id)
        if (binding.chatContainer.isDrawerOpen(GravityCompat.START)) {
            binding.chatContainer.closeDrawer(GravityCompat.START)
        }
    }

    private fun selectChat(id: String) {
        currentChatId = id
        chatMessages.clear()

        val chatFile = getChatFile(id)
        if (chatFile.exists()) {
            try {
                val json = chatFile.readText()
                val type = object : TypeToken<List<ChatMessage>>() {}.type
                val loadedList: List<ChatMessage>? = Gson().fromJson(json, type)
                if (loadedList != null) {
                    chatMessages.addAll(loadedList)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        chatAdapter.notifyDataSetChanged()
        chatHistoryAdapter.updateData(chatHistoryList, currentChatId)
        
        if (chatMessages.isNotEmpty()) {
            binding.rvChatMessages.scrollToPosition(chatMessages.size - 1)
        }
        if (binding.chatContainer.isDrawerOpen(GravityCompat.START)) {
            binding.chatContainer.closeDrawer(GravityCompat.START)
        }
    }

    private fun deleteChat(id: String) {
        val chatFile = getChatFile(id)
        if (chatFile.exists()) {
            chatFile.delete()
        }

        val itemToDelete = chatHistoryList.find { it.id == id }
        if (itemToDelete != null) {
            chatHistoryList.remove(itemToDelete)
            saveHistoryIndex()
        }

        if (currentChatId == id) {
            if (chatHistoryList.isNotEmpty()) {
                selectChat(chatHistoryList[0].id)
            } else {
                createNewChat()
            }
        } else {
            chatHistoryAdapter.updateData(chatHistoryList, currentChatId)
        }
    }

    private fun saveActiveChatMessages() {
        val id = currentChatId ?: return
        try {
            val json = Gson().toJson(chatMessages)
            getChatFile(id).writeText(json)
            
            val activeChat = chatHistoryList.find { it.id == id }
            if (activeChat != null) {
                val updatedChat = activeChat.copy(timestamp = System.currentTimeMillis())
                val index = chatHistoryList.indexOf(activeChat)
                if (index != -1) {
                    chatHistoryList.removeAt(index)
                    chatHistoryList.add(0, updatedChat)
                    saveHistoryIndex()
                    chatHistoryAdapter.updateData(chatHistoryList, currentChatId)
                }
            }
        } catch (e: Exception) {
        }
    }
    ===== КІНЕЦЬ GEMINI CHAT METHODS ===== */

    companion object {
        @JvmStatic
        fun newInstance() = MixerFragment()
    }
}
