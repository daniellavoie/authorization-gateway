# Authorization Gateway

## Why an authorization Gateway

bla bla bla

## Technologies

* Spring Cloud Gateway
* Spring Cloud Config Server
* Spring Security

## Build

```
$ ./mvnw clean package
```

## Run the Authorization Gateway

```
$ java -jar authorization-gateway/target/authorization-gateway-0.1.0-SNAPSHOT.jar --spring.profiles.active=local
```

## Run the Sample User Service

```
$ java -jar sample-services/user-service/target/authorization-gateway-samples-user-service-0.1.0-SNAPSHOT.jar
```

## Access the Sample User Service directly

```
$ curl http://localhost:8081/user
```

## Access the Sample User Service through the Authorization Gateway

```
$ curl http://localhost:8080/user
```

## Play with the routing rules

``` 
authorization:
  gateway:
     routes:
       user:
         get-user:
           method=GET
           host=localhost:8080
           path=/user
           scopes=cloud_controller.read
           redirect-host=http://localhost:8081/user
         put-user:
           method=PUT
           host=localhost:8080
           path=/user
           scopes=cloud_controller.write
           redirect-host=http://localhost:8081/user
```

 
