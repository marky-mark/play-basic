
package api.common

case class Id[V, T](value: V) extends AnyVal {
  override def toString = value.toString
  def convert[O](f: V => O): O = f(value)
  def as[T2]: Id[V, T2] = convert(Id[V, T2])
}

object Id {
  import play.api.libs.json._
  import play.api.libs.functional.syntax._

  implicit def idFormat[V, T](implicit vf: Format[V]) = Format[Id[V, T]](Reads(_.validate[V].map(Id[V, T])), Writes(v => Json.toJson(v.value)))

  implicit class IdOps[V](val self: V) extends AnyVal {
    def id[T] = Id[V, T](self)
  }

  trait Conversions {
    /**
     * Allow applications to define compatabilities between equivalent items from different Swagger APIs.
     * This is really useful, for example, with IDs.
     */
    trait Compatible[T1, T2]

    type Bicompat2[T1, T2] = Compatible[T1, T2] with Compatible[T2, T1]
    type Bicompat3[T1, T2, T3] = Bicompat2[T1, T2] with Bicompat2[T1, T3] with Bicompat2[T2, T3]
    type Bicompat4[T1, T2, T3, T4] = Bicompat3[T1, T2, T3] with Bicompat3[T1, T2, T4] with Bicompat3[T2, T3, T4]
    type Bicompat5[T1, T2, T3, T4, T5] = Bicompat4[T1, T2, T3, T4] with Bicompat4[T1, T2, T4, T5] with Bicompat4[T1, T2, T3, T5] with Bicompat4[T2, T3, T4, T5]

    import scala.language.implicitConversions
    implicit def id2id[V, T1, T2](id: Id[V, T1])(implicit ev: Compatible[T1, T2]): Id[V, T2] = id.as[T2]
    implicit def optId2OptId[V, T1, T2](id: Option[Id[V, T1]])(implicit ev: Compatible[T1, T2]): Option[Id[V, T2]] = id.map(_.as[T2])
    implicit def seqId2SeqId[V, T1, T2](ids: Seq[Id[V, T1]])(implicit ev: Compatible[T1, T2]): Seq[Id[V, T2]] = ids.map(_.as[T2])
  }
}

object IdBindables {
  import play.api.mvc.{QueryStringBindable, PathBindable}

  implicit def idPathBindable[V, T](implicit binder: PathBindable[V]) = new PathBindable[Id[V, T]] {
      override def bind(key: String, value: String) = binder.bind(key, value).right.map(Id[V, T](_))
      override def unbind(key: String, value: Id[V, T]) = binder.unbind(key, value.value)
    }

  implicit def idQueryStringBindable[V, T](implicit binder: QueryStringBindable[V]) = new QueryStringBindable[Id[V, T]] {
    override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, Id[V, T]]] = {
      binder.bind(key, params).map(_.right.map(Id[V, T]))
    }
    override def unbind(key: String, value: Id[V, T]): String = binder.unbind(key, value.value)
  }
}
       
