package org.emunix.metaparser.ui.game

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.activity_main.*
import org.emunix.metaparser.R
import org.emunix.metaparser.helper.ThemeHelper
import org.emunix.metaparser.helper.showToast
import org.emunix.metaparser.helper.visible
import org.emunix.metaparser.ui.dialog.NewGameDialog
import org.emunix.metaparser.ui.dialog.NewGameDialogListener
import org.emunix.metaparser.ui.view.TopSmoothScroller


const val REQUEST_SPEECH_TO_TEXT = 1001

class GameActivity : AppCompatActivity() {

    private lateinit var listAdapter: GameAdapter
    private lateinit var viewModel: GameViewModel
    private lateinit var newGameDialogListener: NewGameDialogListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        appbar.setExpanded(false)

        val layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        layoutManager.stackFromEnd = true
        recyclerView.layoutManager = layoutManager
        listAdapter = GameAdapter()
        recyclerView.adapter = listAdapter

        viewModel = ViewModelProvider(this).get(GameViewModel::class.java)

        viewModel.getShowProgressState().observe(this, Observer { showProgressState ->
            progressBar.visible(showProgressState)
            recyclerView.visible(!showProgressState)
            editText.visible(!showProgressState)
            voiceButton.visible(!showProgressState)
            errorMessage.visible(false)
        })

        viewModel.getShowCriticalError().observe(this, Observer { message ->
            errorMessage.text = getString(R.string.critical_error, message)
            progressBar.visible(false)
            recyclerView.visible(false)
            editText.visible(false)
            voiceButton.visible(false)
            errorMessage.visible(true)
        })

        viewModel.getHistory().observe(this, Observer { history ->
            listAdapter.setItems(history)
            listAdapter.notifyDataSetChanged()
            val smoothScroller = TopSmoothScroller(this)
            val target = listAdapter.itemCount - 1
            if (target >= 0) {
                smoothScroller.targetPosition = target
                layoutManager.startSmoothScroll(smoothScroller)
            }
        })

        editText.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                val text = editText.text.toString()
                viewModel.sendTextToGame(text)
                editText.text?.clear()
                return@OnKeyListener true
            }
            false
        })

        viewModel.getPinToolbar().observe(this, Observer { pin ->
            val params = toolbar.layoutParams as AppBarLayout.LayoutParams
            if (pin)
                params.scrollFlags = AppBarLayout.LayoutParams.SCROLL_FLAG_NO_SCROLL
            else
                params.scrollFlags = AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS
        })

        viewModel.getShowSaveMenu().observe(this, Observer { trigger ->
            if(trigger) {
                showSaveMenu()
            }
        })

        viewModel.getShowLoadMenu().observe(this, Observer { trigger ->
            if(trigger) {
                showLoadMenu()
            }
        })

        viewModel.init()

        newGameDialogListener = object : NewGameDialogListener{
            override fun onDialogPositiveClick(dialog: DialogFragment) {
                viewModel.restartGame()
            }

            override fun onDialogNegativeClick(dialog: DialogFragment) {
                dialog.dismiss()
            }
        }

        voiceButton.setOnClickListener {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "ru-RU")

            try {
                startActivityForResult(intent, REQUEST_SPEECH_TO_TEXT)
            } catch (e: ActivityNotFoundException) {
                applicationContext.showToast(R.string.error_device_not_support_speech_to_text)
            }
        }

        if (savedInstanceState != null) {
            // after changing screen orientation the listener is not set
            val newGameDialog = supportFragmentManager.findFragmentByTag("new_game_dialog") as NewGameDialog?
            newGameDialog?.setListener(newGameDialogListener)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_SPEECH_TO_TEXT) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                if (result != null && result[0] != null)
                    viewModel.sendTextToGame(result[0])
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        when (viewModel.getAppTheme()) {
            ThemeHelper.LIGHT_MODE -> menu.findItem(R.id.theme_light).isChecked = true
            ThemeHelper.DARK_MODE -> menu.findItem(R.id.theme_dark).isChecked = true
            ThemeHelper.DEFAULT_MODE -> menu.findItem(R.id.theme_default).isChecked = true
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.action_new_game -> {
                val newGameDialog = NewGameDialog.newInstance(newGameDialogListener)
                newGameDialog.show(supportFragmentManager, "new_game_dialog")
            }
            R.id.action_save_game -> {
                showSaveMenu()
            }
            R.id.action_load_game -> {
                showLoadMenu()
            }
            R.id.theme_light -> {
                viewModel.setAppTheme(ThemeHelper.LIGHT_MODE)
                item.isChecked = true
            }
            R.id.theme_dark -> {
                viewModel.setAppTheme(ThemeHelper.DARK_MODE)
                item.isChecked = true
            }
            R.id.theme_default -> {
                viewModel.setAppTheme(ThemeHelper.DEFAULT_MODE)
                item.isChecked = true
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    fun showSaveMenu() {
        val saves = viewModel.getSaveStates()
        val items = mutableListOf<String>()
        for ((key, value) in saves) {
            val text = value ?: getString(R.string.save_menu_text_empty_slot)
            items.add("$key. $text")
        }
        MaterialAlertDialogBuilder(this, R.style.AppTheme_AlertDialogOverlay)
            .setTitle(R.string.action_save_game)
            .setItems(items.toTypedArray()) { _, which ->
                viewModel.saveState("${which + 1}.sav")
            }.setNegativeButton(R.string.dialog_save_load_negative_button) { dialog, _ ->
                dialog.cancel()
            }.create()
            .show()
    }

    fun showLoadMenu() {
        val saves = viewModel.getSaveStates()
        val items = mutableListOf<String>()
        val mapDialogItemToSave = mutableMapOf<Int, Int>()
        var couter = 0
        for ((key, value) in saves) {
            if (value != null) {
                items.add("$key. $value")
                mapDialogItemToSave[couter] = key
                couter++
            }
        }
        if (items.isEmpty()) {
            showToast(getString(R.string.saves_not_found))
        } else {
            MaterialAlertDialogBuilder(this, R.style.AppTheme_AlertDialogOverlay)
                .setTitle(R.string.action_load_game)
                .setItems(items.toTypedArray()) { _, which ->
                    viewModel.loadState("${mapDialogItemToSave[which]}.sav")
                }.setNegativeButton(R.string.dialog_save_load_negative_button) { dialog, _ ->
                    dialog.cancel()
                }.create()
                .show()
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.saveState()
    }
}
