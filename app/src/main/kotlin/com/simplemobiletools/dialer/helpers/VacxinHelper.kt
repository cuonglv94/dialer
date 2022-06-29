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
import java.lang.Math.abs
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

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
                    val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
                    val currentDate: String = simpleDateFormat.format(Date())
                    Log.d("currentDate",currentDate)
                    for (i in 0 until jsonArray.length()) {
                        val babyList: MutableList<Vacxin> = ArrayList()
                        val name = jsonArray.getJSONObject(i).getString("kh_ten")
                        val date = jsonArray.getJSONObject(i).getString("kh_ngay_sinh")
                        var dateStr = date.substring(0,10)
                        if(dateStr.contains("-")) {
                            val arTemp: List<String> = dateStr.split("-")
                            val arTempReve = arTemp.reversed()
                            dateStr = "  |   " + arTempReve.joinToString(separator = "/")
                        }
                        val strArr: List<String> = name.split(" ")
                        val subArr = strArr.subList(0, strArr.size - 1)
                        val strName = subArr.joinToString(separator = " ") + dateStr
                        val vacxin = jsonArray.getJSONObject(i).getString("kh_vac_xin")
                        val vxArray = JSONTokener(vacxin).nextValue() as JSONArray
                        for (i in 0 until vxArray.length()) {
                            val vxName = vxArray.getJSONObject(i).getString("vx_ten")
                            var date = ""
                            var isTiem = 0
                            var isCurrent = false
                            if(!vxArray.getJSONObject(i).isNull("vx_ngay_tiem")){
                                val ngay_tiem = vxArray.getJSONObject(i).getString("vx_ngay_tiem")
                                val date1 = simpleDateFormat.parse(currentDate)
                                val date2 = simpleDateFormat.parse(ngay_tiem)
                                val difference = abs(date1.time - date2.time)/(24 * 60 * 60 * 1000)
                                isCurrent = difference.toString() == "2"
                                date = ngay_tiem.substring(0,10)
                                isTiem = 1
                            } else if(!vxArray.getJSONObject(i).isNull("vx_ngay_hen")){
                                val ngay_hen = vxArray.getJSONObject(i).getString("vx_ngay_hen")
                                date = ngay_hen.substring(0,10)
                            } else{
                                date = "Chưa có lịch"
                                isTiem = 2
                            }
                            if(babyList.firstOrNull  { it.date == date } == null){
                                babyList.add(Vacxin(date,vxName,isTiem,isCurrent))
                            } else{
                                babyList?.find { it.date == date }?.vxName += "-$vxName"
                            }

                        }
                        babyList.sortBy { it.date }
                        babyList.reverse()
                        Log.d("36care", babyList.toString())
                        Log.d("baby list",babyList.toString())
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
