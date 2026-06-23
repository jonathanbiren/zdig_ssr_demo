package org.soldatenwohnen.adapter.`in`

import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxTrigger
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

@Controller
class Controller() {

	@GetMapping("/")
	fun index(): String = "pages/site"

	@GetMapping("/hello")
	fun get(model: Model): String {
		model.addAttribute(
			"container",
			HelloContainer(1, HelloModel("Jonathan"))
		)
		return "katalog/index"
	}

	@HxRequest
	@GetMapping("/hxhello")
	fun hxTrigger(model: Model): String {
		return "katalog/fragments/helloFragment"
	}

	@HxRequest
	@GetMapping("/hxfragmenttest")
	fun hxFragmentTest(model: Model): String {
		model.addAttribute(
			"container",
			HelloContainer(
				1, HelloModel("Jonathan")
			)
		)
		return "fragments/"
	}

}


abstract class Container {
	abstract val version: Int
}

data class HelloContainer(override val version: Int, val model: HelloModel) : Container()
data class HelloModel(val name: String)
