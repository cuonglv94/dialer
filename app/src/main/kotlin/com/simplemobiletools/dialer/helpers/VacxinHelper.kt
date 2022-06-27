package com.simplemobiletools.dialer.helpers

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.simplemobiletools.commons.helpers.ensureBackgroundThread
import com.simplemobiletools.dialer.models.Baby
import com.simplemobiletools.dialer.models.Vacxin
import org.json.JSONArray
import org.json.JSONTokener

@RequiresApi(Build.VERSION_CODES.O)
fun getVacxinInfo(context: Context, number: String, callback: (Baby) -> Unit) {
    ensureBackgroundThread {
        val baby = Baby(HashMap())
        val url = "http://tc36.xyz:4000/api/timkh/$number"
        val queue = Volley.newRequestQueue(context)
        val vacxinList = HashMap<String, List<Vacxin>>()
        if (checkForInternet(context)) {
            val request = StringRequest(Request.Method.POST, url,
                { response ->
                    val jsonArray = JSONTokener(response).nextValue() as JSONArray
                    for (i in 0 until jsonArray.length()) {
                        val babyList: MutableList<Vacxin> = ArrayList()
                        val name = jsonArray.getJSONObject(i).getString("kh_ten")
                        val strArr: List<String> = name.split(" ")
                        val subArr = strArr.subList(0, strArr.size - 1)
                        val strName = subArr.joinToString(separator = " ")
                        val vacxin = jsonArray.getJSONObject(i).getString("kh_vac_xin")
                        val vxArray = JSONTokener(vacxin).nextValue() as JSONArray
                        for (i in 0 until vxArray.length()) {
                            val vxName = vxArray.getJSONObject(i).getString("vx_ten")
                            val ngay_hen: String? = vxArray.getJSONObject(i).getString("vx_ngay_hen")
                            var ngay_tiem: String? = ""
                            if (ngay_hen == "null") ngay_tiem = vxArray.getJSONObject(i).getString("vx_ngay_tiem")
                            val vx_tiem = vxArray.getJSONObject(i).getString("vx_tiem")
                            val date: String? = if (ngay_hen == "null") ngay_tiem else ngay_hen
                            val isTiem = if (vx_tiem.compareTo("Chưa chọn", ignoreCase = true) > 0)  0 else 1
                            babyList.add(Vacxin(date!!.substring(0,10),vxName,isTiem))
                        }
                        babyList.sortBy { it.image }
                        babyList.reverse()
                        vacxinList[strName] =babyList
                    }
                    baby.vacxinInfo = vacxinList
                    callback(baby)
                }, {})
            queue.add(request)
        }
    }
}
fun checkForInternet(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            else -> false
        }
    } else {
        @Suppress("DEPRECATION") val networkInfo =
            connectivityManager.activeNetworkInfo ?: return false
        @Suppress("DEPRECATION")
        return networkInfo.isConnected
    }
}
