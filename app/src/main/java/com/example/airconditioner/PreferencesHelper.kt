package com.example.airconditoner

import android.content.Context
import android.content.SharedPreferences

class PreferencesHelper(context: Context) {
    companion object {
        private const val PREFS_NAME = "ac_preferences"
        private const val KEY_ESP32_IP = "esp32_ip"
    }

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // Lưu địa chỉ IP của ESP32
    fun setEsp32Ip(ip: String) {
        sharedPreferences.edit().putString(KEY_ESP32_IP, ip).apply()
    }

    // Lấy địa chỉ IP của ESP32
    fun getEsp32Ip(): String? {
        return sharedPreferences.getString(KEY_ESP32_IP, null)
    }

    // Xóa địa chỉ IP của ESP32
    fun clearEsp32Ip() {
        sharedPreferences.edit().remove(KEY_ESP32_IP).apply()
    }
}