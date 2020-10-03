package doobie.agroal

import doobie._
import cats.effect._
import io.agroal.api.AgroalDataSource
import io.agroal.api.configuration.AgroalDataSourceConfiguration
import io.agroal.api.configuration.AgroalDataSourceConfiguration.DataSourceImplementation
import io.agroal.api.configuration.supplier.AgroalDataSourceConfigurationSupplier
import io.agroal.api.configuration.supplier.AgroalConnectionPoolConfigurationSupplier
import io.agroal.api.security.{NamePrincipal, SimplePassword}
import io.agroal.pool.DataSource

import scala.concurrent.ExecutionContext

object AgroalTransactor {
  def apply[M[_]: Async: ContextShift](
    agroalDataSource: AgroalDataSource,
    connectEC: ExecutionContext,
    blocker: Blocker
  ): AgroalTransactor[M] = Transactor.fromDataSource[M](agroalDataSource, connectEC, blocker)

  def newAgroalConfig(
    url: String,
    user: String,
    pass: String
  ): AgroalDataSourceConfiguration = {
    val pool = new AgroalConnectionPoolConfigurationSupplier
    val cf   = pool.connectionFactoryConfiguration()
    cf.principal(new NamePrincipal(user))
    cf.credential(new SimplePassword(pass))
    cf.jdbcUrl(url)

    val config = new AgroalDataSourceConfigurationSupplier
    config.dataSourceImplementation(DataSourceImplementation.AGROAL)
    config.connectionPoolConfiguration(pool)

    config.get()
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
      config = newAgroalConfig(url, user, pass)
      ds    <- Resource.make(Async[M].delay(new DataSource(config)))(a => Async[M].delay(a.close()))
    } yield apply(ds, connectEC, blocker)
}
