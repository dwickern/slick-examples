import slick.driver.H2Driver.api._

case class User(name: String, id: Option[Int] = None)

class Users(tag: Tag) extends Table[User](tag, "USERS") {
  def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
  def name = column[String]("NAME")

  def * = (name, id.?) <> (User.tupled, User.unapply)
}
