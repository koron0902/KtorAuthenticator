# Ktor Authenticator
[![](https://jitpack.io/v/koron0902/KtorAuthenticator.svg)](https://jitpack.io/#koron0902/KtorAuthenticator)  
this is a ktor plugin for more authentication schema.

1. firebase authentication
2. header authentication (like 'API_KEY', 'X_API_KEY' and more.....)

# Installation
```
repositories {
    mavenCentral()
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation("com.github.koron0902:KtorAuthenticator:0.1.6")
}
```


# Usage
## Firebase Authenticate

### Pre-Requirements
- firebase_admin.json

### Supported authentication keyword
#### JWT
when use `JWT`, request header format is `Authorization: JWT xxxxxxxxxxxx`

#### Bearer
when use `Bearer`, request header format is `Authorization: Bearer xxxxxxxxxxxx`

### Handle errors
`FirebaseAuthProvider` throws these exceptions when authentication failed.  
#### TokenNotProvidedException  
throws when request header not contains `Authorization`

#### ClaimNotFoundException
throws when malformed `Authorization` header.  
example
- `Authorization: JWTxxxxxxxxxxxxxxxxxxxxxxxx` 
- `Authorization: xxxxxxxxxxxxxxxxxx`

correct format is `Authorization: JWT xxxxxxxxxxxxxxxxxx`

#### FirebaseAuthException
firebase library's exception.  
throws on token which provided from client is not correct, expired, or user is disabled(when checkRevoked is true)

#### IllegalArgumentException
throws when FirebaseApp is not valid

### sample
setup firebase authorization with `Authentication` plugin 

``` kotlin
install(Authentication){
    firebase("firebase") { // name is optional. if not set 'FIREBASE_AUTH' is used
        firebaseApp = <your firebase instance>
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
                    call.respondText("some error occured", status = HttpStatusCode.Unauthorized)
                }
                is FirebaseAuthProvider.ClaimNotProvidedException ->{
                    call.respondText("missing token", status = HttpStatusCode.Unauthorized)
                }
            }
        }
    }
}
```
now, you can use firebase authentication.


``` kotlin
routing {
    authenticate("firebase") {
        get("/firebase"){
            call.respondText("firebase jwt")
        }
    }

    get("/") {
        call.respondText("Hello, World!")
    }
}
```



## Header Authenticate
when use this, you can use more authentication schema.  

### Validation
`validate` func called with `ApplicationCall` and `<HeaderValue>`, so you can check this.  
if value is correct you should return true, otherwise return false

### Handling errors
`HeaderAuthProvider` throws these exceptions when authentication failed.

#### HeaderNotProvidedException
throws when specified header is not passed.

#### ValidateFailedException
throws when `validate` returns false

``` kotlin
install(Authentication){
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
}
```

``` kotlin
routing {
    authenticate("meow") {
        get("/meow"){
            call.respondText("meow!")
        }
    }
    get("/") {
        call.respondText("Hello, World!")
    }
}
```