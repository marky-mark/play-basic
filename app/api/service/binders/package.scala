package api.service

import api.service.models._
import play.api.mvc.QueryStringBindable

import scala.util.Either

package object binders {

  
  

  

  private def enumQueryStringBinder[E <: NamedEnum](fn: String => Either[EnumError, E])(implicit stringBinder: QueryStringBindable[String]): QueryStringBindable[E] = new QueryStringBindable[E] {
    override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, E]] = {
      stringBinder.bind(key, params).map {
        _.right.flatMap(fn(_).left.map(_.message))
      }
    }

    override def unbind(key: String, value: E): String = {
      stringBinder.unbind(key, value.name)
    }
  }

}
     
