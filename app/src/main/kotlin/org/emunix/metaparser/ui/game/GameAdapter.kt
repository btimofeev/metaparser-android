/*
 * Copyright (c) 2019-2020 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.metaparser.ui.game

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.emunix.metaparser.Paragraph
import org.emunix.metaparser.R

class GameAdapter : RecyclerView.Adapter<GameAdapter.ViewHolder>() {

    private var dataset: List<Paragraph> = emptyList()

    fun setItems(items: List<Paragraph>){
        dataset = items
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val command = itemView.findViewById<TextView>(R.id.command)!!
        val text = itemView.findViewById<TextView>(R.id.text)!!
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.activity_game_list_item, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.command.text = dataset[position].command
        holder.text.text = dataset[position].response
    }

    override fun getItemCount() = dataset.size
}
