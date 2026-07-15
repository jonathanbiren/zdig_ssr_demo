package org.soldatenwohnen

import org.soldatenwohnen.config.ClientSearchProperties
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.context.properties.EnableConfigurationProperties

//This is the entry point into the application. This annotation is a combination of @Configuration, @ComponentScan
// and @EnableAutoConfiguration.
@SpringBootApplication
@ConfigurationPropertiesScan
class SoldatenWohnenApplication

fun main(args: Array<String>) {
    SpringApplication.run(SoldatenWohnenApplication::class.java, *args)
}
