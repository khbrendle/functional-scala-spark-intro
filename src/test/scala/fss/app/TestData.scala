package fss.app

import fss.app.Types._

object TestData {
  val defaultFilm: Film = Film(
    film_id = 101, // encoding all of my films to be 10...
    title = "film title",
    description = Some("film description"),
    release_year = Some(2022L),
    language_id = Some(2),
    original_language_id = Some(3),
    rental_duration = 4,
    rental_rate = 4.990000000000000000,
    length = Some(95),
    replacement_cost = 16.500000000000000000,
    rating = "PG-13",
    last_update = java.sql.Timestamp.valueOf("2022-03-01 12:30:00"),
    special_features = Array[String]("a feature"),
    fulltext = "more text"
  )

  val defaultActor: Actor = Actor(
    actor_id = 111, // encoding all actors to be 11...
    first_name = "Bob",
    last_name = "Filmstar",
    last_update = java.sql.Timestamp.valueOf("2020-01-01 15:58:30")
  )

  val defaultFilmActor: FilmActor = FilmActor(
    actor_id = 111,
    film_id = 101,
    last_update = java.sql.Timestamp.valueOf("2022-03-01 12:30:15")
  )
}
