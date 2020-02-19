package com.hadi.menu.overflow

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.*

import androidx.annotation.MenuRes

import androidx.appcompat.view.menu.MenuBuilder
import android.widget.ImageView
import android.widget.TextView
import android.widget.AbsListView
import com.mikepenz.iconics.utils.IconicsMenuInflaterUtil
import com.skydoves.powermenu.CustomPowerMenu
import com.skydoves.powermenu.MenuBaseAdapter
import com.skydoves.powermenu.OnMenuItemClickListener
import kotlin.math.min

open class OverFlowMenu private constructor(context: Context, menuRes: Int) {

    private var powerMenu: CustomPowerMenu<IconPowerMenuItem, IconMenuAdapter>? = null
    private var onItemClickListener: OnItemClickListener? = null
    private var onDismissListener: OnDismissListener? = null

    init {
        powerMenu = CustomPowerMenu.Builder(context, IconMenuAdapter())
                .addItemList(fromMenu(menuRes, context))
                .setMenuRadius(10f)
                .setMenuShadow(10f)
                .setBackgroundAlpha(0.2f)
                .setOnDismissListener {
                    onDismissed()
                }
                .setOnMenuItemClickListener(object : OnMenuItemClickListener<IconPowerMenuItem> {
                    override fun onItemClick(position: Int, item: IconPowerMenuItem?) {
                        onItemClickListener?.onItemClick(item!!.itemId!!)
                    }
                })
                .build() as CustomPowerMenu<IconPowerMenuItem, IconMenuAdapter>?
    }

    private fun fromMenu(menuResourceId: Int, context: Context): List<IconPowerMenuItem> {
        val menu = newMenuInstance(context)!!
        val menuInflater = MenuInflater(context)

        // IconicsMenuInflaterUtil will show Iconics icons
        IconicsMenuInflaterUtil.inflate(menuInflater, context, menuResourceId, menu)

//        var lastGroupId = menu.getItem(0).groupId

        val menuItems = ArrayList<IconPowerMenuItem>()

        for (i in 0 until menu.size()) {
            val mItem = menu.getItem(i)
            val dMenuItem = IconPowerMenuItem(mItem.icon, mItem.title.toString(), mItem.isVisible)

            if (mItem.itemId > 0) {
                dMenuItem.itemId = mItem.itemId
            }

            menuItems.add(dMenuItem)
        }

        return menuItems
    }

    @SuppressLint("RestrictedApi")
    private fun newMenuInstance(context: Context): Menu? {
        return MenuBuilder(context)
    }

    fun setOnMenuItemClickListener(onItemClickListener: OnItemClickListener): OverFlowMenu {
        this.onItemClickListener = onItemClickListener
        return this
    }

    fun setOnDismissListener(onDismissListener: OnDismissListener): OverFlowMenu {
        this.onDismissListener = onDismissListener
        return this
    }

    fun dismiss() {
        powerMenu?.dismiss()
    }

    fun show(anchor: View) {
        powerMenu?.apply {
            anchor.isFocusableInTouchMode = true
            anchor.requestFocus()
            anchor.setOnKeyListener { _, keyCode, event ->
                if (powerMenu != null && powerMenu!!.isShowing && event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_BACK) {
                    this@OverFlowMenu.dismiss()
                    return@setOnKeyListener true
                }

                return@setOnKeyListener false
            }

            var visibleCount = 0

            itemList.forEach {
                if (it.visible) {
                    visibleCount++
                }
            }

            val showingInTop = anchor.resources.displayMetrics.run {
                setWidth((contentViewWidth + 48 * density).toInt())
                val height = min(visibleCount * (contentViewHeight / itemList.size), (heightPixels * 0.6).toInt())
                setHeight(height)
                menuListView.layoutParams = menuListView.layoutParams.apply {
                    this.height = height
                }

                val location = IntArray(2)
                anchor.getLocationOnScreen(location)
                (heightPixels - location[1] - anchor.height) < height
            }

            val yOff = (anchor.height) * if (showingInTop) 0.0 else -0.55

            showAsDropDown(anchor, 0, yOff.toInt())
        }
    }

    private fun onDismissed() {
        onItemClickListener = null
        onDismissListener?.onDismiss()
        onDismissListener = null
        powerMenu = null
    }

    fun setVisibilityById(isVisible: Boolean, vararg ids: Int) {
        var idI = 0

        for (i in 0 until powerMenu!!.itemList.size) {
            powerMenu!!.itemList[i].apply {
                if (ids.contains(itemId!!)) {
                    visible = isVisible
                    idI++
                }
            }

            if (idI >= ids.size) {
                break
            }
        }
    }

    fun findItem(id: Int): IconPowerMenuItem {
        for (i in 0 until powerMenu!!.itemList.size) {
            val iconPowerMenuItem = powerMenu!!.itemList[i]

            if (id == iconPowerMenuItem.itemId!!) {
                return iconPowerMenuItem
            }
        }

        throw IllegalArgumentException("Can't found any IconPowerMenuItem by id = $id")
    }

    companion object {
        @JvmStatic
        fun createDefaultMenu(context: Context, @MenuRes menuRes: Int): OverFlowMenu {
            return OverFlowMenu(context, menuRes)
        }

    }


    interface OnItemClickListener {
        fun onItemClick(itemId: Int)
    }

    interface OnDismissListener {
        fun onDismiss()
    }
}

data class IconPowerMenuItem(var icon: Drawable?, var title: String, var visible: Boolean) {
    var itemId: Int? = null
}

class IconMenuAdapter : MenuBaseAdapter<IconPowerMenuItem>() {

    override fun getView(index: Int, lastView: View?, viewGroup: ViewGroup): View {
        var view = lastView
        val context = viewGroup.context

        if (view == null) {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = inflater.inflate(R.layout.item_menu, viewGroup, false)
        }

        val (icon, title, visible) = getItem(index) as IconPowerMenuItem

        if (visible) {
            if (view!!.visibility == View.GONE) {
                view.layoutParams = AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, AbsListView.LayoutParams.WRAP_CONTENT)
                view.visibility = View.VISIBLE
            }
        } else {
            if (view!!.visibility == View.VISIBLE) {
                view.layoutParams = AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1)
                view.visibility = View.GONE
            }
        }

        val imgIcon = view.findViewById<ImageView>(R.id.imgIcon)

        if (icon == null) {
            imgIcon.visibility = View.GONE
        } else {
            imgIcon.visibility = View.VISIBLE
            imgIcon.setImageDrawable(icon)
        }

        val txtTitle = view.findViewById<TextView>(R.id.txtTitle)
        txtTitle.text = title
        return super.getView(index, view, viewGroup)
    }
}
