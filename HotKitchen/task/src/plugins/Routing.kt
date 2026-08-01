package hotkitchen.plugins

import hotkitchen.db.UserDaoImpl
import hotkitchen.model.User
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.server.application.*
import io.ktor.server.request.receive
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

// TODO: clean up
@Serializable
data class Login(val email: String, val password: String)

fun Application.configureRouting(dao: UserDaoImpl) {
    routing {
        get("/") {
            call.respondText("Hello World!")
        }

        @Serializable
        data class Status(val status: String)

        post("/signup") {
            try {
                val user = call.receive<User>()
                if (dao.allUsers().map { it.email }.contains(user.email)) {
                    call.respond(HttpStatusCode.Forbidden, Status("Registration failed"))
                } else {
                    // TODO: clean up
                    dao.addUser(user.email, user.userType, user.password)
                    call.respond(Status("Signed Up"))
                }
            } catch (_: Throwable) {
                call.respond(HttpStatusCode.Forbidden, Status("Registration failed"))
                // call.respond(HttpStatusCode.BadRequest)
            }
        }
        post("/signin") {
            try {
                val (email, password) = call.receive<Login>()
                println("$email $password")
                if (dao.allUsers().singleOrNull() { it.email == email && it.password == password } != null) {
                    call.respond(Status("Signed In"))
                } else {
                    call.respond(HttpStatusCode.Forbidden, Status("Authorization failed"))
                }
            } catch (_: Throwable) {
                call.respond(HttpStatusCode.Forbidden, Status("Authorization failed"))
                // call.respond(HttpStatusCode.BadRequest)
            }
        }
        get("/users") {
            val users = dao.allUsers()
            call.respond("${users.size} users")
        }
    }
}
