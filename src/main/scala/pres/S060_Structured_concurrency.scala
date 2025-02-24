package pres

import ox.*

import scala.concurrent.duration.DurationInt

@main def s060_Structured_concurrency_1(): Unit =
  supervised:
    val f1 = fork:
      sleep(2.seconds)
      1

    val f2 = fork:
      sleep(1.second)
      2

    println(f1.join() + f2.join())

@main def s060_Structured_concurrency_2(): Unit =
  supervised:
    val f1 = fork:
      sleep(2.seconds)
      1

    val f2 = fork[Int]:
      sleep(1.second)
      throw new RuntimeException("boom")

    println(f1.join() + f2.join())
