package hotkitchen

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import hotkitchen.db.UserDaoImpl
import hotkitchen.plugins.configureRouting
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.respond
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.Database

// TODO: do this properly
const val secret = "3v4IUndwTyDNVAwQcTyM9Ty/Z8qzpecilZTzaOevqlA="
const val issuer = "http://0.0.0.0:28852/"
const val audience = "http://0.0.0.0:28852"
const val myRealm = "Access to hot kitchen"


fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)

}

fun Application.module() {

    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
        })
    }
    install(Authentication) {
        jwt("auth-jwt") {
            realm = myRealm
            verifier(
                JWT
                    .require(Algorithm.HMAC256(secret))
                    .withAudience(audience)
                    .withIssuer(issuer)
                    .build()
            )
            // TODO: validation here and/or in the endpoint?
            validate { credential ->
                if (credential.payload.getClaim("email").asString() != "") {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
            // TODO
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized)
            }

        }
    }

    val database = Database.connect(
        url = "jdbc:postgresql://localhost:5432/hotkitchen",
        driver = "org.postgresql.Driver",
        user = "test",
        password = "superSecretPassword"
    )
    val dao = UserDaoImpl()
    configureRouting(dao)
}
