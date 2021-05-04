package com.example.destined

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.recycler_view_author.view.*

class AuthorsAdapter : RecyclerView.Adapter<AuthorsAdapter.LocationsViewModel>() {
    private var authors = mutableListOf<Locashan>()
    var listener: RecyclerViewClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = LocationsViewModel(
            LayoutInflater.from(parent.context)
                    .inflate(R.layout.recycler_view_author, parent, false)
    )
    override fun getItemCount() = authors.size
    override fun onBindViewHolder(holder: LocationsViewModel, position: Int) {
        holder.view.text_view_name.text = authors[position].name
        holder.view.text_view_city_lat.text=authors[position].latit
        holder.view.text_view_city_lon.text=authors[position].longit
        holder.view.button_edit.setOnClickListener{
            listener?.onRecyclerViewItemClicked(it, authors[position])
        }
        holder.view.button_delete.setOnClickListener {
            listener?.onRecyclerViewItemClicked(it, authors[position])
        }
    }
    fun getAuthors(): MutableList<Locashan> {
        return authors
    }
    fun setAuthors(authors: List<Locashan>) {
        this.authors = authors as MutableList<Locashan>
        notifyDataSetChanged()
    }
    fun addAuthor(locashan:Locashan){
        if(!authors.contains(locashan)){
            authors.add(locashan)
            notifyDataSetChanged()
        }
        else{
            val index = authors.indexOf(locashan)
            if (locashan.isDeleted) {
                authors.removeAt(index)
            } else {
                authors[index] = locashan
            }
        }
        notifyDataSetChanged()
    }
    class LocationsViewModel(val view: View) : RecyclerView.ViewHolder(view)
}