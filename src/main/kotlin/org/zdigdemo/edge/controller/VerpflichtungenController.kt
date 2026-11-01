package org.zdigdemo.edge.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping


object verpflichtungenViews {
	const val INDEX = "pages/verpflichtungen/index"
}

@Controller
@RequestMapping("/verpflichtungen")
class VerpflichtungenController() {

	@GetMapping
	fun index(): String {
		return verpflichtungenViews.INDEX
	}
}
