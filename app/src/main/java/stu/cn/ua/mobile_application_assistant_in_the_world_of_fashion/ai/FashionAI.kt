package stu.cn.ua.mobile_application_assistant_in_the_world_of_fashion.ai

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.palette.graphics.Palette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.label.TensorLabel
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.MappedByteBuffer
import stu.cn.ua.mobile_application_assistant_in_the_world_of_fashion.R

class FashionAI(private val context: Context) {

    private var tflite: Interpreter? = null
    private var labels: List<String>? = null

    init {
        try {
            val model: MappedByteBuffer = FileUtil.loadMappedFile(context, "fashion_model.tflite")
            tflite = Interpreter(model)
            // Load labels if they exist, otherwise use placeholder
            labels = try {
                FileUtil.loadLabels(context, "labels.txt")
            } catch (e: Exception) {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Extracts the dominant color from an image URI.
     */
    suspend fun getDominantColor(imageUri: Uri): Int = withContext(Dispatchers.IO) {
        val inputStream = context.contentResolver.openInputStream(imageUri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        val palette = Palette.from(bitmap).generate()
        palette.getDominantColor(0xFFFFFF)
    }

    /**
     * Classifies clothing using TensorFlow Lite.
     */
    suspend fun classifyClothing(imageUri: Uri): String = withContext(Dispatchers.IO) {
        if (tflite == null) return@withContext "Модель не завантажена"

        val inputStream = context.contentResolver.openInputStream(imageUri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        
        if (bitmap == null) return@withContext "Не вдалося відкрити зображення"

        // Preprocess image to 224x224 (standard for EfficientNet-Lite0)
        val imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(224, 224, ResizeOp.ResizeMethod.BILINEAR))
            .add(NormalizeOp(0f, 255f))
            .build()

        var tensorImage = TensorImage(tflite!!.getInputTensor(0).dataType())
        tensorImage.load(bitmap)
        tensorImage = imageProcessor.process(tensorImage)

        // Output buffer
        val probabilityBuffer = TensorBuffer.createFixedSize(tflite!!.getOutputTensor(0).shape(), tflite!!.getOutputTensor(0).dataType())

        // Run inference
        tflite!!.run(tensorImage.buffer, probabilityBuffer.buffer)

        // Manual search for the highest probability index
        val probabilities = probabilityBuffer.floatArray
        var maxIdx = 0
        var maxProb = 0f
        for (i in probabilities.indices) {
            if (probabilities[i] > maxProb) {
                maxProb = probabilities[i]
                maxIdx = i
            }
        }

        // Map index to label if possible
        return@withContext if (labels != null && maxIdx < labels!!.size) {
            labels!![maxIdx]
        } else {
            // Mapping for EfficientNet ImageNet indices to localized resources
            val resId = when (maxIdx) {
                601 -> R.string.part_tops // T-shirt
                841 -> R.string.look_9_desc // Sweatshirt/Hose desc
                482 -> R.string.part_outerwear // Cloak
                867 -> R.string.part_outerwear // Trench coat
                617 -> R.string.part_outerwear // Lab coat
                611 -> R.string.part_tops // Kimono (top)
                else -> -1
            }
            if (resId != -1) context.getString(resId) else "Category #$maxIdx"
        }
    }

    /**
     * Checks if two colors are "harmonious".
     */
    fun areColorsHarmonious(color1: Int, color2: Int): Boolean {
        // Here we could implement real color harmony logic (complements, analogs, etc.)
        return true
    }
}
