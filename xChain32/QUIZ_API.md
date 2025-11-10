# CargoSync API Quiz

This quiz covers controllers and related Spring concepts used in the CargoSync project. Each question is multiple choice (a–d). Try to answer without looking up the answers, then check the answer key in `QUIZ_API_ANSWERS.md`.

---

1. What does `@ControllerAdvice` do in a Spring application?
   a) Declares a class as a controller that handles HTTP requests.
   b) Configures cross-origin resource sharing for controllers.
   c) Provides global exception handling and advice (e.g., @ExceptionHandler) across controllers.
   d) Enables controller-level caching.

2. Which annotation indicates a REST controller that returns JSON by default?
   a) `@Controller`
   b) `@RestController`
   c) `@Service`
   d) `@Component`

3. Which annotation is used to map HTTP GET requests to a controller method?
   a) `@PostMapping`
   b) `@PutMapping`
   c) `@GetMapping`
   d) `@DeleteMapping`

4. Which annotation binds a method parameter to a path variable in the URL?
   a) `@RequestBody`
   b) `@RequestParam`
   c) `@PathVariable`
   d) `@ModelAttribute`

5. What is the purpose of `@RequestBody`?
   a) Bind a query parameter to a method parameter.
   b) Convert the HTTP request body JSON into a Java object and bind it to a parameter.
   c) Declare the return content type of a controller method.
   d) Map a POST endpoint.

6. Which return type is commonly used to control status codes and headers from a controller?
   a) `String`
   b) `void`
   c) `ResponseEntity<T>`
   d) `Optional<T>`

7. Where should business logic reside in a typical Spring application architecture?
   a) Controller classes
   b) Service classes annotated with `@Service`
   c) Repository interfaces
   d) DTO classes

8. What is the primary role of a Repository (`@Repository`) in Spring Data?
   a) Expose REST endpoints
   b) Map DTOs to JSON
   c) Persist and query domain entities (DB access)
   d) Handle transactions manually

9. Which annotation is used to inject a dependency by type in Spring?
   a) `@Autowired`
   b) `@GetMapping`
   c) `@Bean`
   d) `@ControllerAdvice`

10. What does `@Transactional` typically ensure?
    a) That a method runs asynchronously
    b) That database operations within the method run within a transaction and rollback on failure
    c) That the method is cached
    d) That the method is only accessible by authenticated users

11. What's the main purpose of DTOs (Data Transfer Objects) in controllers?
    a) Replace entities in the repository layer
    b) Carry data between the application and the client (shape requests/responses), decoupling persistence models
    c) Execute SQL queries
    d) Manage application configuration

12. Which HTTP status code is most appropriate when a resource is successfully created?
    a) 200 OK
    b) 201 Created
    c) 204 No Content
    d) 404 Not Found

13. When should controllers return 404 Not Found?
    a) When the input JSON is invalid
    b) When an authenticated user lacks permission
    c) When the requested resource (by ID) does not exist
    d) When server throws an exception

14. How do you validate a request body field with Java Bean Validation (e.g., not null)?
    a) Add `@Valid` on the method parameter and annotations like `@NotNull` on the DTO field
    b) Use `@Autowired` on the DTO
    c) Return `ResponseEntity.badRequest()` manually
    d) Use `@PathVariable` on the field

15. What is CORS and why might you configure it in controllers?
    a) A database pooling mechanism
    b) A network file system
    c) Cross-Origin Resource Sharing — to permit browser clients from other origins to call the API
    d) A testing framework

16. Which annotation allows mapping a controller at the class-level to a base path (e.g., `/api/products`)?
    a) `@RequestMapping`
    b) `@Entity`
    c) `@Repository`
    d) `@Configuration`

17. What is the difference between `@RequestParam` and `@PathVariable`?
    a) `@RequestParam` binds to URL query parameters; `@PathVariable` binds to path segments in the URL
    b) `@RequestParam` binds to POST bodies; `@PathVariable` binds to headers
    c) They are interchangeable
    d) `@RequestParam` is only for files

18. Where are exceptions like validation errors commonly converted into user-friendly responses?
    a) In controllers directly by try/catch
    b) In service constructors
    c) In a `@ControllerAdvice` class that has `@ExceptionHandler` methods
    d) In repositories

19. Which testing approach is best for testing a single controller's web endpoints without starting a full server?
    a) Unit tests using mock MVC (MockMvc)
    b) End-to-end tests only
    c) Manual API calls
    d) Database integration tests only

20. Which of the following is NOT a good practice for controller methods?
    a) Keep them thin and delegate business logic to services
    b) Return domain entities directly to the client without DTO mapping in large apps
    c) Validate input and handle errors centrally
    d) Use appropriate HTTP status codes

21. Why might you use `ResponseEntity.noContent()` (204) as a response?
    a) To indicate a resource was created
    b) To indicate a request has no response body but was successful (e.g., delete)
    c) To indicate an authentication error
    d) To indicate a client-side redirect

22. Which header is used to send a JWT token for authenticated requests?
    a) `X-API-KEY`
    b) `Authorization: Bearer <token>`
    c) `Cookie: sessionid` only
    d) `X-CSRF-Token`

23. What is the recommended content type for JSON request/response bodies?
    a) `text/plain`
    b) `application/json`
    c) `application/x-www-form-urlencoded`
    d) `multipart/form-data`

24. Which HTTP method is idempotent?
    a) POST
    b) PATCH
    c) PUT
    d) None of the above

25. When building an API, why is API versioning important?
    a) To support backward-incompatible changes safely and allow clients to migrate
    b) To improve database performance
    c) To reduce transaction duration
    d) To obfuscate endpoints

26. Which of these annotations is used to map a JSON property to a Java field name if they differ (Jackson)?
    a) `@Column`
    b) `@JsonProperty`
    c) `@RequestBody`
    d) `@PathVariable`

27. What is the role of `@ExceptionHandler` inside `@ControllerAdvice`?
    a) To map exceptions to HTTP responses with specific status codes and bodies
    b) To map URLs to controllers
    c) To schedule background jobs
    d) To create repository beans

28. What does HATEOAS add to REST responses?
    a) Security features
    b) Hypermedia links that describe available actions and navigation
    c) Database indexing
    d) Faster serialization

29. Which is a common way to document Spring REST APIs interactively?
    a) JUnit only
    b) Swagger / OpenAPI with Springdoc or Springfox
    c) Plain text files only
    d) FTP server

30. Which of the following should NOT be in a controller constructor injection list?
    a) Services required for handling requests
    b) Repositories used directly (prefer services instead)
    c) Simple configuration values used by the controller
    d) The entire application context object

---

Good luck! Check `QUIZ_API_ANSWERS.md` for answers and short explanations after you're done.