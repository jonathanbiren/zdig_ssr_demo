@file:Suppress("ktlint")
package gg.jte.generated.ondemand
import gg.jte.Content
@Suppress("UNCHECKED_CAST", "UNUSED_PARAMETER")
internal class JtelayoutGenerated {
companion object {
	@JvmField val JTE_NAME = "layout.kte"
	@JvmField val JTE_LINE_INFO = intArrayOf(0,0,0,1,1,1,1,1,5,5,5,14,14,14,26,26,26,1,2,2,2,2,2)
	@JvmStatic fun render(jteOutput:gg.jte.html.HtmlTemplateOutput, jteHtmlInterceptor:gg.jte.html.HtmlInterceptor?, title:String, content:Content) {
		jteOutput.writeContent("<!DOCTYPE html>\n<html lang=\"de\">\n")
		gg.jte.generated.ondemand.JteheadGenerated.render(jteOutput, jteHtmlInterceptor, title);
		jteOutput.writeContent("\n<div class=\"h-screen w-full flex flex-col overflow-hidden bg-secondary\">\n    <header class=\"shrink-0\">\n        <nav class=\"navbar bg-base-100 shadow-sm\">\n            <a class=\"btn btn-ghost text-xl\">SFA</a>\n        </nav>\n    </header>\n\n    <main class=\"flex-1 min-h-0 overflow-hidden\">\n        ")
		jteOutput.setContext("main", null)
		jteOutput.writeUserContent(content)
		jteOutput.writeContent("\n    </main>\n\n    <footer class=\"shrink-0\">\n        <div class=\"footer sm:footer-horizontal bg-neutral text-neutral-content p-10\">\n            <nav>\n                ZDig BW\n            </nav>\n        </div>\n    </footer>\n</div>\n</html>\n")
	}
	@JvmStatic fun renderMap(jteOutput:gg.jte.html.HtmlTemplateOutput, jteHtmlInterceptor:gg.jte.html.HtmlInterceptor?, params:Map<String, Any?>) {
		val title = params["title"] as String
		val content = params["content"] as Content
		render(jteOutput, jteHtmlInterceptor, title, content);
	}
}
}
