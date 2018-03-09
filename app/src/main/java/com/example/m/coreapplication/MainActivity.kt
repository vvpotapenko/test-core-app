package com.example.m.coreapplication

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.TextView
import com.example.m.coreapplication.data.ModuleVersion
import com.example.m.coreapplication.data.device.IConnectedDeviceListener
import com.example.m.coreapplication.data.device.impl.ConnectedDeviceService
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), IConnectedDeviceListener {

    private val connectedDeviceService by lazy { ConnectedDeviceService(this) }

    private var currentDeviceVersion: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        connectDeviceService()
    }

    private fun connectDeviceService() {
        message_txt.visibility = View.GONE

        connectedDeviceService.addListener(this)
        connectedDeviceService.initialize()

        showProgress()
    }

    override fun onDestroy() {
        destroyDeviceService()
        super.onDestroy()
    }

    private fun destroyDeviceService() {
        connectedDeviceService.removeListener(this)
        connectedDeviceService.destroy()
    }

    override fun onDeviceConnected() {
        connectedDeviceService.getVersion().subscribe(
                { result ->
                    currentDeviceVersion = result
                    current_device_version_txt.text = currentDeviceVersion
                    hideProgress()

                    requestDeviceVersions()
                },
                { err ->
                    showMessage(err.message)

                    err.printStackTrace()
                    hideProgress()
                }
        )
    }

    private fun requestDeviceVersions() {
        showProgress()
        CoreApp.instance.modulesManager.getDeviceVersions().subscribe(
                { result ->
                    deviceVersionsLoaded(result)
                    hideProgress()
                },
                { err ->
                    showMessage(err.message)

                    err.printStackTrace()
                    hideProgress()
                }
        )
    }

    private fun deviceVersionsLoaded(versions: List<ModuleVersion>) {
        versions.forEach { createVersionView(it) }
    }

    private fun createVersionView(version: ModuleVersion) {
        val v = View.inflate(this, R.layout.activity_main_version, null)

        v.findViewById<TextView>(R.id.version_txt).text = version.version

        val statusRes = if (version.latest) R.string.version_latest else R.string.version_obsolete
        v.findViewById<TextView>(R.id.status_txt).text = getString(statusRes)

        v.findViewById<TextView>(R.id.path_txt).text = version.path

        val installBtn = v.findViewById<View>(R.id.install_btn)
        if (version.version == currentDeviceVersion) {
            installBtn.visibility = View.GONE
        }
        installBtn.setOnClickListener { installClicked(version) }

        versions_layout.addView(v)
    }

    private fun installClicked(version: ModuleVersion) {
        installVersion(version)
    }

    private fun installVersion(version: ModuleVersion) {
        showProgress()
        CoreApp.instance.modulesManager.installApk(version.path).subscribe(
                { uri ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        val intent = Intent(Intent.ACTION_VIEW, uri)
                        intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
                        intent.setDataAndType(uri, "application/vnd.android.package-archive")
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

                        startActivity(intent)
                    } else {
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.setDataAndType(uri, "application/vnd.android.package-archive")
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                    }

                    showMessage("После установке модуля перезапустите приложение Core Application")

                    hideProgress()
                },
                { err ->
                    showMessage(err.message)

                    err.printStackTrace()
                    hideProgress()
                }
        )
    }

    private fun showMessage(message: String?) {
        message_txt.visibility = View.VISIBLE
        message_txt.text = if (message.isNullOrBlank()) "Произошла ошибка" else message
    }

    private fun showProgress() {
        content_panel.visibility = View.GONE
        progress_bar.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        content_panel.visibility = View.VISIBLE
        progress_bar.visibility = View.GONE
    }
}
