package org.soldatenwohnen.adapter.`in`

import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest
import org.springframework.stereotype.Component
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

@Controller
class Controller(val counter: Counter) {

	@GetMapping("/")
	fun index(model: Model): String {
		model.addAttribute("heroSectionTitle", "This is the hero section")
		model.addAttribute("featureSectionTitle", "This is the feature section")
		return "katalog/index"
	}

	@HxRequest
	@GetMapping("/update-oob")
	open fun updatePage(model: Model): String {
		counter.increment()
		model.addAttribute("heroSectionTitle", "This is the OOB hero update ${counter.count}")
		model.addAttribute("featureSectionTitle", "This is the OOB feature update ${counter.count}")
		return "katalog/hx-fragments/page-update"
	}

}

@Component
class Counter(var count: Int = 0) {
	fun increment() {
		count++
	}
}

