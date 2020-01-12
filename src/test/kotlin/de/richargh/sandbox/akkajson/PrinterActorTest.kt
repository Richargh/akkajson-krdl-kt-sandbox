package de.richargh.sandbox.akkajson

import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.testkit.javadsl.TestKit
import com.typesafe.config.ConfigFactory
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.time.Duration

class PrinterActorTest {

    private lateinit var system: ActorSystem

    @BeforeAll
    fun setup() {
        val config = ConfigFactory.load()
        system = ActorSystem.create("test", config)
    }

    @AfterAll
    fun teardown() {
        TestKit.shutdownActorSystem(system)
    }

    @Test
    fun `Akka does not drop messages in-process, even if actors are busy`() {
        val counter = Counter()
        val maxCount = 10_000
        object: TestKit(system) {
            init {
                // arrange
                val actorRefs = (1..maxCount).map { system.actorOf(
                        PrinterActor.props(counter), "printerActor${it}") }

                // act
                actorRefs.forEach{ it.tell(PrintMessage(), ActorRef.noSender())}

                // assert
                awaitAssert(Duration.ofMinutes(1)) {
                    println("Asserting ${counter.messageCount()} with ${counter.actorCount()} actors")
                    Assertions.assertThat(counter.messageCount()).isEqualTo(maxCount)
                }
            }
        }
    }
}