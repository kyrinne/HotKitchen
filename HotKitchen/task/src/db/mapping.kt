package hotkitchen.db

import hotkitchen.model.User
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

object UserTable : Table("users") {
    val email = varchar("email", 100)
    val userType = varchar("usertype", 50)
    val password = varchar("password", 50)
}

interface UserDao {
    suspend fun allUsers(): List<User>
    suspend fun addUser(email: String, userType: String, password: String): User?
}

class UserDaoImpl: UserDao {
    override suspend fun allUsers(): List<User> =
        newSuspendedTransaction(Dispatchers.IO) {
            UserTable.selectAll().map {
                User(
                    email = it[UserTable.email],
                    userType = it[UserTable.userType],
                    password = it[UserTable.password],
                )
            }
        }

    override suspend fun addUser(email: String, userType: String, password: String): User? {
        val newUser = newSuspendedTransaction(Dispatchers.IO) {
            // TODO: fix IDs, use DAO notation
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

}


