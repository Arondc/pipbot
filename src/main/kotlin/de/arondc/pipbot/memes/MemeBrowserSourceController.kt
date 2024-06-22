package de.arondc.pipbot.memes

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody

@Controller
@RequestMapping("/browser/memes")
class MemeBrowserSourceController(val memeBrowserSourceQueue: MemeBrowserSourceQueue) {
	@GetMapping("/{channelName}")
	fun source(@PathVariable channelName: String, model: Model): String {
		model.set("channelName", channelName)
		return "memesource"
	}

	@GetMapping("/{channelName}/message")
	@ResponseBody
	fun message(@PathVariable channelName: String): String {
		val nextMeme = memeBrowserSourceQueue.getNextMeme(channelName)
		return nextMeme ?: ""
	}
}