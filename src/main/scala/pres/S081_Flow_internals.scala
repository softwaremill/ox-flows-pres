package pres

import ox.*
import ox.channels.{forkPropagate, selectOrClosed, Channel, ChannelClosed, Sink, Source}
import pres.S081_Flow_internals.FlowOps.channelToEmit

object S081_Flow_internals:

  class Flow[+T](val last: FlowStage[T]) extends FlowOps[T]

  trait FlowStage[+T]:
    def run(emit: FlowEmit[T]): Unit

  trait FlowEmit[-T]:
    def apply(t: T): Unit

  /*

  Signal completion: when `run` completes

  Signal error: throw an exception

   */

  trait FlowOps[+T]:
    outer: Flow[T] =>

    def map[U](f: T => U): Flow[U] =
      Flow((emit: FlowEmit[U]) => last.run((t: T) => emit(f(t))))

    def filter(f: T => Boolean): Flow[T] =
      Flow((emit: FlowEmit[T]) => last.run((t: T) => if f(t) then emit(t)))

    def merge[U >: T](other: Flow[U]): Flow[U] =
      Flow((emit: FlowEmit[U]) =>
        unsupervised:
          val c1 = outer.runToChannel()
          val c2 = other.runToChannel()

          repeatWhile:
            selectOrClosed(c1, c2) match
              case ChannelClosed.Done =>
                if c1.isClosedForReceive then channelToEmit(c2, emit)
                else channelToEmit(c1, emit)
                false
              case e: ChannelClosed.Error => throw e.toThrowable
              case r: U @unchecked        => emit(r); true
      )

    def runToChannel()(using OxUnsupervised): Source[T] =
      val ch = Channel.bufferedDefault[T]
      forkPropagate(ch) {
        last.run((t: T) => ch.send(t))
        ch.done()
      }.discard
      ch

  object FlowOps:
    private def channelToEmit[T](source: Source[T], emit: FlowEmit[T]): Unit =
      repeatWhile:
        val t = source.receiveOrClosed()
        t match
          case ChannelClosed.Done     => false
          case e: ChannelClosed.Error => throw e.toThrowable
          case t: T @unchecked        => emit(t); true

end S081_Flow_internals
