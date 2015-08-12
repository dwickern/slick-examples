import slick.driver.H2Driver.api._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Await
import scala.concurrent.duration.Duration

object InsertIfNotExists extends App {
  val users = TableQuery[Users]

  def insertIfNotExists(name: String) = users.forceInsertQuery {
    val exists = (for (u <- users if u.name === name.bind) yield u).exists
    val insert = (name.bind, None) <> (User.apply _ tupled, User.unapply)
    for (u <- Query(insert) if !exists) yield u
  }

  val db = Database.forURL(
    url = "jdbc:h2:mem:test",
    keepAliveConnection = true
  )
  try {
    val sql = insertIfNotExists("dummy").statements.head
    println("Generated SQL: " + sql)

    Await.result(db.run(DBIO.seq(
      users.schema.create,

      // insert two different users named Bob, each with a unique auto-generated ID
      users += User("Bob"),
      users += User("Bob"),

      // this will do nothing because user "Bob" already exists
      insertIfNotExists("Bob"),

      // insert a new user "Fred"
      insertIfNotExists("Fred"),

      // this will do nothing because user "Fred" already exists
      insertIfNotExists("Fred"),

      // we should end up with 3 users
      users.result.map(println)
    )), Duration.Inf)
  } finally db.close()
}
