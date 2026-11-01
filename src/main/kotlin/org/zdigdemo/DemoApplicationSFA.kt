package org.zdigdemo

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan

//This is the entry point into the application. This annotation is a combination of @Configuration, @ComponentScan
// and @EnableAutoConfiguration. Spring will only scan for components, configurations, and services in the package which
// contains the class annotated with @SpringBootApplication, as well as inside the subpackages of that package.
@SpringBootApplication
@ConfigurationPropertiesScan
class DemoApplicationSFA

fun main(args: Array<String>) {
	SpringApplication.run(DemoApplicationSFA::class.java, *args)
}
