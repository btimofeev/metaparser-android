/*
 * Copyright (c) 2019, 2021 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

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
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import org.emunix.metaparser.R
import org.emunix.metaparser.databinding.ActivityMainBinding
import org.emunix.metaparser.helper.ThemeHelper
import org.emunix.metaparser.helper.showToast
import org.emunix.metaparser.helper.visible
import org.emunix.metaparser.preferences.ApplicationPreferences
import org.emunix.metaparser.ui.dialog.NewGameDialog
import org.emunix.metaparser.ui.dialog.NewGameDialogListener
import org.emunix.metaparser.ui.view.TopSmoothScroller
import javax.inject.Inject


const val REQUEST_SPEECH_TO_TEXT = 1001

@AndroidEntryPoint
class GameActivity : AppCompatActivity() {

    private val viewModel by viewModels<GameViewModel>()

    private lateinit var binding: ActivityMainBinding
    private lateinit var listAdapter: GameAdapter
    private lateinit var newGameDialogListener: NewGameDialogListener

    @Inject lateinit var preferences: ApplicationPreferences
    @Inject lateinit var themeHelper: ThemeHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        binding.appbar.setExpanded(false)

        val layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        layoutManager.stackFromEnd = true
        binding.recyclerView.layoutManager = layoutManager
        listAdapter = GameAdapter()
        binding.recyclerView.adapter = listAdapter

        viewModel.progress.observe(this, { showProgressState ->
            binding.progressBar.visible(showProgressState)
            binding.recyclerView.visible(!showProgressState)
            binding.editText.visible(!showProgressState)
            binding.enterButton.visible(!showProgressState)
            binding.errorMessage.visible(false)
            setVoiceButtonVisibility(!showProgressState)
        })

        viewModel.fatalError.observe(this, { message ->
            binding.errorMessage.text = getString(R.string.critical_error, message)
            binding.progressBar.visible(false)
            binding.recyclerView.visible(false)
            binding.editText.visible(false)
            binding.enterButton.visible(false)
            binding.errorMessage.visible(true)
            setVoiceButtonVisibility(false)
        })

        viewModel.message.observe(this, { message ->
            message.getContentIfNotHandled()?.let { text ->
                Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
            }
        })

        viewModel.history.observe(this, { history ->
            listAdapter.setItems(history)
            listAdapter.notifyDataSetChanged()
            val smoothScroller = TopSmoothScroller(this)
            val target = listAdapter.itemCount - 1
            if (target >= 0) {
                smoothScroller.targetPosition = target
                layoutManager.startSmoothScroll(smoothScroller)
            }
        })

        binding.editText.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                val text = binding.editText.text.toString()
                viewModel.sendTextToGame(text)
                binding.editText.text?.clear()
                return@OnKeyListener true
            }
            false
        })

        viewModel.pinToolbar.observe(this, { pin ->
            val params = binding.toolbar.layoutParams as AppBarLayout.LayoutParams
            if (pin)
                params.scrollFlags = AppBarLayout.LayoutParams.SCROLL_FLAG_NO_SCROLL
            else
                params.scrollFlags = AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS
        })

        viewModel.showSaveMenu.observe(this, { trigger ->
            trigger.getContentIfNotHandled()?.let {
                showSaveMenu()
            }
        })

        viewModel.showLoadMenu.observe(this, { trigger ->
            trigger.getContentIfNotHandled()?.let {
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

        binding.enterButton.setOnClickListener {
            binding.editText.dispatchKeyEvent(KeyEvent(0, 0, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER, 0))
            binding.editText.dispatchKeyEvent(KeyEvent(0, 0, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER, 0))
        }

        binding.voiceButton.setOnClickListener {
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
                    binding.editText.append(result[0])
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        when (preferences.theme) {
            ThemeHelper.LIGHT_MODE -> menu.findItem(R.id.theme_light).isChecked = true
            ThemeHelper.DARK_MODE -> menu.findItem(R.id.theme_dark).isChecked = true
            ThemeHelper.DEFAULT_MODE -> menu.findItem(R.id.theme_default).isChecked = true
        }

        menu.findItem(R.id.action_show_voice_button).isChecked = preferences.showVoiceButton

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
                setTheme(ThemeHelper.LIGHT_MODE)
                item.isChecked = true
            }
            R.id.theme_dark -> {
                setTheme(ThemeHelper.DARK_MODE)
                item.isChecked = true
            }
            R.id.theme_default -> {
                setTheme(ThemeHelper.DEFAULT_MODE)
                item.isChecked = true
            }
            R.id.action_show_voice_button -> {
                item.isChecked = !item.isChecked
                preferences.showVoiceButton = item.isChecked
                binding.voiceButton.visible(preferences.showVoiceButton)
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun showSaveMenu() {
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

    private fun showLoadMenu() {
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

    private fun setTheme(theme: String) {
        preferences.theme = theme
        themeHelper.applyTheme(theme)
    }

    private fun setVoiceButtonVisibility(state: Boolean) {
        if (preferences.showVoiceButton and state)
            binding.voiceButton.visible(true)
        else
            binding.voiceButton.visible(false)
    }
}
