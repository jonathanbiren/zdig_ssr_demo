package org.zdigdemo.edge.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping


@Controller
@RequestMapping("/verpflichtungen")
class VerpflichtungenController() {

	@GetMapping
	fun index(): String {
		return "pages/verpflichtungen/index.kte"
	}
}
