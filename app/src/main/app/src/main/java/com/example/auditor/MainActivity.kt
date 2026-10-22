package com.example.auditor

import android.app.Activity
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.widget.ScrollView
import android.widget.TextView

class MainActivity : Activity() {

    private val dangerous = setOf(
        "android.permission.READ_SMS",
        "android.permission.RECEIVE_SMS",
        "android.permission.READ_CONTACTS",
        "android.permission.CAMERA",
        "android.permission.RECORD_AUDIO",
        "android.permission.ACCESS_FINE_LOCATION",
        "android.permission.READ_CALL_LOG",
        "android.permission.BIND_ACCESSIBILITY_SERVICE"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val text = TextView(this).apply {
            setTextColor(Color.GREEN)
            setBackgroundColor(Color.BLACK)
            textSize = 13f
            setPadding(32, 32, 32, 32)
            movementMethod = ScrollingMovementMethod()
            text = buildReport()
        }

        val scroll = ScrollView(this)
        scroll.addView(text)
        setContentView(scroll)
    }

    private fun buildReport(): String {
        val pm = packageManager
        val apps = pm.getInstalledPackages(PackageManager.GET_PERMISSIONS)
        val sb = StringBuilder()
        sb.append("PERMISSION AUDIT REPORT\n")
        sb.append("Sirf user-installed apps dikhayi ja rahi hain.\n\n")

        val reports = apps
            .filter { (it.applicationInfo!!.flags and ApplicationInfo.FLAG_SYSTEM) == 0 }
            .map { pkg ->
                val requested = pkg.requestedPermissions?.toList() ?: emptyList()
                val risky = requested.filter { it in dangerous }
                val name = pkg.applicationInfo!!.loadLabel(pm).toString()
                Triple(name, pkg.packageName, risky)
            }
            .sortedByDescending { it.third.size }

        for ((name, pkg, risky) in reports) {
            sb.append("App: $name\n")
            sb.append("Package: $pkg\n")
            if (risky.isEmpty()) {
                sb.append("Risky permissions: koi nahi\n")
            } else {
                sb.append("Risky permissions (${risky.size}):\n")
                risky.forEach { sb.append("  - ${it.removePrefix("android.permission.")}\n") }
            }
            sb.append("\n")
        }
        return sb.toString()
    }
}
