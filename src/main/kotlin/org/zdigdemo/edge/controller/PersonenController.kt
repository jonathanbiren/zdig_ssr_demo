package org.zdigdemo.edge.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping


object personenViews {
	const val INDEX = "pages/personen/index"
}

@Controller
@RequestMapping("/personen")
class PersonenController() {

	@GetMapping
	fun index(): String {
		return personenViews.INDEX
	}
}