package de.richargh.sandbox.akkajson

import akka.actor.AbstractActor
import akka.actor.Props
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.random.Random

class PrinterActor private constructor(private val counter: Counter): AbstractActor() {
    private val id = UUID.randomUUID()

    override fun createReceive() = receiveBuilder()
            .match(PrintMessage::class.java, this::print)
            .build()

    private fun print(printMessage: PrintMessage) {
        heavyCompute()
        counter.message(Thread.currentThread().name,
                        "${Thread.currentThread().name} ${System.currentTimeMillis()} printing: $printMessage")
    }

    private fun heavyCompute() {
        (1L..10000L).fold(10000000000L) { acc, i ->
            val result = (acc / i) * Random(System.currentTimeMillis()).nextInt() * Math.sin(i.toDouble())
            result.toLong()
        }
    }

    companion object {
        fun props(counter: Counter): Props {
            return Props.create(PrinterActor::class.java) {
                PrinterActor(counter)
            }
        }
    }
}

class Counter {

    private val list = ConcurrentLinkedQueue<String>()
    private val actorIds = ConcurrentHashMap<String, Unit>()

    fun message(threadId: String, msg: String) {
        actorIds[threadId] = Unit
        list.add(msg)
    }

    fun messageCount() = list.size

    fun actorCount() = actorIds.size
}