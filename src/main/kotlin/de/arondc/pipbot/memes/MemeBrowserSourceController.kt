package de.arondc.pipbot.memes

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.*

@Controller
@RequestMapping("/browser/memes")
class MemeBrowserSourceController(val memeBrowserSourceQueue: MemeBrowserSourceQueue) {
	@GetMapping("/{channelName}")
	fun obsBrowserSource(
		@PathVariable channelName: String,
		@RequestParam imgWidth: String = "600px",
		@RequestParam address: String = "localhost:8080",
		model: Model
	): String {
		model.set("channelName", channelName)
		model.set("imgWidth", imgWidth)
		model.set("address", address)
		return "memeobssource"
	}

	@GetMapping("/{channelName}/monitor")
	fun monitorBrowserSource(@PathVariable channelName: String, model: Model): String {
		model.set("channelName", channelName)
		return "mememonitorsource"
	}

	@GetMapping("/{channelName}/next_meme")
	@ResponseBody
	fun nextMeme(@PathVariable channelName: String): String {
		val nextMeme = memeBrowserSourceQueue.getNextMeme(channelName)
		return nextMeme ?: ""
	}

	@GetMapping("/{channelName}/last_returned_meme")
	@ResponseBody
	fun lastReturnedMeme(@PathVariable channelName: String): String {
		val lastReturnedMeme = memeBrowserSourceQueue.getLastReturnedMeme(channelName)
		return lastReturnedMeme ?: ""
	}
}