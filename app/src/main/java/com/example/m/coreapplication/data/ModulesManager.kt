package com.example.m.coreapplication.data

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.support.v4.content.FileProvider
import com.example.m.coreapplication.IOUtils
import io.reactivex.Single
import org.json.JSONArray
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.util.*

class ModulesManager(private val context: Context) : BaseRx() {

    fun getDeviceVersions(): Single<List<ModuleVersion>> {
        val single = Single.create<List<ModuleVersion>> {
            try {
                val requestUrl = URL(DEVICE_MODULE_URL)
                val connection = requestUrl.openConnection()

                val content = IOUtils.readTextAndClose(connection.getInputStream())
                val result = mutableListOf<ModuleVersion>()
                val array = JSONArray(content)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val v = ModuleVersion(
                            obj.optString("version", "-"),
                            obj.optString("path", "-"),
                            obj.optBoolean("latest", false)
                    )
                    result.add(v)
                }
                it.onSuccess(result)
            } catch (e: Exception) {
                it.onError(e)
            }
        }
        return prepareRx(single)
    }

    @SuppressLint("SetWorldReadable")
    fun installApk(url: String): Single<Uri> {
        val single = Single.create<Uri> {
            try {
                val appFolder = prepareExtFolder()
                val apkFile = getApkFile(appFolder)

                val requestUrl = URL(url)
                val connection = requestUrl.openConnection()

                val input = connection.getInputStream()
                val output = FileOutputStream(apkFile)

                var count: Int
                val data = ByteArray(4096)

                count = input.read(data)
                while (count != -1) {
                    output.write(data, 0, count)
                    count = input.read(data)
                }
                input.close()
                output.close()

                apkFile.setReadable(true, false)
                val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    FileProvider.getUriForFile(context,
                            context.applicationContext.packageName,
                            apkFile)
                } else {
                    Uri.fromFile(apkFile)
                }

                it.onSuccess(uri)
            } catch (e: Exception) {
                it.onError(e)
            }
        }
        return prepareRx(single)
    }

    private fun getApkFile(appFolder: File): File {
        val baseApkName = "module"
        var file = File(appFolder, "$baseApkName.apk")

        var tryNumber = 0
        while (file.exists() || tryNumber >= 100) {
            file = File(appFolder, "$baseApkName$tryNumber.apk")

            tryNumber++
        }

        return file
    }

    private fun prepareExtFolder(): File {
        return context.getExternalFilesDir(null)
    }

    companion object {

        const val DEVICE_MODULE_URL = "https://raw.githubusercontent.com/vvpotapenko/test-device-service/master/dist/versions.json"
    }
}

data class ModuleVersion(val version: String, val path: String, val latest: Boolean)