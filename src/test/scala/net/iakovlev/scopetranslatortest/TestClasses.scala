package net.iakovlev.scopetranslatortest

import net.iakovlev.scopetranslatortest.AttractorPB.AttractorModePB.AttractorContentPB
import net.iakovlev.scopetranslatortest.AttractorPB.{
  AttractorModePB,
  ReferenceSystemPB
}

case class CartesianCoordinate2(X: Double, Y: Double)
case class Coordinate(latitude: Double,
                      longitude: Double,
                      altitude: Option[Double])

case class Attractor(LocalReference: ReferenceSystem,
                     DetectionRadius: Double,
                     Modes: List[AttractorMode])

case class AttractorMode(Radius: Double,
                         Tolerance: Option[Double],
                         SquareRadius: Option[Double],
                         Center: CartesianCoordinate2,
                         Content: List[AttractorContent])

case class AttractorContent(TimestampMs: Long, Center: CartesianCoordinate2)
final case class CartesianCoordinate2PB(
    x: _root_.scala.Double,
    y: _root_.scala.Double
)

case class ReferenceSystem(Coordinates: Coordinate,
                           LatitudeFactor: Double,
                           LongitudeFactor: Double)

object AttractorPB {
  final case class AttractorModePB(
      radius: _root_.scala.Double,
      tolerance: scala.Option[_root_.scala.Double] = None,
      squareRadius: scala.Option[_root_.scala.Double] = None,
      center: CartesianCoordinate2PB,
      content: _root_.scala.collection.Seq[AttractorContentPB] =
        _root_.scala.collection.Seq.empty
  )

  object AttractorModePB {
    final case class AttractorContentPB(
        timestampMs: _root_.scala.Long,
        center: CartesianCoordinate2PB
    )
  }
  final case class ReferenceSystemPB(
      coordinates: CoordinatePB,
      latitudeFactor: _root_.scala.Double,
      longitudeFactor: _root_.scala.Double
  )
}
final case class AttractorPB(
    localReference: ReferenceSystemPB,
    detectionRadius: _root_.scala.Double,
    modes: _root_.scala.collection.Seq[AttractorModePB] =
      _root_.scala.collection.Seq.empty
)

final case class CoordinatePB(
    latitude: _root_.scala.Double,
    longitude: _root_.scala.Double,
    altitude: scala.Option[_root_.scala.Double] = None
)
