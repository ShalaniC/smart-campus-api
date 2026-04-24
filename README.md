# Smart Campus Sensor & Room Management API

A RESTful API built with **JAX-RS (Jersey 3.1.3)** and deployed on **Apache Tomcat 10.1** for managing campus rooms and IoT sensors.

---

## Project Structure

```
smart-campus-api/
├── pom.xml
├── nb-configuration.xml
├── README.md
├── src/
│   └── main/
│       ├── java/com/smartcampus/
│       │   ├── application/
│       │   │   ├── SmartCampusApplication.java   ← JAX-RS app config
│       │   │   └── DataStore.java                ← Singleton in-memory store
│       │   ├── model/
│       │   │   ├── Room.java
│       │   │   ├── Sensor.java
│       │   │   ├── SensorReading.java
│       │   │   └── ErrorResponse.java
│       │   ├── resource/
│       │   │   ├── DiscoveryResource.java         ← GET /api/v1
│       │   │   ├── RoomResource.java              ← /api/v1/rooms
│       │   │   ├── SensorResource.java            ← /api/v1/sensors
│       │   │   └── SensorReadingResource.java     ← Sub-resource for readings
│       │   ├── exception/
│       │   │   ├── RoomNotEmptyException.java
│       │   │   ├── RoomNotEmptyExceptionMapper.java
│       │   │   ├── LinkedResourceNotFoundException.java
│       │   │   ├── LinkedResourceNotFoundExceptionMapper.java
│       │   │   ├── SensorUnavailableException.java
│       │   │   ├── SensorUnavailableExceptionMapper.java
│       │   │   └── GlobalExceptionMapper.java
│       │   └── filter/
│       │       └── LoggingFilter.java
│       └── webapp/WEB-INF/
│           └── web.xml
└── target/
    └── smart-campus-api.war   ← built WAR file
```

---

## API Overview

This API provides a RESTful interface for managing Smart Campus infrastructure including rooms and IoT sensors. It follows REST architectural principles with proper HTTP status codes, JSON responses, and a logical resource hierarchy.

### Base URL

```
http://localhost:8080/smart-campus-api/api/v1
```

### All Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/v1 | Discovery - API metadata and links |
| GET | /api/v1/rooms | List all rooms |
| POST | /api/v1/rooms | Create a new room |
| GET | /api/v1/rooms/{roomId} | Get a specific room |
| DELETE | /api/v1/rooms/{roomId} | Delete a room |
| GET | /api/v1/sensors | List all sensors (optional ?type= filter) |
| POST | /api/v1/sensors | Register a new sensor |
| GET | /api/v1/sensors/{sensorId} | Get a specific sensor |
| DELETE | /api/v1/sensors/{sensorId} | Delete a sensor |
| GET | /api/v1/sensors/{sensorId}/readings | Get reading history |
| POST | /api/v1/sensors/{sensorId}/readings | Add a new reading |
| GET | /api/v1/sensors/{sensorId}/readings/{readingId} | Get a specific reading |

---

## Build & Run Instructions

### Prerequisites

- Java 17
- Maven 3.9+
- Apache Tomcat 10.1

### Step 1: Build the WAR file

```bash
mvn clean package
```

### Step 2: Deploy to Tomcat

Copy the generated WAR file to Tomcat's webapps folder:

```bash
copy target\smart-campus-api.war C:\apache-tomcat-10.1.xx\webapps\
```

### Step 3: Start Tomcat

```bash
set JAVA_HOME=C:\Program Files\Java\jdk-17
set CATALINA_HOME=C:\apache-tomcat-10.1.xx
set CATALINA_BASE=C:\apache-tomcat-10.1.xx
C:\apache-tomcat-10.1.xx\bin\startup.bat
```

### Step 4: Verify

Open browser and go to:

```
http://localhost:8080/smart-campus-api/api/v1
```

---

## Sample curl Commands

### 1. Discovery Endpoint

```bash
curl http://localhost:8080/smart-campus-api/api/v1
```

### 2. Create a Room

```bash
curl -X POST http://localhost:8080/smart-campus-api/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d "{\"id\":\"LIB-301\",\"name\":\"Library Quiet Study\",\"capacity\":50}"
```

### 3. Create a Sensor

```bash
curl -X POST http://localhost:8080/smart-campus-api/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d "{\"id\":\"TEMP-001\",\"type\":\"Temperature\",\"status\":\"ACTIVE\",\"currentValue\":21.5,\"roomId\":\"LIB-301\"}"
```

### 4. Filter Sensors by Type

```bash
curl "http://localhost:8080/smart-campus-api/api/v1/sensors?type=Temperature"
```

### 5. Post a Sensor Reading

```bash
curl -X POST http://localhost:8080/smart-campus-api/api/v1/sensors/TEMP-001/readings \
  -H "Content-Type: application/json" \
  -d "{\"value\":23.7}"
```

### 6. Get All Readings for a Sensor

```bash
curl http://localhost:8080/smart-campus-api/api/v1/sensors/TEMP-001/readings
```

### 7. Try Deleting a Room With Sensors (409 Conflict)

```bash
curl -X DELETE http://localhost:8080/smart-campus-api/api/v1/rooms/LIB-301
```

### 8. Try Creating Sensor With Invalid Room (422 Error)

```bash
curl -X POST http://localhost:8080/smart-campus-api/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d "{\"id\":\"CO2-001\",\"type\":\"CO2\",\"status\":\"ACTIVE\",\"currentValue\":400,\"roomId\":\"FAKE-999\"}"
```

### 9. Try Posting Reading to MAINTENANCE Sensor (403 Error)

```bash
curl -X POST http://localhost:8080/smart-campus-api/api/v1/sensors/CO2-001/readings \
  -H "Content-Type: application/json" \
  -d "{\"value\":500}"
```

### 10. Delete a Sensor Then Delete the Empty Room

```bash
curl -X DELETE http://localhost:8080/smart-campus-api/api/v1/sensors/TEMP-001
curl -X DELETE http://localhost:8080/smart-campus-api/api/v1/rooms/LIB-301
```

---

## Conceptual Report - Answers to Questions

---

### Part 1.1 - JAX-RS Resource Class Lifecycle

For every incoming HTTP request JAX-RS generates a new instance of each Resource class by default, which is the per-request lifecycle as defined by the JAX-RS specification, meaning there is no instance-level state retained between requests.

The implications of this behavior on in-memory storage of data is quite severe. If the shared data stores (rooms HashMap or sensors list) are declared as normal class instance variables, then every time a request is received they will be re-instantiated as empty collections and all the data previously saved in them will be permanently lost.

To avoid this, shared data stores should be declared as static variables of the Resource class or contained in a separate singleton store or utility class. Since static variables are shared among all instances of the Resource class and therefore all concurrent request-handling threads, thread safety must also be taken into account. Using ConcurrentHashMap instead of HashMap, or creating synchronized blocks when accessing data either to read or write, will mitigate race conditions such as lost updates, corrupted iteration state, or stale reads due to the nature of concurrent requests.

---

### Part 1.2 - HATEOAS (Hypermedia as the Engine of Application State)

With HATEOAS (Hypermedia as the Engine of Application State), API responses are dynamically embedded with navigational links so that clients can discover and trigger actions without hard coding URLs into their code or relying on static documents outside of the application.

Some of the benefits of HATEOAS to client developers are as follows. It decouples client applications from the server — if the URL structure changes for an API, then all clients using HATEOAS will work correctly and do not need to change any code to work with it. The API is self-documenting at runtime, meaning clients can explore available operations by using hyperlinks provided in responses to their requests. HATEOAS also allows clients to know what operations are available to them based on the state of the resource they are manipulating. For example, if there are active sensors in the room from which the client is trying to delete, the response does not contain a hyperlink that points to the delete operation so that the client cannot issue an invalid request to the server.

---

### Part 2.1 - Returning IDs vs Full Room Objects in List Responses

When using an API like the one discussed here, we can return only the IDs of rooms requested in a list response. Although this approach is lightweight since it sends minimal data and can be transferred very quickly to the requesting client, it also forces the client application to send a separate API call (GET /api/v1/rooms/{id}) for every room requested in order to obtain useful data about it — commonly referred to as the N+1 query problem. In a case where there are hundreds of rooms on campus, this creates numerous additional round-trips which increases the total amount of time spent waiting for responses and places an unnecessary strain on both the network and the server.

Alternatively, returning full room objects in a list response incurs more expense per call because they contain more data. However once all of the full room objects are obtained, the client can process all of the rooms received from one API call and not have to make additional API calls. This is useful in many real-world scenarios where you may need to display a room dashboard showing the names and capacities of all of the rooms in a building along with many sensors associated with each room.

A reasonable compromise would be to return a summary representation of each of the rooms returned from the list API call. For example, the response could include only the room ID, name and capacity, while the sensorID array could be excluded. While this does keep the size of the returned payload manageable, it allows enough useful data to be delivered to the client without causing additional requests from the client for more data.

---

### Part 2.2 - Is DELETE Idempotent?

As per the HTTP specification, DELETE is idempotent, and as such, our implementation respects this semantic. In general, idempotency defines that the intended effect of executing the same request multiple times will produce the same observable state on the server as if it were executed only once.

For our implementation, the first call to DELETE /api/v1/rooms/{roomId} for an existing and sensor-free room will remove it from the data store and return a response of 204 No Content. In the event of repeating this request two or more times, the room will no longer exist in that data store, and as such, the service will return a response of 404 Not Found. Importantly, the server state will be the same after each request — the room will continue to not exist. There are no additional effects such as double-deletes or corruption that will occur due to any subsequent calls.

It is also critical to understand that idempotency refers to the effect on the server state and not to the HTTP response code. The first request returns a 204 response code while the second and third requests return a 404 response code. However, this behavior does not violate idempotency since the difference in response code is expected. Accordingly, DELETE has been classified as an idempotent method in the HTTP specification.

---

### Part 3.1 - @Consumes(MediaType.APPLICATION_JSON) and Format Mismatches

The `@Consumes(MediaType.APPLICATION_JSON)` annotation specifies that this particular service can only process requests that have the Content-Type header set as `application/json`. The JAX-RS framework enforces this requirement at the framework level, before the method body is executed and prior to processing the body of the request.

If a client submits a request with a Content-Type of `text/plain` or Content-Type of `application/xml`, JAX-RS will automatically intercept that request and respond with an HTTP 415 Unsupported Media Type response to the client without invoking the resource method. This provides a mechanism for the application to reject requests based on their content type without having to implement that logic within the business layer of the application. This helps to ensure that only validly formatted and properly constructed data will be accepted by the application at the application layer.

The net effect of this process is a predictable and standards compliant response from the server: an error code of 415 is returned to the client, which gives them a clear and actionable description of what is wrong and how to correct the issue by sending the correct Content-Type in the header of the request.

---

### Part 3.2 - @QueryParam vs Path-Based Filtering

The standard way to filter resources in a RESTful API design is to use a query parameter, for example `GET /api/v1/sensors?type=CO2`, which treats the base endpoint `/api/v1/sensors` as the canonical representation of the collection of all sensors. The query parameter refines this collection but does not create a new resource for the filtered subset.

Using a path segment such as `/api/v1/sensors/type/CO2` is semantically incorrect because a path parameter represents a specific resource (i.e. `/sensors/{sensorId}`), whereas the string `CO2` is not a resource — it is a filter. Including it as a path parameter creates an incorrect resource hierarchy and is ambiguous: does `/sensors/type/CO2` represent a sensor with an ID of `type`, or is it a filtered view of sensors based on a type of `CO2`?

In addition, query parameters are much more extensible than path segments. More than one filter can be used at a time with consistency and without impacting the URL structure, as in this example: `GET /api/v1/sensors?type=CO2&status=ACTIVE`. Implementing the equivalent using path segments would involve building either a custom routing solution or creating a large number of endpoints. For this reason, query parameters are the standard and preferred method for filtering resources in a REST API.

---

### Part 4.1 - Sub-Resource Locator Pattern Benefits

By using the Sub-Resource Locator pattern in a REST API, routing and handling logic can be distributed across focused, single-responsibility classes instead of being concentrated in one monolithic controller, thus improving the overall architecture.

Without the Sub-Resource Locator pattern, every nested path such as `/sensors/{sensorId}/readings` and `/sensors/{sensorId}/readings/{readingId}` would need to be defined within the `SensorResource` class. As an API builds up over time, the `SensorResource` class would grow to an unmanageable size and be difficult to navigate, making it hard to isolate for testing.

With this pattern, `SensorResource` has a locator method that returns an instance of a separate `SensorReadingResource` class. The locator method in `SensorResource` is annotated with `@Path("{sensorId}/readings")` and returns an instance of a dedicated `SensorReadingResource` class where all reading-related operations such as retrieve, create, update and delete are encapsulated. This means there is a clean separation of concerns as each class can focus only on operations at one level of the resource hierarchy with clearly defined responsibilities.

Another benefit of the sub-resource locator pattern is context propagation. Because the `sensorId` is provided as a constructor argument during instantiation of `SensorReadingResource`, all methods within `SensorReadingResource` automatically know which sensor they are operating on, meaning there is no need to accept and validate the `sensorId` parameter for every method call. In large production API applications with multiple layers of nesting, this pattern allows individual classes to remain small, independently testable and easy to maintain.

---

### Part 5.2 - Why HTTP 422 is More Accurate Than 404 for Missing References

A 404 Not Found indicates that the URI does not correspond to a known resource, meaning the requested API endpoint was not found on the server. Therefore, if a client sends a POST request to create a new sensor at `/api/v1/sensors` with a `roomId` that does not exist, a 404 is misleading because it suggests the URL is incorrect, even though the requested endpoint and API resource are valid.

The semantically accurate response in this case is HTTP 422 Unprocessable Entity, which conveys that the request is syntactically correct (the server can read and parse the JSON), but the semantics of the payload are not valid. Since the referenced `roomId` does not correspond to any existing resource, the request is logically unprocessable.

Developers also benefit from this distinction. A 404 indicates that developers should double check their URLs, whereas a 422 clearly indicates that the reason the request was not processed was due to the submitted data. As a result, developers are able to more quickly debug their code and have an improved developer experience, which aligns with the HTTP specification for each status code.

---

### Part 5.4 - Security Risks of Exposing Stack Traces

Providing raw Java stack traces as API responses is an extremely large security risk because they expose internal implementation details to an outside party and can be a vast help to a potential attacker when creating targeted exploits.

The greatest immediate risk associated with stack traces is technology fingerprinting. They expose the precise framework, the libraries being used, and their version numbers such as `jersey-server-3.1.3` or `jackson-databind-2.14.0`. An attacker can cross-reference the library and framework names and version information disclosed by stack traces with public CVE databases to determine if there are known vulnerabilities in the specific versions in use.

Internal path disclosure is another major concern. Many stack traces contain a fully qualified filesystem path such as `/home/deploy/smartcampus/src/main/java/...` that reveals the folder structure of the application server and provides insight into the conventions used for deployment and class organization. This leads to better targeted attacks using directory traversal or file inclusion techniques.

Business logic exposure is also a significant risk. The methods naming conventions and class hierarchies visible in a stack trace illustrate exactly how the application is constructed and which classes are responsible for validation, making it much more efficient for attackers to identify attack vectors. Furthermore, injection-based attacks become possible by revealing details such as null dereferences, parse errors, and missing keys, which allow an attacker to craft specific input to reach certain parts of the code or bypass validation.

The `GlobalExceptionMapper` in this project captures all unexpected error conditions, logs the full stack trace to a secure server-side log, and returns only a generic anonymous 500 response to the calling application, ensuring no internal information is ever exposed to external consumers.

---

### Part 5.5 - Why Filters Are Better Than Manual Logger Calls

When `Logger.info()` calls are inserted manually, it violates the DRY principle (Don't Repeat Yourself) and causes maintenance problems in the future. If there is a change in how logs are structured such as adding a correlation ID or timestamp, then every method in every resource class needs to change as well. As the API continues to grow, it becomes easy to forget to add logging to new methods, leading to gaps in observability.

By using JAX-RS filters that implement `ContainerRequestFilter` and `ContainerResponseFilter`, the framework automatically invokes these filters for each and every request and response without any need to alter any of the methods in the resource classes. All logging logic is defined once, in one place, and is guaranteed to cover all API consumers.

This approach also represents the separation of concerns concept. Resource methods should be concerned only with business logic. Infrastructure-related aspects such as logging, authentication, CORS headers, and rate-limiting should be contained within a dedicated layer. This allows both resource methods and filter logic to be simpler, independently testable, and easier to reason about. The use of filters as the standard JAX-RS approach for implementing cross-cutting functionality aligns with common industry standards for developing maintainable and production-ready APIs.
