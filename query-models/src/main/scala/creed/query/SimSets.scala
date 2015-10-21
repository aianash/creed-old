package creed
package query


case class SimSets[T]() {
  def apply(tg: T): Set[T] = Set.empty[T]
}