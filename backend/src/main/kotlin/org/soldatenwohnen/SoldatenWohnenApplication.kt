package org.soldatenwohnen

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.EnableAspectJAutoProxy

// @SpringBootApplication enables @SpringBootConfiguration, @EnableAutoConfiguration, and @ComponentScan, so this one
// annotation is one of the reasons that your application can run with so little configuration.
@SpringBootApplication
@EnableAspectJAutoProxy
class SoldatenWohnenApplication

fun main(args: Array<String>) {
    runApplication<SoldatenWohnenApplication>(*args)
}
