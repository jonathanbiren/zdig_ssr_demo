package org.soldatenwohnen.adapter.inbound

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

object ViewsSecurity {
	const val TEST = "pages/security/test"
}

@Controller
class SecurityController {
	@GetMapping("/security")
	fun security(): String {
		return ViewsSecurity.TEST
	}
}


