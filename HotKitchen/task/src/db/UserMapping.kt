package db

import db.UserTable.address
import db.UserTable.name
import db.UserTable.phone
import db.UserTable.userType
import model.User
import model.UserProfile
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update

object UserTable : Table("users") {
    val email = varchar("email", 100)
    val userType = varchar("usertype", 50)
    val password = varchar("password", 50)
    val name = varchar("name", 100).nullable()
    val phone = varchar("phone", 30).nullable()
    val address = varchar("address", 200).nullable()
}
// TODO: figure out notations in general
// TODO: don't abuse `allUsers()` - use select instead
interface UserDao {
    suspend fun allUsers(): List<User>
    suspend fun addUser(email: String, userType: String, password: String): User?
    suspend fun updateProfile(userProfile: UserProfile): UserProfile?
    suspend fun getProfile(email: String): UserProfile?
    suspend fun deleteUser(email: String): Boolean
}

class UserDaoImpl: UserDao {
    override suspend fun allUsers(): List<User> =
        newSuspendedTransaction(Dispatchers.IO) {
            UserTable.selectAll().map {
                User(
                    email = it[UserTable.email],
                    userType = it[userType],
                    password = it[UserTable.password],
                    name = it[name],
                    phoneNumber = it[phone],
                    address = it[address],
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
                it[UserTable.phone] = userProfile.phone
                it[UserTable.address] = userProfile.address
            }
        }
        return when (updatedRecords) {
            0 -> null
            1 -> userProfile
            else -> throw Exception("More than one user found for email: ${userProfile.email}.")
        }
    }
    override suspend fun getProfile(email: String): UserProfile? {
        return newSuspendedTransaction(Dispatchers.IO) {
            UserTable.selectAll().where { UserTable.email eq email }.map {
                // TODO: surely there's a more elegant way?!
                val name = it.getOrNull(name)
                val phone = it.getOrNull(phone)
                val address = it.getOrNull(address)
                if (name != null && phone != null && address != null) {
                    UserProfile(
                        name = name,
                        userType = it[userType],
                        phone = phone,
                        email = it[UserTable.email],
                        address = address,
                    )
                } else null
            }.singleOrNull() // TODO: error handling
        }
    }
    override suspend fun deleteUser(email: String): Boolean {
        val deleted = newSuspendedTransaction(Dispatchers.IO) {
            UserTable.deleteWhere { UserTable.email eq email }
        }
        return deleted > 0
    }
}


