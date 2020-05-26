package com.example.dragdropdemo

import android.content.ClipData
import android.content.ClipDescription
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.DragEvent
import android.view.View
import android.widget.Button
import androidx.cardview.widget.CardView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    companion object {
        // Default card elevation.
        const val CARD_ELEVATION_DEFAULT_DP = 2F
        // Card elevation once the dragging has started.
        const val CARD_ELEVATION_DRAG_START_DP = 8F
        // Card elevation once the color is dragged over one of the areas.
        const val CARD_ELEVATION_DRAG_ENTER_DP = 16F
    }
    private val onLongClickListener = View.OnLongClickListener { view: View ->
        (view as? Button)?.let {

            // First we create the `ClipData.Item` that we will need for the `ClipData`.
            // The `ClipData` carries the information of what is being dragged.
            // If you look at the main activity layout XML, you'll see that we've stored
            // color values for each of the FABs as their tags.
            val item = ClipData.Item(it.tag as? CharSequence)

            // We create a `ClipData` for the drag action and save the color as plain
            // text using `ClipDescription.MIMETYPE_TEXT_PLAIN`.
            val dragData = ClipData(
                    it.tag as? CharSequence,
                    arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN),
                    item)

            // Instantiates the drag shadow builder, which is the class we will use
            // to draw a shadow of the dragged object. The implementation details
            // are in the rest of the article.
            val myShadow = MyDragShadowBuilder(it)

            // Start the drag. The new method is called `startDragAndDrop()` instead
            // of `startDrag()`, so we'll use it on the newer API.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                it.startDragAndDrop(dragData, myShadow, null, 0)
            } else {
                it.startDrag(dragData, myShadow, null, 0)
            }

            true
        }
        false
    }
    private val onDragListener = View.OnDragListener { view, dragEvent ->
        (view as? CardView)?.let {
            when (dragEvent.action) {
                // Once the drag event has started, we elevate all the views that are listening.
                // In our case, that's two of the areas.
                DragEvent.ACTION_DRAG_STARTED -> {
                    it.cardElevation = CARD_ELEVATION_DRAG_START_DP
                    return@OnDragListener true
                }
                // Once the drag gesture enters a certain area, we want to elevate it even more.
                DragEvent.ACTION_DRAG_ENTERED -> {
                    it.cardElevation = CARD_ELEVATION_DRAG_ENTER_DP
                    return@OnDragListener true
                }
                // No need to handle this for our use case.
                DragEvent.ACTION_DRAG_LOCATION -> {
                    return@OnDragListener true
                }
                // Once the drag gesture exits the area, we lower the elevation down to the previous one.
                DragEvent.ACTION_DRAG_EXITED -> {
                    it.cardElevation = CARD_ELEVATION_DRAG_START_DP
                    return@OnDragListener true
                }
                // Once the color is dropped on the area, we want to paint it in that color.
                DragEvent.ACTION_DROP -> {
                    // Read color data from the clip data and apply it to the card view background.
                    val item: ClipData.Item = dragEvent.clipData.getItemAt(0)
                    val colorHex = item.text
                    it.setCardBackgroundColor(Color.parseColor(colorHex.toString()))
                    return@OnDragListener true
                }
                // Once the drag has ended, revert card views to the default elevation.
                DragEvent.ACTION_DRAG_ENDED -> {
                    it.cardElevation = CARD_ELEVATION_DEFAULT_DP
                    return@OnDragListener true
                }
                else -> return@OnDragListener false
            }
        }
        false
    }
    private class MyDragShadowBuilder(v: View) : View.DragShadowBuilder(v) {

        private val shadowBorder = ColorDrawable(Color.BLACK)

        private val shadow = ColorDrawable(Color.parseColor(v.tag.toString()))

        // Defines a callback that sends the drag shadow dimensions and touch point back to the system.
        override fun onProvideShadowMetrics(size: Point, touch: Point) {
            // First, we define the shadow width and height. In our example, it will be
            // half of the size of the view that's been dragged.
            val width: Int = view.width / 2
            val height: Int = view.height / 2

            // The drag shadow is a `ColorDrawable`. This sets its dimensions to be the same as the
            // `Canvas` that the system will provide. We leave some room (four pixels) for the shadow border.
            shadow.setBounds(4, 4, width - 4, height - 4)
            shadowBorder.setBounds(0, 0, width, height)

            // Sets the size parameter's width and height values.
            // These get back to the system through the size parameter.
            size.set(width, height)

            // Sets the touch point's position to be in the middle of the drag shadow.
            touch.set(width / 2, height / 2)
        }

        // Defines a callback that draws the drag shadow in a `Canvas` that the
        // system constructs from the dimensions passed in `onProvideShadowMetrics()`.
        override fun onDrawShadow(canvas: Canvas) {

            // Draws the border drawable first.
            shadowBorder.draw(canvas)

            // Draws the actual shadow drawable onto the `Canvas` passed in
            // from the system so that the shadow content is above its border.
            shadow.draw(canvas)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        area1.setOnDragListener(onDragListener)
        area2.setOnDragListener(onDragListener)

        fabRed.setOnLongClickListener(onLongClickListener)
        fabBlue.setOnLongClickListener(onLongClickListener)
        fabGreen.setOnLongClickListener(onLongClickListener)
        fabPurple.setOnLongClickListener(onLongClickListener)
        fabYellow.setOnLongClickListener(onLongClickListener)
    }
}
