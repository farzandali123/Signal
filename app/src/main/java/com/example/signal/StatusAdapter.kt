package com.example.signal

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class UserStatusAdapter(
    private val userStatusList: List<UserStatus>,
    private val onStatusClick: (UserStatus) -> Unit
) : RecyclerView.Adapter<UserStatusAdapter.UserStatusViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserStatusViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_status, parent, false)
        return UserStatusViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserStatusViewHolder, position: Int) {
        val userStatus = userStatusList[position]
        holder.userNameTextView.text = userStatus.userName

        // Decode the first status image
        val firstStatus = userStatus.statuses.firstOrNull()
        if (firstStatus != null) {
            val imageBytes = Base64.decode(firstStatus.imageUrl, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            holder.statusImageView.setImageBitmap(bitmap)
        }

        holder.itemView.setOnClickListener {
            onStatusClick(userStatus)
        }
    }

    override fun getItemCount(): Int = userStatusList.size

    inner class UserStatusViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val statusImageView: ImageView = itemView.findViewById(R.id.statusImageView)
        val userNameTextView: TextView = itemView.findViewById(R.id.userNameTextView)
    }
}
