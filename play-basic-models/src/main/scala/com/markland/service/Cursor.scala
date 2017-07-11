
package com.markland.service

case class Cursor[T](value: String) extends AnyVal {
  override def toString = value
  def as[T2]: Cursor[T2] = Cursor[T2](value)
}

object Cursor {
  import play.api.libs.json._
  import play.api.libs.functional.syntax._

  implicit def cursorFormat[T](implicit vf: Format[String]) = Format[Cursor[T]](Reads(_.validate[String].map(Cursor[T])), Writes(v => Json.toJson(v.value)))

  implicit class CursorOps(val self: String) extends AnyVal {
    def cursor[T] = Cursor[T](self)
  }
}

object CursorBindables {
  import play.api.mvc.{QueryStringBindable, PathBindable}

  implicit def cursorPathBindable[T](implicit binder: PathBindable[String]) = new PathBindable[Cursor[T]] {
      override def bind(key: String, value: String) = binder.bind(key, value).right.map(Cursor[T](_))
      override def unbind(key: String, value: Cursor[T]) = binder.unbind(key, value.value)
    }

  implicit def cursorQueryStringBindable[T](implicit binder: QueryStringBindable[String]) = new QueryStringBindable[Cursor[T]] {
    override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, Cursor[T]]] = {
      binder.bind(key, params).map(_.right.map(Cursor[T]))
    }
    override def unbind(key: String, value: Cursor[T]): String = binder.unbind(key, value.value)
  }
}
       
