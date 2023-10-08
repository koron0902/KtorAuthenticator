package com.milkcocoa.info

import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class HeaderAuthProviderTest{
    @Test
    fun testRoot() = testApplication {
        application {
            install(Authentication){
                configureHeaderAuth()
            }
            configureHeaderAuthRoute()
        }
        client.get("/").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("Hello, World!", bodyAsText())
        }
    }

    @Test
    fun testMeowWithoutHeader() = testApplication {
        application {
            install(Authentication){
                configureHeaderAuth()
            }
            configureHeaderAuthRoute()
        }

        client.get("/meow").apply {
            assertEquals(HttpStatusCode.Unauthorized, status)
            assertEquals("header is not provided(meow)", bodyAsText())
        }
    }

    @Test
    fun testMeowWithIncorrectHeader() = testApplication {
        application {
            install(Authentication){
                configureHeaderAuth()
            }
            configureHeaderAuthRoute()
        }

        val c = createClient {
            install(DefaultRequest){
                header("meow", "meow?")
            }
        }

        c.get("/meow").apply {
            assertEquals(HttpStatusCode.Unauthorized, status)
            assertEquals("header value is not correct(meow)", bodyAsText())
        }
    }

    @Test
    fun testMeowWithCorrectHeader() = testApplication {
        application {
            install(Authentication){
                configureHeaderAuth()
            }
            configureHeaderAuthRoute()
        }

        val c = createClient {
            install(DefaultRequest){
                header("meow", "meow!!!!")
            }
        }

        c.get("/meow").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("meow!", bodyAsText())
        }
    }



    @Test
    fun testBowWowWithoutHeader() = testApplication {
        application {
            install(Authentication){
                configureHeaderAuth()
            }
            configureHeaderAuthRoute()
        }

        client.get("/bow").apply {
            assertEquals(HttpStatusCode.Unauthorized, status)
            assertEquals("header is not provided(bow)", bodyAsText())
        }
    }

    @Test
    fun testBowWowWithIncorrectHeader() = testApplication {
        application {
            install(Authentication){
                configureHeaderAuth()
            }
            configureHeaderAuthRoute()
        }

        val c = createClient {
            install(DefaultRequest){
                header("bow", "meow?")
            }
        }

        c.get("/bow").apply {
            assertEquals(HttpStatusCode.Unauthorized, status)
            assertEquals("header value is not correct(bow)", bodyAsText())
        }
    }

    @Test
    fun testBowWowWithCorrectHeader() = testApplication {
        application {
            install(Authentication){
                configureHeaderAuth()
            }
            configureHeaderAuthRoute()
        }

        val c = createClient {
            install(DefaultRequest){
                header("bow", "bow wow!")
            }
        }

        c.get("/bow").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("bow wow!", bodyAsText())
        }
    }

    @Test
    fun testBowWowSkip() = testApplication {
        application {
            install(Authentication){
                configureHeaderAuth()
            }
            configureHeaderAuthRoute()
        }

        val c = createClient {
            install(DefaultRequest){
                header("bow", "bow-meow")
            }
        }

        c.get("/bow").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("bow wow!", bodyAsText())
        }
    }

}