package com.muslim.prayerapp.data.repository

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

data class DownloadableAzan(
    val id: String,
    val titleArabic: String,
    val descriptionArabic: String,
    val downloadUrl: String
)

data class LocalCustomAzan(
    val id: String,
    val titleArabic: String,
    val filePath: String,
    val isDownloadedFromNet: Boolean
)

class CustomAzanRepository(private val context: Context) {

    private val azanFolder = File(context.filesDir, "custom_azans").apply {
        if (!exists()) mkdirs()
    }

    val onlineAzanList = listOf(
        DownloadableAzan(
            id = "ali_mulla",
            titleArabic = "أذان الشيخ علي أحمد ملا",
            descriptionArabic = "الأذان المكي الشهير بصوت شيخ مؤذني الحرم المكي",
            downloadUrl = "https://download.quranicaudio.com/azan/ali_mulla.mp3"
        ),
        DownloadableAzan(
            id = "mustafa_ismail",
            titleArabic = "أذان الشيخ مصطفى إسماعيل",
            descriptionArabic = "الأذان الخاشع بصوت قارة المقرئين الشيخ مصطفى إسماعيل",
            downloadUrl = "https://download.quranicaudio.com/azan/mustafa_ismail.mp3"
        ),
        DownloadableAzan(
            id = "rifat",
            titleArabic = "أذان الشيخ محمد رفعت",
            descriptionArabic = "صوت رمضان والتاريخ أذان الشيخ محمد رفعت الخالد",
            downloadUrl = "https://download.quranicaudio.com/azan/rifat.mp3"
        ),
        DownloadableAzan(
            id = "azhar",
            titleArabic = "أذان مصر التاريخي (الأزهر الشريف)",
            descriptionArabic = "أذان تاريخي مبارك من رحاب الجامع الأزهر الشريف",
            downloadUrl = "https://download.quranicaudio.com/azan/azhar.mp3"
        ),
        DownloadableAzan(
            id = "aqsa_online",
            titleArabic = "أذان المسجد الأقصى المبارك",
            descriptionArabic = "نداء الأذان الخاشع من رحاب المسجد الأقصى الشريف",
            downloadUrl = "https://download.quranicaudio.com/azan/aqsa.mp3"
        )
    )

    fun isAzanDownloaded(id: String): Boolean {
        val file = File(azanFolder, "$id.mp3")
        return file.exists() && file.length() > 10_000L
    }

    fun getDownloadedFilePath(id: String): String {
        return File(azanFolder, "$id.mp3").absolutePath
    }

    suspend fun downloadAzan(
        azan: DownloadableAzan,
        onProgress: (Int) -> Unit
    ): String? = withContext(Dispatchers.IO) {
        try {
            val destinationFile = File(azanFolder, "${azan.id}.mp3")
            val url = URL(azan.downloadUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 15_000
            connection.readTimeout = 30_000
            connection.connect()

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                return@withContext null
            }

            val fileLength = connection.contentLength
            val inputStream = connection.inputStream
            val outputStream = FileOutputStream(destinationFile)

            val data = ByteArray(4096)
            var total: Long = 0
            var count: Int

            while (inputStream.read(data).also { count = it } != -1) {
                total += count
                if (fileLength > 0) {
                    onProgress((total * 100 / fileLength).toInt())
                }
                outputStream.write(data, 0, count)
            }

            outputStream.flush()
            outputStream.close()
            inputStream.close()

            return@withContext destinationFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext null
        }
    }

    suspend fun copyUserPickedAudio(
        uri: Uri,
        customName: String
    ): LocalCustomAzan? = withContext(Dispatchers.IO) {
        try {
            val filename = "user_azan_${System.currentTimeMillis()}.mp3"
            val destinationFile = File(azanFolder, filename)

            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destinationFile).use { output ->
                    input.copyTo(output)
                }
            }

            if (destinationFile.exists() && destinationFile.length() > 0) {
                return@withContext LocalCustomAzan(
                    id = filename,
                    titleArabic = customName,
                    filePath = destinationFile.absolutePath,
                    isDownloadedFromNet = false
                )
            }
            null
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext null
        }
    }
}
