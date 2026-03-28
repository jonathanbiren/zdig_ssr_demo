package org.soldatenwohnen.adapter.`in`

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/")
class TestController {

    @GetMapping()
    fun hello() = "This is the home page!"

    @GetMapping("workout")
    fun workout() = "Run a hard 5K!"

    @GetMapping("fortune")
    fun fortune() = "Today is your lucky day!"
}