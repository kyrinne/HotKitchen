package hotkitchen.db

import hotkitchen.model.User
import hotkitchen.model.UserProfile
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update

object UserTable : Table("users") {
    val email = varchar("email", 100)
    val userType = varchar("usertype", 50)
    val password = varchar("password", 50)
    val name = varchar("name", 100)
    val phone = varchar("phone", 30)
    val address = varchar("address", 200)
}

interface UserDao {
    suspend fun allUsers(): List<User>
    suspend fun addUser(email: String, userType: String, password: String): User?
    suspend fun updateProfile(userProfile: UserProfile): UserProfile?
}

class UserDaoImpl: UserDao {
    override suspend fun allUsers(): List<User> =
        newSuspendedTransaction(Dispatchers.IO) {
            UserTable.selectAll().map {
                User(
                    email = it[UserTable.email],
                    userType = it[UserTable.userType],
                    password = it[UserTable.password],
                    name = it[UserTable.name],
                    phoneNumber = it[UserTable.phone],
                    address = it[UserTable.address],
                )
            }
        }

    override suspend fun addUser(email: String, userType: String, password: String): User? {
        val newUser = newSuspendedTransaction(Dispatchers.IO) {
            // TODO: use DAO notation
            UserTable.insert {
                it[UserTable.email] = email
                it[UserTable.userType] = userType
                it[UserTable.password] = password
            }
        }.resultedValues?.singleOrNull()?.let { row ->
            User(
                email = row[UserTable.email],
                userType = row[UserTable.userType],
                password = row[UserTable.password],
            )
        }
        return newUser
    }
    override suspend fun updateProfile(userProfile: UserProfile): UserProfile? {
        val updatedRecords = newSuspendedTransaction(Dispatchers.IO) {
            // do not update email
            UserTable.update(where = { UserTable.email eq userProfile.email }) {
                it[UserTable.name] = userProfile.name
                it[UserTable.userType] = userProfile.userType
                it[UserTable.phone] = userProfile.phoneNumber
                it[UserTable.address] = userProfile.address
            }
        }
        return when (updatedRecords) {
            0 -> null
            1 -> userProfile
            else -> throw Exception("More than one user found for email: ${userProfile.email}.")
        }
    }


}


