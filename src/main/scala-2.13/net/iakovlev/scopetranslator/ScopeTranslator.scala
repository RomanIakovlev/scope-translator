package net.iakovlev.scopetranslator

import scala.annotation.implicitNotFound
import scala.collection.Factory
import scala.language.higherKinds

@implicitNotFound("Can't translate ${From} to ${To}")
abstract class ScopeTranslator[From, To] extends Serializable {
  def translate(a: From): To
}

import scala.language.experimental.macros
import scala.reflect.macros._

trait ScopeTranslatorLowPrio {
  implicit def generic[A, B]: ScopeTranslator[A, B] =
    macro MacroImpl.translatorImpl[A, B]
}

object MacroImpl {

  def translatorImpl[A: c.WeakTypeTag, B: c.WeakTypeTag](
      c: blackbox.Context): c.Tree = {
    import c.universe._

    val tpeA = weakTypeOf[A]
    val tpeB = weakTypeOf[B]

    def members(fields: MemberScope) = {
      fields.collect {
        case field if field.isMethod && field.asMethod.isCaseAccessor =>
          (field.asTerm, field.typeSignature)
      }
    }

    def translator(from: Type, to: Type) = {
      tq"_root_.net.iakovlev.scopetranslator.ScopeTranslator[$from, $to]"
    }

    val fieldsA = members(tpeA.decls)
    val fieldsB = members(tpeB.decls)

    val groups = fieldsA.zip(fieldsB).map {
      case ((ln, lt), (_, rt)) if lt weak_<:< rt =>
        q"a.$ln"
      case ((ln, lt), (_, rt)) =>
        q"""_root_.scala.Predef.implicitly[${translator(lt, rt)}].translate(a.$ln)"""
    }

    val r =
      q"""
         new ${translator(tpeA, tpeB)} {
           def translate(a: $tpeA): $tpeB = {
             new $tpeB(..$groups)
           }
         }
       """
    //    println(c.enclosingPosition + showCode(r))
    r
  }
}

object ScopeTranslator extends ScopeTranslatorLowPrio {

  implicit def translateTrivial[A, B](
      implicit ev: A <:< B): ScopeTranslator[A, B] =
    new ScopeTranslator[A, B] {
      override def translate(a: A): B = ev(a)
    }

  implicit def translateOptional[A, B](implicit tr: ScopeTranslator[A, B])
    : ScopeTranslator[Option[A], Option[B]] =
    new ScopeTranslator[Option[A], Option[B]] {
      override def translate(a: Option[A]): Option[B] = {
        a.map(tr.translate)
      }
    }

  implicit def translateTraversableOnce[A,
                                        B,
                                        SA[A] <: IterableOnce[A],
                                        SB[B] <: IterableOnce[B]](
      implicit cbf: Factory[B, SB[B]],
      tr: ScopeTranslator[A, B]): ScopeTranslator[SA[A], SB[B]] =
    new ScopeTranslator[SA[A], SB[B]] {
      override def translate(a: SA[A]): SB[B] = {

        a.iterator
          .foldLeft(cbf.newBuilder)((acc, a) => acc += tr.translate(a))
          .result()
      }
    }

  implicit def translateMap[K1, V1, K2, V2](
      implicit trk: ScopeTranslator[K1, K2],
      trv: ScopeTranslator[V1, V2]
  ): ScopeTranslator[Map[K1, V1], Map[K2, V2]] =
    new ScopeTranslator[Map[K1, V1], Map[K2, V2]] {
      override def translate(a: Map[K1, V1]): Map[K2, V2] = {
        a.map {
          case (k1, v1) => trk.translate(k1) -> trv.translate(v1)
        }
      }
    }

  def apply[From, To](a: From)(implicit tr: ScopeTranslator[From, To]): To =
    tr.translate(a)

}
