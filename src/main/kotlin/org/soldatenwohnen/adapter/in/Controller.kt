package org.soldatenwohnen.adapter.`in`

import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest
import org.springframework.stereotype.Component
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

@Controller
class Controller() {

	@GetMapping("/")
	fun index(): String {
		return "sfa/index"
	}


}

