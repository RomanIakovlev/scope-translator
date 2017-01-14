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
