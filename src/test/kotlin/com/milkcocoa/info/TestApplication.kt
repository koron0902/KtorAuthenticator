package com.milkcocoa.info

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuthException
import com.milkcocoa.info.authentocator.FirebaseAuthProvider
import com.milkcocoa.info.authentocator.HeaderAuthProvider
import com.milkcocoa.info.authentocator.firebase
import com.milkcocoa.info.authentocator.header
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

class TestApplication

fun AuthenticationConfig.configureHeaderAuth(){
    header(name = "meow"){
        headerName = "meow"
        validate = { call, headerValue ->
            headerValue == "meow!!!!"
        }
        whenReject = { call, throwable ->
            when(throwable){
                is HeaderAuthProvider.HeaderNotProvidedException ->{
                    call.respondText("header is not provided(meow)", status = HttpStatusCode.Unauthorized)
                }
                is HeaderAuthProvider.ValidateFailedException ->{
                    call.respondText("header value is not correct(meow)", status = HttpStatusCode.Unauthorized)

                }
            }
        }
    }

    header(name = "bow"){
        headerName = "bow"
        validate = { call, headerValue ->
            headerValue == "bow wow!"
        }
        whenReject = { call, throwable ->
            when(throwable){
                is HeaderAuthProvider.HeaderNotProvidedException ->{
                    call.respondText("header is not provided(bow)", status = HttpStatusCode.Unauthorized)
                }
                is HeaderAuthProvider.ValidateFailedException ->{
                    call.respondText("header value is not correct(bow)", status = HttpStatusCode.Unauthorized)

                }
            }
        }

        skipWhen { call ->
            call.request.headers.get("bow") == "bow-meow"
        }
    }
}
fun AuthenticationConfig.configureFirebaseAuth(){
    firebase("fa-jwt") {
        firebaseApp
        authorizationType = FirebaseAuthProvider.AuthorizationType.Jwt
        checkRevoked = false
        whenReject = { call, throwable ->
            when(throwable){
                is FirebaseAuthProvider.TokenNotProvidedException ->{
                    call.respondText("token is not provided", status = HttpStatusCode.Unauthorized)
                }
                is FirebaseAuthException ->{
                    call.respondText("firebase auth failed", status = HttpStatusCode.Unauthorized)
                }
                is IllegalArgumentException ->{
                    call.respondText("wtf", status = HttpStatusCode.Unauthorized)
                }
                is FirebaseAuthProvider.ClaimNotProvidedException ->{
                    call.respondText("missing token", status = HttpStatusCode.Unauthorized)
                }
            }
        }
    }

    firebase("fa-bearer") {
        firebaseApp
        authorizationType = FirebaseAuthProvider.AuthorizationType.Bearer
        checkRevoked = false
        whenReject = { call, throwable ->
            when(throwable){
                is FirebaseAuthProvider.TokenNotProvidedException ->{
                    call.respondText("token is not provided", status = HttpStatusCode.Unauthorized)
                }
                is FirebaseAuthException ->{
                    call.respondText("firebase auth failed", status = HttpStatusCode.Unauthorized)
                }
                is IllegalArgumentException ->{
                    call.respondText("wtf", status = HttpStatusCode.Unauthorized)
                }
                is FirebaseAuthProvider.ClaimNotProvidedException ->{
                    call.respondText("missing token", status = HttpStatusCode.Unauthorized)
                }
            }
        }
    }
}





fun Application.configureHeaderAuthRoute(){
    routing {
        authenticate("bow") {
            get("/bow"){
                call.respondText("bow wow!")
            }
        }

        authenticate("meow") {
            get("/meow"){
                call.respondText("meow!")
            }
        }

        get("/") {
            call.respondText("Hello, World!")
        }
    }
}

fun Application.configureFirebaseAuthRoute(){
    routing {

        authenticate("fa-jwt") {
            get("/fa_jwt"){
                call.respondText("firebase jwt")
            }
        }
        authenticate("fa-bearer") {
            get("/fa_bearer"){
                call.respondText("firebase bearer")
            }
        }

        get("/") {
            call.respondText("Hello, World!")
        }
    }
}