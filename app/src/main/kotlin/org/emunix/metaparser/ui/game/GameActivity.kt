package org.emunix.metaparser.ui.game

import android.os.Bundle
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*
import org.emunix.metaparser.helper.visible
import org.emunix.metaparser.R


class GameActivity : AppCompatActivity() {

    private lateinit var listAdapter: GameAdapter
    private lateinit var viewModel: GameViewModel

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

        viewModel = ViewModelProviders.of(this).get(GameViewModel::class.java)

        viewModel.getShowProgressState().observe(this, Observer { showProgressState ->
            progressBar.visible(showProgressState)
            recyclerView.visible(!showProgressState)
            editText.visible(!showProgressState)
        })

        viewModel.getHistory().observe(this, Observer { history ->
            listAdapter.setItems(history)
            listAdapter.notifyDataSetChanged()
            recyclerView.smoothScrollToPosition(listAdapter.itemCount - 1)
        } )

        editText.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                val text = editText.text.toString()
                viewModel.sendTextToGame(text)
                editText.text.clear()
                return@OnKeyListener true
            }
            false
        })

        viewModel.init()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.saveGame()
    }
}
