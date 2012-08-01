object apps {
  import scala.collection.immutable._

  trait Functor[T[_]]{
    def map[A,B](f:A=>B)(ta:T[A]):T[B]
  }

  trait Applicative[T[_]] extends Functor[T]{
    def pure[A](a:A):T[A]
    def ap[A,B](tf:T[A=>B])(ta:T[A]):T[B]
  }

  case class ApplicableSet[A, B](fs: Set[A=>B]) {
    def <*>(as: Set[A]) = (for (f <- fs; a <- as) yield f(a)).toSet
  }

  implicit def applicablize[A, B](fs: Set[A=>B]) = new {
    def <*>(as: Set[A]) = (for (f <- fs; a <- as) yield f(a)).toSet
  }
    //ApplicableSet(fs)

  implicit object AppSet extends Applicative[Set] {
    def pure[A](a: A) = Set(a)
    def ap[A,B](fs:Set[A=>B])(as:Set[A]):Set[B] = (for (f <- fs; a <- as) yield f(a)).toSet
    def map[A,B](f:A=>B)(as:Set[A]) = ap(pure(f))(as)
  }

  val p = AppSet.pure((s:String) => "'" + s + "'")
  val p2 = AppSet.pure((first: String) => (second:String) => first + " " + second)
  val names = Set("Joe", "Jill")
  val lasts = Set("Smith", "Frazer")
  p2 <*> names <*> lasts
//  class AppSet[A] extends HashSet[A] with Applicative[AppSet] {
//    def ap[A, B](fs: AppSet[(A) => B])(as: AppSet[A]): AppSet[B] = {
//      val result = new AppSet[B]
//      for (f <- fs; a <- as) {result add f(a)}
//      result
//    }
//    def pure[A](a: A): AppSet[A] = { val result: AppSet[A] = new AppSet[A]; result add a; result }
//    def map[A, B](fs: (A) => B)(as: AppSet[A]): AppSet[B] = ap(pure(fs))(as)
//  }
}
