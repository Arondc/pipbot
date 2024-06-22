package de.arondc.pipbot.memes

import org.springframework.stereotype.Component

@Component
class MemeBrowserSourceQueue {
    val memesForChannel = mutableMapOf<String, ArrayDeque<String>>()

    fun queue(channelName: String, meme : String){
        memesForChannel.compute(channelName){ _, list ->
            when(list) {
                null -> {
                    val newList = ArrayDeque<String>()
                    newList.add(meme)
                    newList
                }
                else -> {
                    list.addLast(meme)
                    list
                }
            }}
    }

    fun getNextMeme(channelName: String): String? = memesForChannel[channelName]?.removeFirstOrNull()
}