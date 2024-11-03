package com.example.signal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment

class ChatDetailFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_chat_detail, container, false)

        // Retrieve chat name from arguments and display it
        val chatName = arguments?.getString("chat_name") ?: "Chat"
        val chatTitle = view.findViewById<TextView>(R.id.chatTitle)
        chatTitle.text = chatName

        // Get the message container
        val messageContainer = view.findViewById<LinearLayout>(R.id.messageContainer)

        // Add hardcoded messages
        addHardcodedMessages(messageContainer)

        return view


    }

    private fun addHardcodedMessages(container: LinearLayout) {
        val messages = listOf(
            Pair("in", "I'm on my way! What's the address?"),
            Pair("out", "We're at 118 68th Ave."),
            Pair("in", "Is there a buzzer? Don’t want to ruin the surprise"),
            Pair("out", "Buzz 2F if you get here before 7pm, otherwise text me."),
            Pair("in", "Ok, stay there, I’ll come down in a moment."),
            Pair("out", "Thanks!")
        )

        for ((type, message) in messages) {
            val messageView = TextView(requireContext()).apply {
                text = message
                textSize = 16f
                setPadding(8, 8, 8, 8)
                if (type == "in") {
                    setTextColor(resources.getColor(android.R.color.holo_blue_light, null))
                    setBackgroundResource(R.drawable.incoming_message_background)
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(16, 8, 48, 8) // Margin for incoming messages
                    }
                } else {
                    setTextColor(resources.getColor(android.R.color.white, null))
                    setBackgroundResource(R.drawable.outgoing_message_background)
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(48, 8, 16, 8) // Margin for outgoing messages
                    }
                }
            }
            container.addView(messageView)
        }


    }
    override fun onResume() {
        super.onResume()
        (activity as? MainActivity)?.showNavBar(false)
    }

}
