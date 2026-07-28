package hotkitchen.plugins

import hotkitchen.db.UserDaoImpl
import io.ktor.http.Parameters
import io.ktor.server.application.*
import io.ktor.server.request.receive
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting(dao: UserDaoImpl) {
    routing {
        get("/") {
            call.respondText("Hello World!")
        }
        post("/add-user") {
            val formParameters = call.receive<Parameters>()
            val name = formParameters["name"]
            val email = formParameters["email"]
            val type = formParameters["type"]
            val password = formParameters["password"]
            // TODO: proper error handling, remove name
            dao.addUser(email = email!!, userType = type!!, password = password!!)
            call.respondText("Hello $name!")
        }

        get("/users") {
            val users = dao.allUsers()
            call.respond("${users.size} users")
        }
    }
}
