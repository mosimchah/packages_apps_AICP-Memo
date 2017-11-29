package com.srbodroid.aicpmemo

import android.Manifest
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.View.OnClickListener
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Toast

import com.github.clans.fab.FloatingActionButton
import com.github.clans.fab.FloatingActionMenu
import com.pes.androidmaterialcolorpickerdialog.ColorPicker

import java.util.UUID

class MainActivity : AppCompatActivity(), OnClickListener {

    private var drawView: DrawingView? = null
    private val MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: Int = 0
    private var selectedColorR = 38
    private var selectedColorG = 50
    private var selectedColorB = 56
    private var selectedColorRGB: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FullScreencall()
        setContentView(R.layout.activity_main)

        // Get intent, action and MIME type
        val intent = intent
        val action = intent.action
        val type = intent.type

        if (Intent.ACTION_SEND == action && type != null) {
            if (type.startsWith("image/")) {
                handleSendImage(intent) // Handle single image being sent
            }
        } else {
            Toast.makeText(this, "Nema ulaza", Toast.LENGTH_LONG).show()
        }
        //        else {
        // Handle other intents, such as being started from the home screen
        //            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        //            fab.setOnClickListener(new View.OnClickListener() {
        //                @Override
        //                public void onClick(View view) {
        //                    Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
        //                            .setAction("Action", null).show();
        //                }
        //            });

        drawView = findViewById(R.id.drawing) as DrawingView

        drawView!!.setBrushSize(10f)

        val brush_size = findViewById(R.id.draw_btn) as FloatingActionButton
        brush_size.setOnClickListener(this)

        val erase_btn = findViewById(R.id.erase_btn) as FloatingActionButton
        erase_btn.setOnClickListener(this)

        val color_fill = findViewById(R.id.color_fill) as FloatingActionButton
        color_fill.setOnClickListener(this)

        val color_pick = findViewById(R.id.color_pick) as FloatingActionButton
        color_pick.setOnClickListener(this)

        val save_btn = findViewById(R.id.save_btn) as FloatingActionButton
        save_btn.setOnClickListener(this)

        //            FloatingActionButton screenShotButton = new FloatingActionButton(this);
        //
        //            if (RootUtil.isDeviceRooted()){
        //                final FloatingActionMenu fabMenu = (FloatingActionMenu) findViewById(R.id.menu);
        //                fabMenu.addMenuButton(screenShotButton);
        //            }

        //        }

    }

    private fun handleSendImage(intent: Intent) {
        val imageUri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
        if (imageUri != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    // We will need to request the permission
                    if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        // Explain to the user why we need to read the storage
                        val whyWeNeedPermission = Toast.makeText(applicationContext,
                                "We need permission to access that image.", Toast.LENGTH_SHORT)
                        whyWeNeedPermission.show()
                    }
                    requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                            MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE)


                } else {
                    // The permission is granted, we can perform the action
                    drawView = findViewById(R.id.drawing) as DrawingView
                    drawView!!.placeImage(imageUri)
                }
            } else {
                // The permission is granted, we can perform the action
                drawView = findViewById(R.id.drawing) as DrawingView
                drawView!!.placeImage(imageUri)
            }
        }
    }

    override fun onClick(view: View) {
        //respond to clicks
        val defaultColorG: Int
        val defaultColorB: Int
        val defaultColorR: Int
        if (view.id == R.id.color_fill) {
            defaultColorR = selectedColorR
            defaultColorG = selectedColorG
            defaultColorB = selectedColorB
            val cp = ColorPicker(this@MainActivity, defaultColorR, defaultColorG, defaultColorB)
            cp.show()
            /* On Click listener for the dialog, when the user select the color */
            val okColor = cp.findViewById(R.id.okColorButton) as Button
            okColor.setOnClickListener {
                /* You can get single channel (value 0-255) */
                selectedColorR = cp.red
                selectedColorG = cp.green
                selectedColorB = cp.blue

                /* Or the android RGB Color (see the android Color class reference) */
                selectedColorRGB = cp.color
                drawView!!.startNew(selectedColorRGB)

                cp.dismiss()
                closeFab()
            }
        } else if (view.id == R.id.color_pick) {
            defaultColorR = selectedColorR
            defaultColorG = selectedColorG
            defaultColorB = selectedColorB
            val cp = ColorPicker(this@MainActivity, defaultColorR, defaultColorG, defaultColorB)
            cp.show()
            /* On Click listener for the dialog, when the user select the color */
            val okColor = cp.findViewById(R.id.okColorButton) as Button
            okColor.setOnClickListener {
                /* You can get single channel (value 0-255) */
                selectedColorR = cp.red
                selectedColorG = cp.green
                selectedColorB = cp.blue

                /* Or the android RGB Color (see the android Color class reference) */
                selectedColorRGB = cp.color
                val hexColor = String.format("#%06X", 0xFFFFFF and selectedColorRGB)
                drawView!!.setColor(hexColor)

                cp.dismiss()
                closeFab()
            }
        } else if (view.id == R.id.draw_btn) {
            //draw button clicked
            val brushDialog = Dialog(this)
            brushDialog.setTitle("Brush size:")
            brushDialog.setContentView(R.layout.brush_size)

            val seekBar = brushDialog.findViewById(R.id.seekBar) as SeekBar
            val size = (Math.round(drawView!!.lastBrushSize) + 5) * 10
            val brushPreview = brushDialog.findViewById(R.id.brush_preview) as ImageView
            seekBar.progress = size
            val sizeParams = LinearLayout.LayoutParams(size, size)
            brushPreview.layoutParams = sizeParams

            val seekBarChangeListener = object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    val width = (progress + 5) * 10
                    val height = (progress + 5) * 10
                    val params = LinearLayout.LayoutParams(width, height)
                    brushPreview.layoutParams = params
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {

                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {

                }
            }

            seekBar.setOnSeekBarChangeListener(seekBarChangeListener)

            val setSize = brushDialog.findViewById(R.id.get_size) as Button
            setSize.setOnClickListener {
                val finalSize = seekBar.progress
                drawView!!.setBrushSize(finalSize.toFloat())
                drawView!!.lastBrushSize = finalSize
                drawView!!.setErase(false)
                brushDialog.hide()
                closeFab()
            }

            brushDialog.show()
        } else if (view.id == R.id.erase_btn) {
            //switch to erase - choose size
            val brushDialog = Dialog(this)
            brushDialog.setTitle("Eraser size:")
            brushDialog.setContentView(R.layout.brush_size)

            val seekBar = brushDialog.findViewById(R.id.seekBar) as SeekBar
            val size = (Math.round(drawView!!.lastBrushSize) + 5) * 10
            val brushPreview = brushDialog.findViewById(R.id.brush_preview) as ImageView
            seekBar.progress = size
            val sizeParams = LinearLayout.LayoutParams(size, size)
            brushPreview.layoutParams = sizeParams

            val seekBarChangeListener = object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    val width = (progress + 5) * 10
                    val height = (progress + 5) * 10
                    val params = LinearLayout.LayoutParams(width, height)
                    brushPreview.layoutParams = params
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {

                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {

                }
            }

            seekBar.setOnSeekBarChangeListener(seekBarChangeListener)

            val setSize = brushDialog.findViewById(R.id.get_size) as Button
            setSize.setOnClickListener {
                val finalSize = seekBar.progress
                drawView!!.setBrushSize(finalSize.toFloat())
                drawView!!.lastBrushSize = finalSize
                drawView!!.setErase(true)
                brushDialog.hide()
                closeFab()
            }

            brushDialog.show()

        } else if (view.id == R.id.save_btn) {
            //save drawing
            val saveDialog = AlertDialog.Builder(this)
            saveDialog.setTitle("Save drawing")
            saveDialog.setMessage("Save drawing to device Gallery?")
            saveDialog.setPositiveButton("Yes") { dialog, which ->
                //save drawing
                drawView!!.isDrawingCacheEnabled = true
                val imgSaved = MediaStore.Images.Media.insertImage(
                        contentResolver, drawView!!.drawingCache,
                        UUID.randomUUID().toString() + ".png", "drawing")
                if (imgSaved != null) {
                    val savedToast = Toast.makeText(applicationContext,
                            "Drawing saved to Gallery!", Toast.LENGTH_SHORT)
                    savedToast.show()
                } else {
                    val unsavedToast = Toast.makeText(applicationContext,
                            "Oops! Image could not be saved.", Toast.LENGTH_SHORT)
                    unsavedToast.show()
                }
                drawView!!.destroyDrawingCache()
            }
            saveDialog.setNegativeButton("Cancel") { dialog, which -> dialog.dismiss() }
            saveDialog.show()
        }
    }

    fun FullScreencall() {
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) { // lower api
            val v = this.window.decorView
            v.systemUiVisibility = View.GONE
        } else if (Build.VERSION.SDK_INT >= 19) {
            //for new api versions.
            val decorView = window.decorView
            val uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            decorView.systemUiVisibility = uiOptions
        }
    }

    fun closeFab() {
        val fabMenu = findViewById(R.id.menu) as FloatingActionMenu
        fabMenu.close(true)
    }
}
