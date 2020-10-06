package doobie.agroal

import cats.implicits._
import cats.effect._
import doobie.implicits._

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import java.util.concurrent.Executors

class AgroalTransactorSuite extends munit.CatsEffectSuite {
  test("no leaking connections") {
    val agroalConfig  = AgroalTransactor.newAgroalConfig(
      className = "org.postgresql.Driver",
      url = "jdbc:postgresql://127.0.0.1:5432/postgres",
      user = "postgres",
      pass = "",
      maxSize = 5.some
    )
    def newTransactor = AgroalTransactor.newAgroalTransactor[IO](
      "org.postgresql.Driver",
      agroalConfig,
      ExecutionContext.fromExecutor(Executors.newFixedThreadPool(32)),
      Blocker.liftExecutorService(Executors.newCachedThreadPool())
    )

    val polling: fs2.Stream[IO, Unit] = for {
      xa  <- fs2.Stream.resource(newTransactor)
      poll = fr"select 1".query[Int].stream.transact(xa) ++ fs2.Stream.eval_(IO.sleep(50.millis))
      _   <- fs2.Stream
               .emits(List.fill(100)(poll.repeat))
               .parJoinUnbounded
               .take(1000)
    } yield ()

    polling.compile.drain.map(assertEquals(_, ()))
  }
}
