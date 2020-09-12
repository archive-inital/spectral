package org.spectral.asm

import io.mockk.every
import io.mockk.mockk
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.gherkin.Feature

object ClassPoolTest : Spek({
    val cls = mockk<Class>() {
        every { name } answers { "test" }
    }

    Feature("ClassPool") {
        var pool: ClassPool = ClassPool()

        Scenario("adding classes") {
            When("adding a single class") {
                pool.add(cls)
            }

            Then("should have size of 1") {
                assert(pool.size == 1)
            }
        }
    }
})