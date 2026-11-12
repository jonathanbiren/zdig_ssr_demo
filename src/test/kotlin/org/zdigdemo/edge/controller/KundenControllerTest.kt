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

	@Test
	fun `validateZsNr returns error span with role alert on invalid input`() {
		mockMvc.get("/kunden/some-id/validateZsNr") {
			param("zsNummer", "123")
		}.andExpect {
			status { isOk() }
			content {
				contentTypeCompatibleWith(MediaType.TEXT_HTML)
				string(containsString("id=\"zsNummer-validation\""))
				string(containsString("role=\"alert\""))
				string(containsString("text-error"))
				string(containsString("ZS-Nummer muss größer als 999 sein"))
			}
		}
	}

	@Test
	fun `validateZsNr returns empty validation span on valid input`() {
		mockMvc.get("/kunden/some-id/validateZsNr") {
			param("zsNummer", "1234")
		}.andExpect {
			status { isOk() }
			content {
				contentTypeCompatibleWith(MediaType.TEXT_HTML)
				string(containsString("<span id=\"zsNummer-validation\"></span>"))
			}
		}
	}
}
