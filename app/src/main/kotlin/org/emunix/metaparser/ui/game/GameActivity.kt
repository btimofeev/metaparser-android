package org.emunix.metaparser.ui.game

import android.os.Bundle
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import kotlinx.android.synthetic.main.activity_main.*
import org.emunix.metaparser.R
import org.emunix.metaparser.helper.ThemeHelper
import org.emunix.metaparser.helper.visible
import org.emunix.metaparser.ui.dialog.NewGameDialog
import org.emunix.metaparser.ui.dialog.NewGameDialogListener


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
        val smoothScroller = object : LinearSmoothScroller(this) {
            override fun getVerticalSnapPreference(): Int {
                return SNAP_TO_START
            }
        }

        viewModel = ViewModelProvider(this).get(GameViewModel::class.java)

        viewModel.getShowProgressState().observe(this, Observer { showProgressState ->
            progressBar.visible(showProgressState)
            recyclerView.visible(!showProgressState)
            editText.visible(!showProgressState)
            enterButton.visible(!showProgressState)
            errorMessage.visible(false)
        })

        viewModel.getShowCriticalError().observe(this, Observer { message ->
            errorMessage.text = getString(R.string.critical_error, message)
            progressBar.visible(false)
            recyclerView.visible(false)
            editText.visible(false)
            enterButton.visible(false)
            errorMessage.visible(true)
        })

        viewModel.getHistory().observe(this, Observer { history ->
            listAdapter.setItems(history)
            listAdapter.notifyDataSetChanged()
            smoothScroller.targetPosition = listAdapter.itemCount - 1
            layoutManager.startSmoothScroll(smoothScroller)
        } )

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

        viewModel.init()

        newGameDialogListener = object : NewGameDialogListener{
            override fun onDialogPositiveClick(dialog: DialogFragment) {
                viewModel.restartGame()
            }

            override fun onDialogNegativeClick(dialog: DialogFragment) {
                dialog.dismiss()
            }
        }

        enterButton.setOnClickListener {
            editText.dispatchKeyEvent(KeyEvent(0, 0, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER, 0))
            editText.dispatchKeyEvent(KeyEvent(0, 0, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER, 0))
        }

        if (savedInstanceState != null) {
            // after changing screen orientation the listener is not set
            val newGameDialog = supportFragmentManager.findFragmentByTag("new_game_dialog") as NewGameDialog?
            newGameDialog?.setListener(newGameDialogListener)
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

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        val saves = viewModel.getSaveStates()
        for (save in saves) {
            val text = save.value ?: getString(R.string.action_text_empty_slot)
            when (save.key) {
                1 -> {
                    menu?.findItem(R.id.action_save_game_1)?.title = "1. $text"
                    menu?.findItem(R.id.action_load_game_1)?.run {
                        title = "1. $text"
                        isEnabled = save.value != null
                    }
                }
                2 -> {
                    menu?.findItem(R.id.action_save_game_2)?.title = "2. $text"
                    menu?.findItem(R.id.action_load_game_2)?.run {
                        title = "2. $text"
                        isEnabled = save.value != null
                    }
                }
                3 -> {
                    menu?.findItem(R.id.action_save_game_3)?.title = "3. $text"
                    menu?.findItem(R.id.action_load_game_3)?.run {
                        title = "3. $text"
                        isEnabled = save.value != null
                    }
                }
            }
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.action_new_game -> {
                val newGameDialog = NewGameDialog.newInstance(newGameDialogListener)
                newGameDialog.show(supportFragmentManager, "new_game_dialog")
            }
            R.id.action_save_game_1 -> {
                viewModel.saveState("1.sav")
                invalidateOptionsMenu()
            }
            R.id.action_save_game_2 -> {
                viewModel.saveState("2.sav")
                invalidateOptionsMenu()
            }
            R.id.action_save_game_3 -> {
                viewModel.saveState("3.sav")
                invalidateOptionsMenu()
            }
            R.id.action_load_game_1 -> {
                viewModel.loadState("1.sav")
            }
            R.id.action_load_game_2 -> {
                viewModel.loadState("2.sav")
            }
            R.id.action_load_game_3 -> {
                viewModel.loadState("3.sav")
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

    override fun onPause() {
        super.onPause()
        viewModel.saveState()
    }
}
