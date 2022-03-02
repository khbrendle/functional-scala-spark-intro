package fss.app

object Types {
  @SuppressWarnings(Array("org.wartremover.warts.ArrayEquals"))
  final case class Film(
      film_id: Long,
      title: String,
      description: Option[String],
      release_year: Option[Long],
      language_id: Option[Short],
      original_language_id: Option[Short],
      rental_duration: Short,
      rental_rate: BigDecimal,
      length: Option[Short],
      replacement_cost: BigDecimal,
      rating: String,
      last_update: java.sql.Timestamp,
      special_features: Array[String],
      fulltext: String
  )

  final case class Actor(
      actor_id: Long,
      first_name: String,
      last_name: String,
      last_update: java.sql.Timestamp
  ) {
    def fullName: String = s"${first_name} ${last_name}"
  }

  final case class FilmActor(
      actor_id: Long,
      film_id: Long,
      last_update: java.sql.Timestamp
  )
}
