package fss.app

import fss.app.TestData._
import fss.app.Transform._
import fss.app.Utils.withSpark
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should

@SuppressWarnings(
  Array("org.wartremover.warts.Nothing", "org.wartremover.warts.Any", "org.wartremover.warts.NonUnitStatements"))
class TransformTest extends AnyFunSpec with should.Matchers {
  withSpark { spark =>
    import spark.implicits._

    describe("filter films") {
      it("should include films that are greater than 90 minutes") {
        val films = Seq(defaultFilm)

        val out = filterFilms(spark, films.toDS())

        out.collect().length shouldBe 1
      }

      it("should include films that are exactly 90 minutes") {
        val films = Seq(defaultFilm.copy(length = Some(90)))

        val out = filterFilms(spark, films.toDS())

        out.collect().length shouldBe 1
      }

      it("should exclude films that have no length") {
        val films = Seq(defaultFilm.copy(length = None))

        val out = filterFilms(spark, films.toDS())

        out.collect().length shouldBe 0
      }

      it("should exclude films that are less than 90 minutes") {
        val films = Seq(defaultFilm.copy(length = Some(89)))

        val out = filterFilms(spark, films.toDS())

        out.collect().length shouldBe 0
      }

    }
  }
}
