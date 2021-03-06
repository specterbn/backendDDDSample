package port.secondary.database.user

import application.dto.UserRecord
import com.google.inject.{Inject, Singleton}
import domain.masterData.user.{Credential, User, UserRepository}
import utility.exceptions.EntityNotFound
import utility.repository.AbstractDao

import scala.util.{Failure, Success, Try}

/**
  * Created by specter8x on 3/9/17.
  */

@Singleton
class UserRepositoryImpl @Inject() (userDao: UserDao) extends UserRepository{

  override val CONSTRAINT_VIOLATION_MSG = "user.error.email.existing"
  override protected val dao = userDao

  override protected def checkInvariant(user: User): Boolean =
    getByEmail(user.email, allowInactive = true) match {
      case Failure(e) =>
        e match {
          case _:EntityNotFound => true
          case _ => throw e
        }
      case Success(existingUser) => user isSameAs existingUser
    }

  override def getByEmail(email: String, allowInactive: Boolean): Try[User] = {
    getAll(allowInactive).map(_.find(_.email == email) match {
      case None => throw new EntityNotFound("Couldn't find User with Email: " + email)
      case Some(user) => user
    })
  }

  override protected def record2Entity(record: UserRecord): User = {
    User(
      record.userId,
      record.userName,
      record.email,
      record.isInactive,
      Credential(record.password,record.salt)
    )
  }

  override protected def entity2Record(entity: User): UserRecord = {
    UserRecord(
      entity.userId,
      entity.userName,
      entity.email,
      entity.isInactive,
      entity.credential.hashedPassword,
      entity.credential.salt
    )
  }
}
