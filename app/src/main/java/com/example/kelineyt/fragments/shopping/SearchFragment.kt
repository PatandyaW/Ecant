package com.example.kelineyt.fragments.shopping

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kelineyt.R
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class SearchFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var welcomeText: TextView
    private lateinit var messageEditText: EditText
    private lateinit var sendButton: ImageButton
    private lateinit var messageList: MutableList<Message>
    private lateinit var messageAdapter: MessageAdapter
    private val client = OkHttpClient()
    private val API_KEY = "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"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_search, container, false)

        messageList = ArrayList()
        recyclerView = rootView.findViewById(R.id.recycler_view)
        welcomeText = rootView.findViewById(R.id.welcome_text)
        messageEditText = rootView.findViewById(R.id.message_edit_text)
        sendButton = rootView.findViewById(R.id.send_bt)
        messageAdapter = MessageAdapter(messageList)
        recyclerView.adapter = messageAdapter
        val layoutManager = LinearLayoutManager(requireContext())
        layoutManager.stackFromEnd = true
        recyclerView.layoutManager = layoutManager

        sendButton.setOnClickListener {
            val question = messageEditText.text.toString().trim { it <= ' ' }
            addToChat(question, Message.SENT_BY_ME)
            messageEditText.setText("")
            callAPI(question)
            welcomeText.visibility = View.GONE
        }

        return rootView
    }

    private fun addToChat(message: String, sentBy: String) {
        requireActivity().runOnUiThread {
            messageList.add(Message(message, sentBy))
            messageAdapter.notifyDataSetChanged()
            recyclerView.smoothScrollToPosition(messageAdapter.itemCount)
        }
    }

    private fun addResponse(response: String?) {
        messageList.removeAt(messageList.size - 1)
        addToChat(response!!, Message.SENT_BY_BOT)
    }

    private fun callAPI(question: String) {
        messageList.add(Message("Typing...",Message.SENT_BY_BOT))
        val jsonBody = JSONObject()
        try {
            jsonBody.put("model", "text-davinci-003")
            jsonBody.put("prompt", question)
            jsonBody.put("max_tokens", 4000)
            jsonBody.put("temperature", 0)
        }catch (e:JSONException){
            e.printStackTrace()
        }
        val body :RequestBody = RequestBody.create(JSON,jsonBody.toString())
        val request:Request = Request.Builder()
            .url("https://api.openai.com/v1/completions")
            .header("Authorization", "Bearer $API_KEY")
            .post(body)
            .build()
        client.newCall(request).enqueue(object :Callback{
            override fun onFailure(call: Call, e: IOException) {
                val errorMessage = "Failed to load response due to ${e.message}"
                addResponse(errorMessage)
                // Log the error for further analysis
                Log.e("API_CALL_ERROR", errorMessage)}

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful){
                    var jsonObject :JSONObject? = null
                    try {
                        jsonObject = JSONObject(response.body!!.string())
                        val jsonArray = jsonObject.getJSONArray("choices")
                        val result = jsonArray.getJSONObject(0).getString("text")
                        addResponse(result.trim{it <= ' '})
                    }catch (e:JSONException){
                        e.printStackTrace()
                    }
                }else{
                    addResponse("Failed to load response due to ${response.body.toString()}")
                }
            }

        })

    }
    companion object{
        val JSON :MediaType = "application/json; charset=utf-8".toMediaType()
    }
}
