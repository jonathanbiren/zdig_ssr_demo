@file:Suppress("ktlint")
package gg.jte.generated.ondemand
import gg.jte.Content
@Suppress("UNCHECKED_CAST", "UNUSED_PARAMETER")
internal class JtetestGenerated {
companion object {
	@JvmField val JTE_NAME = "test.kte"
	@JvmField val JTE_LINE_INFO = intArrayOf(0,0,0,1,1,1,1,1,2,2,2,6,6,6,15,15,15,16,16,16,1,1,1,1,1)
	@JvmStatic fun render(jteOutput:gg.jte.html.HtmlTemplateOutput, jteHtmlInterceptor:gg.jte.html.HtmlInterceptor?, name:String) {
		gg.jte.generated.ondemand.JtelayoutGenerated.render(jteOutput, jteHtmlInterceptor, "JTE Test", object : gg.jte.html.HtmlContent {
			override fun writeTo(jteOutput:gg.jte.html.HtmlTemplateOutput) {
				jteOutput.writeContent("\n    <section class=\"h-full flex flex-col gap-4 p-4 mx-auto w-full max-w-7xl mt-2\">\n        <div class=\"bg-base-200 rounded-box p-4 shadow-sm\">\n            <h1 class=\"text-2xl font-bold text-primary\">JTE Test Page</h1>\n            <p class=\"text-base-content mt-2\">Hello, <span class=\"text-secondary font-semibold\">")
				jteOutput.setContext("span", null)
				jteOutput.writeUserContent(name)
				jteOutput.writeContent("</span>!</p>\n            <p class=\"text-sm text-base-content/70 mt-1\">If you can see this page with proper styling, JTE + Tailwind + DaisyUI is working correctly.</p>\n        </div>\n        <div class=\"flex gap-2\">\n            <button class=\"btn btn-primary\">Primary Button</button>\n            <button class=\"btn btn-secondary\">Secondary Button</button>\n            <span class=\"badge badge-accent\">Badge</span>\n        </div>\n    </section>\n")
			}
		});
		jteOutput.writeContent("\n")
	}
	@JvmStatic fun renderMap(jteOutput:gg.jte.html.HtmlTemplateOutput, jteHtmlInterceptor:gg.jte.html.HtmlInterceptor?, params:Map<String, Any?>) {
		val name = params["name"] as String
		render(jteOutput, jteHtmlInterceptor, name);
	}
}
}
