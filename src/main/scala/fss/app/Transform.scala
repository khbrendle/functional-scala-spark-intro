package fss.app

import org.apache.spark.sql.{Dataset, SparkSession}
import fss.app.Types._

object Transform {
  def filterFilms(spark: SparkSession, films: Dataset[Film]): Dataset[Film] = {
    films
      .filter(_.length match {
        case Some(l) =>
          l >= 90 // only include films with length of 1 hour or more
        case None =>
          false // if no length then exclude
      })
  }

  final case class FilmActorTuple(film_id: Option[Long], actor_id: Long, actor_name: Option[String])

  def joinFilmsAndActors(
      spark: SparkSession,
      films: Dataset[Film],
      filmActors: Dataset[FilmActor],
      actors: Dataset[Actor]
  ): Dataset[FilmActorTuple] = {
    import spark.implicits._

    films
      .alias("film") // provide alias to prevent column name clashing
      .joinWith(filmActors.alias("film_actors"), $"film.film_id" <=> $"film_actors.film_id")
      .as[(Film, FilmActor)] // when we join we create a tuple of both datasets
      .map(r => FilmActorTuple(Some(r._1.film_id), r._2.actor_id, None))
      .alias("film_actor")
      .joinWith(actors.alias("actors"), $"film_actor.actor_id" <=> $"actors.actor_id", "right")
      .as[(Option[FilmActorTuple], Actor)] // here we did a right join so the left side is optional
      .map(_ match {
        case (None, a) => // if the actor has no films we still want their record
          FilmActorTuple(film_id = None, actor_id = a.actor_id, actor_name = Some(a.fullName))
        case (Some(fat), a) => // if they do have films, just add their name to the records
          fat.copy(actor_name = Some(a.fullName))
      })
  }

  final case class ActorFilmCount(actor_name: String, film_count: Long)
  def calculateActorFilmCount(spark: SparkSession, filmActors: Dataset[FilmActorTuple]): Dataset[ActorFilmCount] = {
    import spark.implicits._

    filmActors
      .flatMap(r => // convert to object representing single row counts that we can later add
        r.actor_name match {
          case Some(n) => // if actor has a name then create a count record
            if (r.film_id.isDefined)
              Seq(ActorFilmCount(n, 1))
            else
              Seq(ActorFilmCount(n, 0))
          case None => // if actor does not have a name then exclude from analysis
            Seq[ActorFilmCount]()
        })
      .groupByKey(_.actor_name) // group by name to create pair (name, ActorFilmCount)
      .reduceGroups((v1, v2) => ActorFilmCount(v1.actor_name, v1.film_count + v2.film_count))
      .map(_._2) // now that we reduced, just keep the ActorFilmCount
  }
}
