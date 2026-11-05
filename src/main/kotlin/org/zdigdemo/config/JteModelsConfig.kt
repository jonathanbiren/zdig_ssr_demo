package org.zdigdemo.config

import gg.jte.TemplateEngine
import gg.jte.generated.precompiled.DynamicTemplates
import gg.jte.generated.precompiled.Templates
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class JteModelsConfig {

	@Bean
	fun templates(templateEngine: TemplateEngine): Templates = DynamicTemplates(templateEngine)
}
