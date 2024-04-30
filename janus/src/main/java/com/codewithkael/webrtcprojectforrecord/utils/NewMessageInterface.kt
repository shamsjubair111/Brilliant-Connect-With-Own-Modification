package com.codewithkael.webrtcprojectforrecord.utils

import com.codewithkael.webrtcprojectforrecord.models.JanusResponse
import com.codewithkael.webrtcprojectforrecord.models.MessageModel

interface NewMessageInterface {
    fun onNewMessage(message: JanusResponse)
}