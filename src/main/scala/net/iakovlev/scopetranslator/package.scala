package net.iakovlev

package object scopetranslator {
  object syntax {
    implicit class ScopeTranslatorOps[From](a: From) {
      def to[To](implicit tr: ScopeTranslator[From, To]): To = tr.translate(a)
    }
  }
}
