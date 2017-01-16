package net.iakovlev.scopetranslatortest

import net.iakovlev.scopetranslator.ScopeTranslator
import org.specs2.mutable.Specification
import shapeless.test.illTyped

class ScopeTranslatorSpec extends Specification {

  sealed trait Enum1
  object Enum1 {
    case object A extends Enum1
    case object B extends Enum1
  }
  sealed trait Enum2
  object Enum2 {
    case object A extends Enum2
    case object B extends Enum2
  }

  sealed trait ADT1
  object ADT1 {
    case class B(i: Int) extends ADT1
    case class A(i: Int) extends ADT1
  }
  sealed trait ADT2
  object ADT2 {
    case class A(i: Int) extends ADT2
    case class B(i: Int) extends ADT2
  }

  "Translator should translate" >> {
    "trivial cases" >> {
      case class C1()
      case class C2()
      ScopeTranslator[C1, C2](C1()) must_== C2()
    }
    "simple cases" >> {
      case class C1(i: Int)
      case class C2(i: Int)
      ScopeTranslator[C1, C2](C1(4)) must_== C2(4)
    }
    "nested cases" >> {
      case class Nested(s: String)
      case class C1(i: Int, n: Nested)
      case class C2(i: Int, n: Nested)
      val c1 = C1(4, Nested(",.p"))
      val c2 = C2(4, Nested(",.p"))
      ScopeTranslator[C1, C2](c1) must_== c2
    }
    "nested non-trivial cases" >> {
      case class Nested(s: String)
      case class Nested1(s: String)
      case class C1(i: Int, n: Nested)
      case class C2(i: Int, n: Nested1)
      val c1 = C1(4, Nested(",.p"))
      val c2 = C2(4, Nested1(",.p"))
      ScopeTranslator[C1, C2](c1) must_== c2
    }
    "deeply nested cases" >> {
      case class Nested(s: String, d: Deep)
      case class Nested1(s: String, d: Deep1)
      case class Deep(s: String, d: Deeper)
      case class Deeper(s: String)
      case class Deep1(s: String, d: Deeper1)
      case class Deeper1(s: String)
      case class C1(i: Int, n: Nested)
      case class C2(i: Int, n: Nested1)
      val c1 = C1(4, Nested(",.p", Deep("b", Deeper("c"))))
      val c2 = C2(4, Nested1(",.p", Deep1("b", Deeper1("c"))))
      ScopeTranslator[C1, C2](c1) must_== c2
    }
    "enum-like ADTs" >> {
      case class C1(a: Enum1, b: Enum1)
      case class C2(a: Enum2, b: Enum2)
      val c1 = C1(Enum1.A, Enum1.B)
      val c2 = C2(Enum2.A, Enum2.B)
      ScopeTranslator[C1, C2](c1) must_== c2
    }
    "ADTs with case classes" >> {
      case class C1(a: ADT1, b: ADT1)
      case class C2(a: ADT2, b: ADT2)
      val c1 = C1(ADT1.A(3), ADT1.B(4))
      val c2 = C2(ADT2.A(3), ADT2.B(4))
      ScopeTranslator[C1, C2](c1) must_== c2
    }
    "Optional primitive fields" >> {
      case class C1(i: Option[Int])
      case class C2(i: Option[Int])
      val c1 = C1(Some(3))
      val c2 = C2(Some(3))
      ScopeTranslator[C1, C2](c1) must_== c2
    }
    "Optional product fields" >> {
      case class Nested1(s: String)
      case class Nested2(s: String)
      case class C1(i: Option[Nested1], j: Int)
      case class C2(i: Option[Nested2], j: Int)
      val c1 = C1(Some(Nested1("3")), 2)
      val c2 = C2(Some(Nested2("3")), 2)
      ScopeTranslator[C1, C2](c1) must_== c2
    }
    "Lists" >> {
      case class C1(a: List[Int], s: String)
      case class C2(a: List[Int], s: String)
      val c1 = C1(List(4, 5), "a")
      val c2 = C2(List(4, 5), "a")
      ScopeTranslator[C1, C2](c1) must_== c2
    }
    "Vectors" >> {
      case class C1(a: Vector[Int], n: Int)
      case class C2(a: Vector[Int], n: Int)
      val c1 = C1(Vector(4, 5, 6), 4)
      val c2 = C2(Vector(4, 5, 6), 4)
      ScopeTranslator[C1, C2](c1) must_== c2
    }
    "Different TraversableOnce instances" >> {
      case class C1(a: Vector[Int], n: Int)
      case class C2(a: List[Int], n: Int)
      val c1 = C1(Vector(4, 5, 6), 4)
      val c2 = C2(List(4, 5, 6), 4)
      ScopeTranslator[C1, C2](c1) must_== c2
    }
    "Same TraversableOnce instances" >> {
      case class C1(a: Seq[Int], n: Int)
      case class C2(a: Seq[Int], n: Int)
      val c1 = C1(Vector(4, 5, 6), 4)
      val c2 = C2(List(4, 5, 6), 4)
      ScopeTranslator[C2, C1](c2) must_== c1
    }
    "Maps of primitive types" >> {
      case class C1(a: Map[String, Int])
      case class C2(a: Map[String, Int])
      val c1 = C1(Map("h" -> 1, "w" -> 5))
      val c2 = C2(Map("h" -> 1, "w" -> 5))
      ScopeTranslator[C1, C2](c1) must_== c2
    }
    "Maps of same types" >> {
      case class Key(s: String)
      case class Value(i: Int)
      case class C1(a: Map[Key, Value])
      case class C2(a: Map[Key, Value])
      val c1 = C1(Map(Key("h") -> Value(1), Key("w") -> Value(5)))
      val c2 = C2(Map(Key("h") -> Value(1), Key("w") -> Value(5)))
      ScopeTranslator[C1, C2](c1) must_== c2
    }
    "Maps of translatable types" >> {
      case class Key1(s: String)
      case class Value1(i: Int)
      case class Key2(s: String)
      case class Value2(i: Int)
      case class C1(a: Map[Key1, Value1])
      case class C2(a: Map[Key2, Value2])
      val c1 = C1(Map(Key1("h") -> Value1(1), Key1("w") -> Value1(5)))
      val c2 = C2(Map(Key2("h") -> Value2(1), Key2("w") -> Value2(5)))
      ScopeTranslator[C1, C2](c1) must_== c2
    }
  }

  "Translator should fail on mismatched" >> {
    "order" >> {
      case class Foo(i: Int, s: String)
      case class Bar(s: String, i: Int)
      illTyped("""ScopeTranslator[Foo, Bar](Foo(5, "hello"))""")
      success
    }
    "names" >> {
      case class Foo(i: Int)
      case class Bar(j: Int)
      illTyped("""ScopeTranslator[Foo, Bar](Foo(5))""")
      success
    }
    "types" >> {
      case class Foo(i: Int)
      case class Bar(i: Long)
      illTyped("""ScopeTranslator[Foo, Bar](Foo(5))""")
      success
    }
    "shapes" >> {
      case class Foo(i: Int)
      case class Bar(i: Int, s: String)
      illTyped("""ScopeTranslator[Foo, Bar](Foo(5))""")
      illTyped("""ScopeTranslator[Bar, Foo](Bar(5, "hello"))""")
      success
    }
  }

  "Translator syntax should be available" >> {
    import net.iakovlev.scopetranslator.syntax._
    case class C1()
    case class C2()
    val c1 = C1()
    val c2 = c1.to[C2]
    c2 must_== C2()
  }
}
