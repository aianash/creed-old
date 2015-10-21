package creed

package object core {
  @inline def !![T, U](a: => T)(thunk: T => U)(nullthunk: => U): U =
    a match { case null => nullthunk; case a => thunk(a) }
}