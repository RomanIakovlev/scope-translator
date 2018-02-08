# Scope translator

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/net.iakovlev/scope-translator_2.12/badge.svg)](https://maven-badges.herokuapp.com/maven-central/net.iakovlev/scope-translator_2.12/)

## Quote
> The original is unfaithful to the translation.
>
>― **Jorge Luis Borges**

## Overview
 
This project aims to provide translation between Scala types of identical shape.
It is particularly useful when an identical set of types exist in different scopes (packages, etc.).

The following rules have to be obeyed for successful translation:

1. Members of product types (case classes) have to have same types and be in the same order; prior to version 0.2.0 the member names were also taken into account, but in 0.2.0 this restriction has been removed.
2. Members of coproduct types (sealed traits families) have to have the same name, order of declaration doesn't matter.

Refer to tests for a more detailed illustration of these rules, where both positive and negative examples are available.

If translation fails for some reason (types don't match, etc.), it results in a compile time error.

## Usage
Add dependency to your project:

In `build.sbt`:
```scala
libraryDependencies += "net.iakovlev" %% "scope-translator" % "0.2.1"
```

Scope translator is published to Sonatype and synced to Maven Central.
Supported Scala versions are 2.11 and 2.12.

Then you can use scope translator in two ways:

* Directly by calling `ScopeTranslator[From, To](a: From)`:
```scala
import net.iakovlev.scopetranslator.ScopeTranslator
case class C1()
case class C2()
val c1 = C1()
val c2: C2 = ScopeTranslator[C1, C2](c1)
```

* By importing the 
translation syntax: 
```scala
import net.iakovlev.scopetranslator.syntax._
case class C1()
case class C2()
val c1 = C1()
val c2: C2 = c1.to[C2]
```  

## Copyright and License

All code is licensed under the MIT license, available at
http://opensource.org/licenses/mit-license.php and also in the COPYING
file.
