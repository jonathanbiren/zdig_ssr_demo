package org.zdigdemo.edge.controller

import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@SpringBootTest
@AutoConfigureMockMvc
class KundenControllerTest {

	@Autowired
	private lateinit var mockMvc: MockMvc

	@Test
	fun `main page is rendered through the typed template facade`() {
		mockMvc.get("/")
			.andExpect {
				status { isOk() }
				content {
					contentTypeCompatibleWith(MediaType.TEXT_HTML)
					string(containsString("Kundenanzahl"))
					string(containsString("kundentabelleContainer"))
				}
			}
	}

	@Test
	fun `HTMX search returns only the rendered customer list`() {
		mockMvc.get("/kunden") {
			param("sucheingabe", "")
			header("HX-Request", "true")
		}.andExpect {
			status { isOk() }
			content {
				contentTypeCompatibleWith(MediaType.TEXT_HTML)
				string(containsString("<table"))
			}
		}
	}
}
