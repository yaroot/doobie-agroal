package doobie

import io.agroal.api.AgroalDataSource

package object agroal {
  type AgroalTransactor[M[_]] = Transactor.Aux[M, AgroalDataSource]
}
