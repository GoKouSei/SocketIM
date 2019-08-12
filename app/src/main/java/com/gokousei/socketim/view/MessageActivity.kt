package com.gokousei.socketim.view

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import com.gokousei.socketim.R
import com.gokousei.socketim.adapder.MessageAdapter
import kotlinx.android.synthetic.main.activity_message.*
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class MessageActivity : AppCompatActivity() {
    private val TAG: String = "GoKouSeiLog"
    private val addressTokyo: String = "18.182.210.199"
    private val heartbeatVal = 0
    private val addressTest = InetSocketAddress("192.168.50.10", 12636)
    private var socket = Socket()
    private val data = mutableListOf<String>()
    private val position = mutableListOf<Float>()
    private val replyMessage = 0F
    private val sendMessage = 1F
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)
        message_parent.adapter = MessageAdapter(data, position, this)
        message_parent.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        val heartbeat = Executors.newScheduledThreadPool(1)
        heartbeat.scheduleAtFixedRate({
            try {
                if (!socket.isConnected) {
                    socket.connect(addressTest)
                    Log.d(TAG, "连接")
                } else if (socket.isClosed) {
                    socket = Socket()
                    socket.connect(addressTest)
                    Log.d(TAG, "重新连接")
                } else {
                    DataOutputStream(socket.getOutputStream()).writeInt(heartbeatVal)
                    Log.d(TAG, "心跳")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                try {
                    socket = Socket()
                    socket.connect(addressTest)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }, 0, 30, TimeUnit.SECONDS)
        Thread(Runnable {
            while (true) {
                try {
                    if (socket.isClosed) {
                        Log.d(TAG, "重新连接")
                        socket = Socket()
                        socket.connect(addressTest)
                    }
                    val input = DataInputStream(socket.getInputStream())
                    val temp = input.readUTF()
                    runOnUiThread {
                        (message_parent.adapter as MessageAdapter).add(temp, replyMessage)
                        message_parent.smoothScrollToPosition(message_parent.adapter!!.itemCount)
                    }
                    Log.d(TAG, "result: $temp")
                } catch (e: Exception) {
                    e.printStackTrace()
                    try {
                        socket = Socket()
                        socket.connect(addressTest)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }).start()
        send.setOnClickListener {
            Thread(Runnable {
                try {
                    if (socket.isClosed) {
                        Log.d(TAG, "重新连接")
                        socket = Socket()
                        socket.connect(addressTest)
                    }
                    val output = DataOutputStream(socket.getOutputStream())
                    output.writeInt(1)
                    output.writeInt(clientId.text.toString().toInt())
                    output.writeInt(targetId.text.toString().toInt())
                    output.writeUTF(message.text.toString())
                    output.flush()
                    runOnUiThread {
                        (message_parent.adapter as MessageAdapter).add(message.text.toString(), sendMessage)
                        message_parent.smoothScrollToPosition(message_parent.adapter!!.itemCount)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    try {
                        socket = Socket()
                        socket.connect(addressTest)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }).start()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            socket.shutdownOutput()
            socket.shutdownInput()
            socket.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}