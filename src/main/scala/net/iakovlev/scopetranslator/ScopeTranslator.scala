package net.iakovlev.scopetranslator

import shapeless._
import shapeless.labelled.{FieldType, field}

import scala.annotation.implicitNotFound
import scala.collection.generic.CanBuildFrom
import scala.language.higherKinds

@implicitNotFound("Can't translate ${From} to ${To}")
abstract class ScopeTranslator[From, To] extends Serializable {
  def translate(a: From): To
}

object ScopeTranslator {
  implicit def translateTrivial[A, B](implicit ev: A =:= B,
                                      lp: LowPriority): ScopeTranslator[A, B] =
    new ScopeTranslator[A, B] {
      override def translate(a: A): B = ev(a)
    }

  implicit def translateCNil: ScopeTranslator[CNil, CNil] {
    def translate(a: CNil): CNil
  } = new ScopeTranslator[CNil, CNil] {
    override def translate(a: CNil): CNil = sys.error("CNil!")
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
                                        SA[A] <: TraversableOnce[A],
                                        SB[B] <: TraversableOnce[B]](
      implicit cbf: CanBuildFrom[SB[B], B, SB[B]],
      tr: Strict[ScopeTranslator[A, B]]): ScopeTranslator[SA[A], SB[B]] =
    new ScopeTranslator[SA[A], SB[B]] {
      override def translate(a: SA[A]): SB[B] = {
        a.foldLeft(cbf())((acc, a) => acc += tr.value.translate(a)).result()
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

  implicit def translateEnum[KA <: Symbol,
                             KB <: Symbol,
                             HA,
                             TA <: Coproduct,
                             HB,
                             TB <: Coproduct,
                             HARepr <: HList,
                             HBRepr <: HList](
      implicit genA: Generic.Aux[HA, HARepr],
      genB: Generic.Aux[HB, HBRepr],
      trTails: ScopeTranslator[TA, TB],
      tr: ScopeTranslator[HARepr, HBRepr]
  ): ScopeTranslator[FieldType[KA, HA] :+: TA, FieldType[KB, HB] :+: TB] =
    new ScopeTranslator[FieldType[KA, HA] :+: TA, FieldType[KB, HB] :+: TB] {
      override def translate(
          a: (FieldType[KA, HA] :+: TA)): (FieldType[KB, HB] :+: TB) = {
        a match {
          case Inl(h) =>
            Inl(field[KB](genB.from(tr.translate(genA.to(h)))))
          case Inr(t) => Inr(trTails.translate(t))
        }
      }
    }

  implicit val translateHNil = new ScopeTranslator[HNil, HNil] {
    override def translate(a: HNil): HNil = HNil
  }

  implicit def translateHCons[KA <: Symbol,
                              KB <: Symbol,
                              HA,
                              TA <: HList,
                              HB,
                              TB <: HList](
      implicit trh: Strict[ScopeTranslator[HA, HB]],
      trt: ScopeTranslator[TA, TB]
  ): ScopeTranslator[FieldType[KA, HA] :: TA, FieldType[KB, HB] :: TB] =
    new ScopeTranslator[FieldType[KA, HA] :: TA, FieldType[KB, HB] :: TB] {
      override def translate(
          a: FieldType[KA, HA] :: TA): FieldType[KB, HB] :: TB = {
        field[KB](trh.value.translate(a.head)) :: trt.translate(a.tail)
      }
    }

  implicit def translateCaseClass[A, ARepr, B, BRepr](
      implicit lga: LabelledGeneric.Aux[A, ARepr],
      lgb: LabelledGeneric.Aux[B, BRepr],
      tr: Strict[ScopeTranslator[ARepr, BRepr]]): ScopeTranslator[A, B] =
    new ScopeTranslator[A, B] {
      override def translate(a: A): B = {
        lgb.from(tr.value.translate(lga.to(a)))
      }
    }

  implicit def translateCoproduct[A, B, RA <: Coproduct, RB <: Coproduct](
      implicit lgb: LabelledGeneric.Aux[B, RB],
      lga: LabelledGeneric.Aux[A, RA],
      tr: ScopeTranslator[RA, RB],
      lp: LowPriority) =
    new ScopeTranslator[A, B] {
      override def translate(a: A): B = {
        lgb.from(tr.translate(lga.to(a)))
      }
    }

  def apply[From, To](a: From)(implicit tr: ScopeTranslator[From, To]): To =
    tr.translate(a)

}
