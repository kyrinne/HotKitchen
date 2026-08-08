package hotkitchen.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import hotkitchen.audience
import hotkitchen.db.UserDaoImpl
import hotkitchen.issuer
import hotkitchen.model.User
import hotkitchen.secret
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.request.receive
import io.ktor.server.request.receiveParameters
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.Date
import kotlin.collections.hashMapOf

// TODO: clean up - figure out a good file structure
@Serializable
data class Login(val email: String, val password: String)

@Serializable
data class Token(val token: String)

// TODO: username, email, ...?
fun createJWT(username: String): String? = JWT.create()
    .withAudience(audience)
    .withIssuer(issuer)
    .withClaim("username", username)
    .withExpiresAt(Date(System.currentTimeMillis() + 24 * 60 * 60000))
    .sign(Algorithm.HMAC256(secret))

/**
 * [email] is valid if there is a prefix and a domain
 */
fun validateEmail(email: String): Boolean = email.matches(Regex(".+@.+\\..+"))

/**
 * [password] is valid if it is at least 6 characters long and contains at least one letter and one number
 */
fun validatePassword(password: String): Boolean {
    return password.length >= 6
            && password.contains(Regex("[0-9]+"))
            && password.contains(Regex("[a-zA-Z]+"))
}

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
                    call.respond(HttpStatusCode.Forbidden, Status("User already exists"))
                } else {
                    // TODO: clean up
                    if (!validateEmail(user.email)) {
                        call.respond(HttpStatusCode.Forbidden, Status("Invalid email"))
                    }
                    if (!validatePassword(user.password)) {
                        call.respond(HttpStatusCode.Forbidden, Status("Invalid password"))
                    }
                    if (validateEmail(user.email) && validatePassword(user.password)) {
                        dao.addUser(email = user.email, userType = user.userType, password = user.password)
                        val token = createJWT(user.email)!! // TODO
                        call.respond(Token(token))
                    }
                }
            } catch (_: Throwable) {
                call.respond(HttpStatusCode.Forbidden, Status("Registration failed"))
                // call.respond(HttpStatusCode.BadRequest)
            }
        }
        post("/signin") {
            try {
                val (email, password) = call.receive<Login>()
                if (dao.allUsers().singleOrNull() { it.email == email && it.password == password } != null) {
                    val token = createJWT(email)!! // TODO
                    call.respond(Token(token))
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
