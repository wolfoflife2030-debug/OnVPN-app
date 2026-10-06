package com.onvpn.app

import android.app.Activity
import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import com.wireguard.android.backend.BackendException
import com.wireguard.android.backend.GoBackend
import com.wireguard.android.backend.Statistics
import com.wireguard.android.backend.Tunnel
import com.wireguard.config.Config
import java.io.StringReader
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

/** On Vpn Android MVP. A valid WireGuard server config is required to establish a real tunnel. */
class MainActivity : Activity() {
    private val executor = Executors.newSingleThreadExecutor()
    private val statsExecutor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
    private lateinit var backend: GoBackend
    private lateinit var status: TextView
    private lateinit var details: TextView
    private lateinit var configInput: EditText
    private lateinit var connectButton: Button
    private lateinit var statsView: TextView
    private lateinit var tunnel: OnVpnTunnel
    private var pendingConfig: Config? = null
    private var busy = false

    companion object { private const val VPN_PERMISSION_REQUEST = 4317 }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        backend = GoBackend(applicationContext)
        tunnel = OnVpnTunnel { state -> runOnUiThread { renderState(state) } }
        buildUi()
        refreshState()
        scheduleStats()
    }

    private fun buildUi() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22), dp(20), dp(22), dp(20))
            setBackgroundColor(Color.rgb(7, 27, 43))
            layoutParams = ViewGroup.LayoutParams(-1, -1)
        }
        val scroll = ScrollView(this)
        val content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        scroll.addView(content)

        content.addView(label("ON VPN", 30, Color.WHITE, true).apply { gravity = Gravity.CENTER_HORIZONTAL })
        content.addView(label("PRIVATE. SIMPLE. CONNECTED.", 11, Color.rgb(24, 199, 156), true).apply {
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(0, dp(4), 0, dp(24))
        })

        val panel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(16), dp(22), dp(16), dp(22))
            setBackgroundColor(Color.rgb(13, 44, 63))
        }
        status = label("غير متصل", 24, Color.WHITE, true).apply { gravity = Gravity.CENTER }
        details = label("أدخل إعداد WireGuard الخاص بسيرفرك للبدء", 13, Color.LTGRAY, false).apply {
            gravity = Gravity.CENTER
            setPadding(0, dp(8), 0, dp(16))
        }
        connectButton = Button(this).apply {
            text = "اتصال"
            isAllCaps = false
            setOnClickListener { onConnectClicked() }
        }
        panel.addView(status)
        panel.addView(details)
        panel.addView(connectButton, LinearLayout.LayoutParams(-1, dp(52)))
        content.addView(panel)

        statsView = label("البيانات: —", 13, Color.LTGRAY, false).apply {
            gravity = Gravity.CENTER
            setPadding(0, dp(14), 0, dp(0))
        }
        content.addView(statsView)

        content.addView(label("إعداد WireGuard", 18, Color.WHITE, true).apply { setPadding(0, dp(24), 0, dp(8)) })
        content.addView(label("الصق ملف الإعداد الذي أنشأه خادمك. تتم معالجة المفتاح الخاص على الجهاز ولا ترسله إلى أي API.", 13, Color.LTGRAY, false))
        configInput = EditText(this).apply {
            hint = "[Interface]\nPrivateKey = ...\nAddress = ...\nDNS = ...\n\n[Peer]\nPublicKey = ...\nEndpoint = host:51820\nAllowedIPs = 0.0.0.0/0, ::/0"
            setHintTextColor(Color.rgb(130, 151, 163))
            setTextColor(Color.WHITE)
            textSize = 12f
            gravity = Gravity.TOP or Gravity.START
            minLines = 9
            setPadding(dp(12), dp(12), dp(12), dp(12))
            setBackgroundColor(Color.rgb(13, 44, 63))
            typeface = Typeface.MONOSPACE
        }
        content.addView(configInput, LinearLayout.LayoutParams(-1, dp(220)))
        content.addView(label("مهم: هذه نسخة MVP. لا يوجد سيرفر مجاني مضمّن؛ يلزم خادم WireGuard فعلي وإعداد صحيح. لا تستخدم إعدادات مجهولة.", 12, Color.rgb(255, 205, 112), false).apply {
            setPadding(0, dp(16), 0, dp(8))
        })
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)
    }

    private fun onConnectClicked() {
        if (busy) return
        val current = runCatching { backend.getState(tunnel) }.getOrDefault(Tunnel.State.DOWN)
        if (current == Tunnel.State.UP) {
            changeTunnel(false, null)
            return
        }
        val raw = configInput.text.toString().trim()
        if (raw.isBlank()) {
            Toast.makeText(this, "أدخل إعداد WireGuard أولًا", Toast.LENGTH_LONG).show()
            return
        }
        val parsed = try {
            Config.parse(StringReader(raw))
        } catch (e: Exception) {
            Toast.makeText(this, "الإعداد غير صالح: ${e.message ?: "تحقق من الملف"}", Toast.LENGTH_LONG).show()
            return
        }
        pendingConfig = parsed
        val permissionIntent = VpnService.prepare(this)
        if (permissionIntent != null) {
            @Suppress("DEPRECATION")
            startActivityForResult(permissionIntent, VPN_PERMISSION_REQUEST)
        } else {
            changeTunnel(true, parsed)
        }
    }

    @Deprecated("Activity result API kept minimal for compatibility with this MVP")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == VPN_PERMISSION_REQUEST) {
            if (resultCode == RESULT_OK) changeTunnel(true, pendingConfig)
            else Toast.makeText(this, "يلزم السماح باتصال VPN", Toast.LENGTH_LONG).show()
            pendingConfig = null
        }
    }

    private fun changeTunnel(up: Boolean, config: Config?) {
        if (busy) return
        busy = true
        connectButton.isEnabled = false
        status.text = if (up) "جارٍ الاتصال…" else "جارٍ الفصل…"
        executor.execute {
            val result = runCatching {
                backend.setState(tunnel, if (up) Tunnel.State.UP else Tunnel.State.DOWN, config)
            }
            runOnUiThread {
                busy = false
                connectButton.isEnabled = true
                result.onFailure { error ->
                    val message = when (error) {
                        is BackendException -> error.message ?: "تعذر إنشاء النفق"
                        else -> error.message ?: "حدث خطأ أثناء الاتصال"
                    }
                    details.text = message
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                }
                refreshState()
            }
        }
    }

    private fun refreshState() {
        val state = runCatching { backend.getState(tunnel) }.getOrDefault(Tunnel.State.DOWN)
        renderState(state)
    }

    private fun scheduleStats() {
        statsExecutor.scheduleWithFixedDelay({
            val state = runCatching { backend.getState(tunnel) }.getOrDefault(Tunnel.State.DOWN)
            val stats = if (state == Tunnel.State.UP) {
                runCatching { backend.getStatistics(tunnel) }.getOrNull()
            } else null
            runOnUiThread { renderStats(stats) }
        }, 0, 1500, TimeUnit.MILLISECONDS)
    }

    private fun renderStats(stats: Statistics?) {
        if (stats == null) {
            statsView.text = "البيانات: —"
            return
        }
        statsView.text = "استقبال: ${formatBytes(stats.totalRx())}   •   إرسال: ${formatBytes(stats.totalTx())}"
    }

    private fun formatBytes(value: Long): String {
        if (value < 1024) return "$value B"
        val units = arrayOf("KB", "MB", "GB", "TB")
        var v = value.toDouble()
        var i = -1
        while (v >= 1024 && i < units.lastIndex) { v /= 1024.0; i++ }
        return String.format(java.util.Locale.US, "%.1f %s", v, units[i])
    }

    private fun renderState(state: Tunnel.State) {
        when (state) {
            Tunnel.State.UP -> {
                status.text = "متصل"
                status.setTextColor(Color.rgb(24, 199, 156))
                details.text = "نفق WireGuard نشط"
                connectButton.text = "فصل الاتصال"
            }
            Tunnel.State.TOGGLE -> {
                status.text = "جارٍ التبديل…"
                status.setTextColor(Color.WHITE)
                connectButton.text = "يرجى الانتظار"
            }
            else -> {
                status.text = "غير متصل"
                status.setTextColor(Color.WHITE)
                details.text = "اتصل بخادم WireGuard الذي تديره أو تثق به"
                connectButton.text = "اتصال"
            }
        }
        connectButton.isEnabled = !busy
    }

    private fun label(text: String, size: Int, color: Int, bold: Boolean) = TextView(this).apply {
        this.text = text
        textSize = size.toFloat()
        setTextColor(color)
        if (bold) setTypeface(typeface, Typeface.BOLD)
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    override fun onDestroy() {
        statsExecutor.shutdownNow()
        executor.shutdownNow()
        super.onDestroy()
    }

    private class OnVpnTunnel(private val stateCallback: (Tunnel.State) -> Unit) : Tunnel {
        override fun getName(): String = "OnVpn"
        override fun onStateChange(newState: Tunnel.State) = stateCallback(newState)
    }
}
