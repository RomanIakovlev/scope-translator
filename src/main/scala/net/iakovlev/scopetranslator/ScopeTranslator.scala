package net.iakovlev.scopetranslator

import shapeless._
import shapeless.labelled.{FieldType, field}

import scala.annotation.implicitNotFound

@implicitNotFound("Can't translate ${From} to ${To}")
abstract class ScopeTranslator[From, To] extends Serializable {
  def translate(a: From): To
}

object ScopeTranslator {
  implicit def translateTrivial[A, B](
      implicit ev: A =:= B): ScopeTranslator[A, B] =
    new ScopeTranslator[A, B] {
      override def translate(a: A): B = ev(a)
    }

  implicit def translateCNilLeft[A] = new ScopeTranslator[CNil, A] {
    override def translate(a: CNil): A = sys.error("CNil from the left!")
  }

  implicit def translateCNilRight[A] = new ScopeTranslator[A, CNil] {
    override def translate(a: A): CNil = sys.error("CNil from the right!")
  }

  implicit def translateOptional[A, B](implicit tr: ScopeTranslator[A, B])
    : ScopeTranslator[Option[A], Option[B]] =
    new ScopeTranslator[Option[A], Option[B]] {
      override def translate(a: Option[A]): Option[B] = {
        a.map(tr.translate)
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
      implicit wka: Witness.Aux[KA],
      wkb: Witness.Aux[KB],
      genA: Generic.Aux[HA, HARepr],
      genB: Generic.Aux[HB, HBRepr],
      tra: ScopeTranslator[FieldType[KA, HA] :+: TA, TB],
      trb: ScopeTranslator[TA, FieldType[KB, HB] :+: TB],
      tr: ScopeTranslator[HARepr, HBRepr]
  ): ScopeTranslator[FieldType[KA, HA] :+: TA, FieldType[KB, HB] :+: TB] =
    new ScopeTranslator[FieldType[KA, HA] :+: TA, FieldType[KB, HB] :+: TB] {
      override def translate(
          a: (FieldType[KA, HA] :+: TA)): (FieldType[KB, HB] :+: TB) = {
        a match {
          case Inl(h) =>
            if (wka.value.name == wkb.value.name)
              Inl(field[KB](genB.from(tr.translate(genA.to(h)))))
            else Inr(tra.translate(a))
          case Inr(t) => trb.translate(t)
        }
      }
    }

  implicit val translateHNil = new ScopeTranslator[HNil, HNil] {
    override def translate(a: HNil): HNil = HNil
  }

  implicit def translateHCons[K <: Symbol, HA, TA <: HList, HB, TB <: HList](
      implicit trh: Lazy[ScopeTranslator[HA, HB]],
      trt: ScopeTranslator[TA, TB]
  ): ScopeTranslator[FieldType[K, HA] :: TA, FieldType[K, HB] :: TB] =
    new ScopeTranslator[FieldType[K, HA] :: TA, FieldType[K, HB] :: TB] {
      override def translate(
          a: FieldType[K, HA] :: TA): FieldType[K, HB] :: TB = {
        field[K](trh.value.translate(a.head)) :: trt.translate(a.tail)
      }
    }

  implicit def translateCaseClass[A, ARepr, B, BRepr](
      implicit lga: LabelledGeneric.Aux[A, ARepr],
      lgb: LabelledGeneric.Aux[B, BRepr],
      tr: ScopeTranslator[ARepr, BRepr],
      lp: LowPriority): ScopeTranslator[A, B] =
    new ScopeTranslator[A, B] {
      override def translate(a: A): B = {
        lgb.from(tr.translate(lga.to(a)))
      }
    }

  implicit def translateCoproduct[A, B, RA <: Coproduct, RB <: Coproduct](
      implicit lgb: LabelledGeneric.Aux[B, RB],
      lga: LabelledGeneric.Aux[A, RA],
      tr: ScopeTranslator[RA, RB]) =
    new ScopeTranslator[A, B] {
      override def translate(a: A): B = {
        lgb.from(tr.translate(lga.to(a)))
      }
    }

  def apply[From, To](a: From)(implicit tr: ScopeTranslator[From, To]): To =
    tr.translate(a)

}
