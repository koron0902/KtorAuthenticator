package com.milkcocoa.info

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.testing.*
import io.ktor.server.testing.TestApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Disabled
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class FirebaseAuthProviderTest{
    val firebaseApiKey = "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
    val obtainTokenUrl =
        URL("https://identitytoolkit.googleapis.com/v1/accounts:signUp?key=$firebaseApiKey")

    companion object{
        init {
            val firebaseJson = "/firebase-admin.json"
            val options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(TestApplication::class.java.getResourceAsStream(firebaseJson)))
                .build()


            FirebaseApp.initializeApp(options)
        }
    }

    private suspend fun getFirebaseAccessToken(): String{
        return withContext(Dispatchers.IO){

            val conn = (obtainTokenUrl.openConnection() as HttpURLConnection).apply {
                doOutput = true
                doInput = true
                setChunkedStreamingMode(0)
                setRequestProperty("Content-Type", "application/json")
            }
            conn.connect()
            conn.outputStream.use { os ->
                BufferedWriter(OutputStreamWriter(os)).use { bw ->
                    bw.write("""{"returnSecureToken": true}""")
                    bw.flush()
                }
            }
            println(conn.responseCode)
            println(conn.responseMessage)
            return@withContext conn.inputStream.use { `is` ->
                String(`is`.readAllBytes()).let {
                    ObjectMapper().readTree(it)
                }
            }.get("idToken").asText()
        }
    }


    @Test
    @Disabled("Firebaseのキーが必要なためJitPackではテストしない")
    fun testRoot() = testApplication {
        application {
            install(Authentication){
                configureFirebaseAuth()
            }
            configureFirebaseAuthRoute()
        }

        println(getFirebaseAccessToken())

        client.get("/").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("Hello, World!", bodyAsText())
        }
    }

    @Test
    @Disabled("Firebaseのキーが必要なためJitPackではテストしない")
    fun testFirebaseWithoutToken() = testApplication {
        application {
            install(Authentication){
                configureFirebaseAuth()
            }
            configureFirebaseAuthRoute()
        }

        client.get("/fa_jwt").apply {
            assertEquals(HttpStatusCode.Unauthorized, status)
            assertEquals("token is not provided", bodyAsText())
        }
    }

    @Test
    @Disabled("Firebaseのキーが必要なためJitPackではテストしない")
    fun testFirebaseWithJwt() = testApplication {
        application {
            install(Authentication){
                configureFirebaseAuth()
            }
            configureFirebaseAuthRoute()
        }

        val token = getFirebaseAccessToken()

        val c = createClient {
            install(DefaultRequest){
                header("Authorization", "JWT $token")
            }
        }

        c.get("/fa_jwt").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("firebase jwt", bodyAsText())
        }
    }


    @Test
    @Disabled("Firebaseのキーが必要なためJitPackではテストしない")
    fun testFirebaseWithMissingKeyword() = testApplication {
        application {
            install(Authentication){
                configureFirebaseAuth()
            }
            configureFirebaseAuthRoute()
        }

        val token = getFirebaseAccessToken()

        val c = createClient {
            install(DefaultRequest){
                header("Authorization", "$token")
            }
        }

        c.get("/fa_jwt").apply {
            assertEquals(HttpStatusCode.Unauthorized, status)
            assertEquals("missing token", bodyAsText())
        }

        c.get("/fa_bearer").apply {
            assertEquals(HttpStatusCode.Unauthorized, status)
            assertEquals("missing token", bodyAsText())
        }
    }

    @Test
    @Disabled("Firebaseのキーが必要なためJitPackではテストしない")
    fun testFirebaseWithBearer() = testApplication {
        application {
            install(Authentication){
                configureFirebaseAuth()
            }
            configureFirebaseAuthRoute()
        }

        val token = getFirebaseAccessToken()

        val c = createClient {
            install(DefaultRequest){
                header("Authorization", "Bearer $token")
            }
        }

        c.get("/fa_bearer").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("firebase bearer", bodyAsText())
        }
    }
}