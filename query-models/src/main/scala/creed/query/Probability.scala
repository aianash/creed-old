package creed
package query


import scalaz._, Scalaz._

trait Probability[R] {
  def +=(p: (R, Float))
}

case class Probability2[X1, X2](unknownsProb: Float, numX1: Int, numX2: Int) extends Probability[(X1, X2)] {
  def +=(p: ((X1, X2), Float)) {}
}

case class Probability3[X1, X2, X3](unknownsProb: Float, numX1: Int, numX2: Int, numX3: Int,
  Px1x2: Option[Probability[(X1, X2)]] = none,
  Px1x3: Option[Probability[(X1, X3)]] = none,
  Px2x3: Option[Probability[(X2, X3)]] = none
) extends Probability[(X1, X2, X3)] {
  def +=(p: ((X1, X2, X3), Float)) {}
}