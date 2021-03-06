package com.simplemobiletools.dialer.adapters

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.ImageView
import android.widget.TextView
import com.simplemobiletools.dialer.R
import com.simplemobiletools.dialer.models.Vacxin
import java.util.HashMap
class ExpandableAdapter internal constructor(
    private val context: Context,
    private val titleList: List<String>,
    private val dataList: HashMap<String, List<Vacxin>>) : BaseExpandableListAdapter() {

    override fun getChild(listPosition: Int, expandedListPosition: Int): Any {
        return this.dataList[this.titleList[listPosition]]!![expandedListPosition]
    }

    override fun getChildId(listPosition: Int, expandedListPosition: Int): Long {
        return expandedListPosition.toLong()
    }

    override fun getChildView(listPosition: Int, expandedListPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val vacxin = getChild(listPosition, expandedListPosition) as Vacxin
        if (convertView == null)
        {
            val layoutInflater = this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = layoutInflater.inflate(R.layout.list_item, null)
        }

        val date = convertView!!.findViewById<TextView>(R.id.date)
        date.text = vacxin.date

        val vxName = convertView!!.findViewById<TextView>(R.id.vxName)
        vxName.text = vacxin.vxName
        val imageview0 = convertView!!.findViewById<ImageView>(R.id.imageview0)
        if(vacxin.isCurrent){
            imageview0.setBackgroundResource(R.drawable.ic_baseline_arrow_forward_24)
        } else {
            imageview0.setBackgroundColor(Color.TRANSPARENT)
        }
        val imageview = convertView!!.findViewById<ImageView>(R.id.imageview)
        if(vacxin.image == 0){
            imageview.setBackgroundResource(R.drawable.ic_baseline_calendar_today_24)
        }else{
            imageview.setBackgroundResource(R.drawable.ic_baseline_history_24)
        }

        return convertView
    }

    override fun getChildrenCount(listPosition: Int): Int {
        return this.dataList[this.titleList[listPosition]]!!.size
    }

    override fun getGroup(listPosition: Int): Any {
        return this.titleList[listPosition]
    }

    override fun getGroupCount(): Int {
        return this.titleList.size
    }

    override fun getGroupId(listPosition: Int): Long {
        return listPosition.toLong()
    }

    override fun getGroupView(listPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val listTitle = getGroup(listPosition) as String
        if (convertView == null)
        {
            val layoutInflater = this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = layoutInflater.inflate(R.layout.list_group, null)
        }

        val listTitleTextView = convertView!!.findViewById<TextView>(R.id.listTitle)
        listTitleTextView.setTypeface(null, Typeface.BOLD)
        listTitleTextView.text = listTitle

        return convertView
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun isChildSelectable(listPosition: Int, expandedListPosition: Int): Boolean {
        return true
    }
}
