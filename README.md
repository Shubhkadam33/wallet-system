# Wallet System with Idempotency & Concurrency

A secure REST API built using Java, Spring Boot, Spring Security, JWT authentication, Spring Data JPA, and an H2 database.

The application supports user registration, login, wallet balance management, adding money, transferring money between users, idempotency, and transaction history.

## Features

- User registration and login
- JWT-based authentication
- Role-based authorization for users and administrators
- Automatic wallet creation for registered users
- Add money to a wallet
- Transfer money between users
- Idempotency keys to prevent duplicate transaction processing
- Database locking to help protect wallet balances during concurrent operations
- Transaction history with pagination
- Request validation and consistent JSON error responses
- H2 database persistence
- Automated unit and integration tests
- JaCoCo test coverage reporting

## Technology Stack

- **Language:** Java
- **Framework:** Spring Boot
- **Security:** Spring Security and JWT
- **Database:** H2
- **Persistence:** Spring Data JPA and Hibernate
- **Validation:** Jakarta Bean Validation
- **Testing:** JUnit and Mockito
- **Coverage:** JaCoCo
- **Build Tool:** Maven
- **API Testing:** Postman

## Prerequisites

Install the following before running the application:

- JDK 17 or a compatible supported Java version
- Eclipse IDE or another Java IDE
- Maven
- Postman (for API testing)

## Running the Application

1. Clone or download this repository.
2. Import the project into Eclipse as an existing Maven project.
3. Wait for Maven dependencies to download.
4. Verify the database and JWT configuration in `src/main/resources/application.properties`.
5. Run the main Spring Boot application class.
6. The API will be available at:

   `http://localhost:8080`

## API Endpoints

| Method | Endpoint | Description | Access |
|---|---|---|---|
| POST | `/auth/register` | Register a user | Public |
| POST | `/auth/login` | Authenticate and receive a JWT | Public |
| GET | `/wallet` | View the authenticated user's wallet | Authenticated |
| POST | `/wallet/add` | Add money to the wallet | Authenticated |
| POST | `/wallet/transfer` | Transfer money to another user | Authenticated |
| GET | `/wallet/transactions` | View paginated transaction history | Authenticated |
| GET | `/admin/wallets` | View wallet information | Admin |
| GET | `/admin/transactions` | View transaction information | Admin |

## Authentication

Register a user and log in to obtain a JWT token.

For protected endpoints, add the following header:

`Authorization: Bearer <your-jwt-token>`

The authenticated user's identity is obtained from the security context rather than trusting a client-supplied wallet owner ID.

## Adding Money

Send a POST request to `/wallet/add`.

Required header:

`Idempotency-Key: <unique-request-key>`

Example request body:

```json
{
  "amount": 10
}
```

Use a unique idempotency key for each new operation. Reusing the same key with the same operation allows the application to return the previously processed transaction instead of processing it again.

## Transferring Money

Send a POST request to `/wallet/transfer`.

Required header:

`Idempotency-Key: <unique-request-key>`

Example request body:

```json
{
  "toEmail": "recipient@example.com",
  "amount": 20
}
```

The transfer operation validates the request and available balance and updates the sender's and recipient's wallets within a transaction.

## Transaction History and Pagination

Use the endpoint:

`GET /wallet/transactions?page=0&size=10`

- `page` starts at `0`.
- `size` specifies the number of records per page.
- The supported page size is between 1 and 100.

The response includes transaction records and pagination metadata.

## Concurrency and Idempotency

Idempotency keys are stored in the database with a uniqueness constraint to help prevent duplicate processing. Wallet locking and transactional updates help protect balances during simultaneous wallet operations.

A repeated request using the same idempotency key should not create a second successful transaction for the same operation.

## Running Tests

In Eclipse:

1. Right-click the project.
2. Select **Run As → Maven test** or run the Maven `verify` lifecycle.
3. Review the test results in the Console.

To generate the JaCoCo report, run:

`mvn clean verify`

The HTML coverage report is normally generated at:

`target/site/jacoco/index.html`

Open that file in a browser to inspect the coverage results.

## Error Handling

The application returns JSON error responses for supported validation, authentication, and request-format errors.

Example:

```json
{
  "message": "Page number cannot be negative"
}
```

## Database

The application uses H2 with configuration defined in `application.properties`. Check your local configuration for the database URL and H2 console settings.

## Future Improvements

- Add more integration tests for concurrent requests and database transaction rollback.
- Add API documentation using OpenAPI/Swagger.
- Integrate SonarQube for static code analysis.
- Deploy the application to a cloud platform.

## Author

Developed as a Java backend wallet system project.
