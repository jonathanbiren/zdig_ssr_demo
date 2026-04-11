package org.soldatenwohnen

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

//This is the entry point into the application. This annotation is a combination of @Configuration, @ComponentScan
// and @EnableAutoConfiguration.
@SpringBootApplication
class SoldatenWohnenApplication

fun main(args: Array<String>) {
    runApplication<SoldatenWohnenApplication>(*args)
}
