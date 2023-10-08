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
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

class KtorAuthenticator

