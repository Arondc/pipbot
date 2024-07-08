package de.arondc.pipbot.memes

import mu.KotlinLogging
import org.springframework.stereotype.Component

@Component
class MemeBrowserSourceQueue {
    private val log = KotlinLogging.logger {}

    val memesForChannel = mutableMapOf<String, ArrayDeque<String>>()
    val lastReturnedMemeForChannel = mutableMapOf<String, String>()

    fun queue(channelName: String, meme : String){
        val memeLink = convertToImageLink(meme)
        if (memeLink != null) {
            addToQueue(channelName, memeLink)
        }
    }

    fun getNextMeme(channelName: String): String? {
        val nextMeme = memesForChannel[channelName]?.removeFirstOrNull()
        if (nextMeme != null) {
            lastReturnedMemeForChannel[channelName] = nextMeme
        }
        return nextMeme
    }

    fun getLastReturnedMeme(channelName: String): String? = lastReturnedMemeForChannel[channelName]

    fun convertToImageLink(meme : String) : String? {
        return when {
            meme.contains("imgflip.com/i/") -> meme.replace("imgflip.com/i","i.imgflip.com") + ".jpg"
            meme.contains("imgflip.com/gif/") -> meme.replace("imgflip.com/gif","i.imgflip.com") + ".gif"
            else -> {
                log.warn { "Could not process the imgflip link '$meme' for meme overlay" }
                null
            }
        }
    }

    private fun addToQueue(channelName: String, meme: String) {
        memesForChannel.compute(channelName) { _, list ->
            when (list) {
                null -> {
                    val newList = ArrayDeque<String>()
                    newList.add(meme)
                    newList
                }
                else -> {
                    list.addLast(meme)
                    list
                }
            }
        }
    }

}