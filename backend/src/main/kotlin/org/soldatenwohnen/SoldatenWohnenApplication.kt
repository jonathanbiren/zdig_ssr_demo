package org.soldatenwohnen

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

//This is the entry point into the application. This annotation is a combination of @Configuration, @ComponentScan
// and @EnableAutoConfiguration.
@SpringBootApplication
class SoldatenWohnenApplication

fun main(args: Array<String>) {
    SpringApplication.run(SoldatenWohnenApplication::class.java, *args)
}
