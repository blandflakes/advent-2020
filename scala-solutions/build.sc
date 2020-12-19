import mill._, scalalib._

object advent extends ScalaModule {
  def scalaVersion = "2.13.4"
  override def ivyDeps = Agg(
    ivy"org.typelevel::cats-effect:2.3.0",
    ivy"org.typelevel::cats-core:2.3.0",
    ivy"org.tpolecat::atto-core:0.7.0"
  )
}
