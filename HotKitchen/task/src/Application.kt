package hotkitchen

import hotkitchen.db.UserDaoImpl
import hotkitchen.db.UserTable
import hotkitchen.plugins.configureRouting
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {

    val database = Database.connect(
        url = "jdbc:postgresql://localhost:5432/hotkitchen",
        driver = "org.postgresql.Driver",
        user = "test",
        password = "superSecretPassword"
    )
    val dao = UserDaoImpl()
    configureRouting(dao)
}