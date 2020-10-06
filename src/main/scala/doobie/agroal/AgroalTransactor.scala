package doobie.agroal

import java.util.Properties

import doobie._
import cats.effect._
import io.agroal.api.AgroalDataSource
import io.agroal.api.configuration.AgroalDataSourceConfiguration
import io.agroal.pool.DataSource

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration

object AgroalTransactor {
  def apply[M[_]: Async: ContextShift](
    agroalDataSource: AgroalDataSource,
    connectEC: ExecutionContext,
    blocker: Blocker
  ): AgroalTransactor[M] = Transactor.fromDataSource[M](agroalDataSource, connectEC, blocker)

  def newAgroalConfig(
    className: String,
    url: String,
    user: String,
    pass: String,
    maxSize: Option[Int] = None,
    initSize: Option[Int] = None,
    maxLifeTime: Option[FiniteDuration] = None
  ): AgroalDataSourceConfiguration = {
    import io.agroal.api.configuration.supplier.{AgroalPropertiesReader => R}
    val props = new Properties()
    props.put(R.IMPLEMENTATION, "AGROAL")
    props.put(R.JDBC_URL, url)
    props.put(R.PRINCIPAL, user)
    props.put(R.CREDENTIAL, pass)
    props.put(R.PROVIDER_CLASS_NAME, className)
    maxSize.map(_.toString).foreach(props.put(R.MAX_SIZE, _))
    initSize.map(_.toString).foreach(props.put(R.INITIAL_SIZE, _))
    maxLifeTime.map(_.toSeconds.toString).foreach(props.put(R.MAX_LIFETIME_S, _))

    (new R).readProperties(props).get()
  }

  def newAgroalTransactor[M[_]: Async: ContextShift](
    driverClassName: String,
    url: String,
    user: String,
    pass: String,
    connectEC: ExecutionContext,
    blocker: Blocker
  ): Resource[M, AgroalTransactor[M]] =
    for {
      _     <- Resource.liftF(Async[M].delay(Class.forName(driverClassName)))
      config = newAgroalConfig(driverClassName, url, user, pass)
      ds    <- Resource.make(Async[M].delay(new DataSource(config)))(a => Async[M].delay(a.close()))
    } yield apply(ds, connectEC, blocker)

  def newAgroalTransactor[M[_]: Async: ContextShift](
    driverClassName: String,
    agroalConfig: AgroalDataSourceConfiguration,
    connectEC: ExecutionContext,
    blocker: Blocker
  ): Resource[M, AgroalTransactor[M]] =
    for {
      _  <- Resource.liftF(Async[M].delay(Class.forName(driverClassName)))
      ds <- Resource.make(Async[M].delay(new DataSource(agroalConfig)))(d => Async[M].delay(d.close()))
    } yield apply(ds, connectEC, blocker)
}
