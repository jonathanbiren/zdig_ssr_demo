@file:Suppress("ktlint")
package gg.jte.generated.ondemand
@Suppress("UNCHECKED_CAST", "UNUSED_PARAMETER")
internal class JteheadGenerated {
companion object {
	@JvmField val JTE_NAME = "head.kte"
	@JvmField val JTE_LINE_INFO = intArrayOf(0,0,0,0,0,0,0,4,4,4,4,9,9,9,0,0,0,0,0)
	@JvmStatic fun render(jteOutput:gg.jte.html.HtmlTemplateOutput, jteHtmlInterceptor:gg.jte.html.HtmlInterceptor?, title:String) {
		jteOutput.writeContent("<head>\n    <meta charset=\"UTF-8\">\n    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n    <title>")
		jteOutput.setContext("title", null)
		jteOutput.writeUserContent(title)
		jteOutput.writeContent("</title>\n    <link rel=\"stylesheet\" href=\"/css/app.css\">\n    <script type=\"text/javascript\" src=\"/webjars/htmx.org/4.0.0-beta4/dist/htmx.min.js\" defer></script>\n    <script type=\"text/javascript\" src=\"/webjars/alpinejs/3.15.12/dist/cdn.min.js\" defer></script>\n</head>\n")
	}
	@JvmStatic fun renderMap(jteOutput:gg.jte.html.HtmlTemplateOutput, jteHtmlInterceptor:gg.jte.html.HtmlInterceptor?, params:Map<String, Any?>) {
		val title = params["title"] as String
		render(jteOutput, jteHtmlInterceptor, title);
	}
}
}
