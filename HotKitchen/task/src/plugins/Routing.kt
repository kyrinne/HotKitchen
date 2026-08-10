package hotkitchen.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import hotkitchen.audience
import hotkitchen.db.UserDaoImpl
import hotkitchen.issuer
import hotkitchen.model.User
import hotkitchen.secret
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import java.util.Date

// TODO: clean up - figure out a good file structure
@Serializable
data class Login(val email: String, val password: String)

@Serializable
data class Token(val token: String)

fun createJWT(email: String, userType: String): String? = JWT.create()
    .withAudience(audience)
    .withIssuer(issuer)
    .withClaim("email", email)
    .withClaim("userType", userType)
    .withExpiresAt(Date(System.currentTimeMillis() + 24 * 60 * 60000))
    .sign(Algorithm.HMAC256(secret))

// TODO: revisit rules after the course is finished
/**
 * [email] is valid if there is a prefix and a domain; tests require exclusion of '#' even though it's technically valid
 */
fun validateEmail(email: String): Boolean {
    return email.matches(Regex("[a-z0-9\\-_]+@([a-z\\-]+\\.)+[a-z]+")) && email.count { it =='@'} == 1
}

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
                        val token = createJWT(user.email, user.userType)!! // TODO
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
                val user: User? = dao.allUsers().singleOrNull() { it.email == email && it.password == password }
                if (user != null) {
                    val token = createJWT(email, user.userType)!! // TODO
                    call.respond(Token(token))
                } else {
                    call.respond(HttpStatusCode.Forbidden, Status("Invalid email or password"))
                }
            } catch (_: Throwable) {
                call.respond(HttpStatusCode.Forbidden, Status("Invalid email or password"))
                // call.respond(HttpStatusCode.BadRequest)
            }
        }
        authenticate("auth-jwt") {
            get("/validate") {
                val principal: JWTPrincipal? = call.principal<JWTPrincipal>()
                if (principal != null) {
                    val email: String? = principal.payload.getClaim("email").asString()
                    val userType: String? = principal.payload.getClaim("userType").asString()
                    if (email != null && userType != null) {
                        call.respondText("Hello, $userType $email")
                    } else {
                        throw IllegalStateException("Email ($email) or user type ($userType) is null!")
                    }
                } else {
                    call.respond(HttpStatusCode.Forbidden)
                }
            }
            // TODO: Fetches the complete profile information for the authenticated user.
            get("/me") {
                /*
                {
                    "name": "Goose",
                    "userType": "client",
                    "phone": "+79999999999",
                    "email": "example@gmail.com",
                    "address": "address"
                }
                 */
                // TODO: 400 Bad Request if the profile hasn't been created yet

            }
            // TODO: Creates or updates the user's profile information.
            put("/me") {
                /*
                Request Body: A JSON object with the user's full profile.
                Important Constraint: The email field in the request body must match the email associated with the JWT.
                You should not change the email.

                Response: Same as GET
                 */

                // TODO: 400 Bad Request: This occurs if the email in the request body does not match the email in the authentication token.

            }
            // TODO: Deletes the user's entire account, including both their profile and their credentials.
            delete("/me") {

                /*
                Successful Response (200 OK): Indicates that the user account was successfully deleted. No response body is required.

                Failure Response (404 Not Found): This occurs if the user account does not exist (for example, if you try to delete the same user twice)
                 */
            }

        }
        get("/users") {
            val users = dao.allUsers()
            call.respond("${users.size} users")
        }
    }
}
