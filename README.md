# Payments (Money transfer) Service
Sample service to build REST APIs using JAX-RS. Implemented simple payment service to transfer amount from one account to another account.

## Code description
* Application.java: Starts Jetty service and initializes app
* config/AppBinder.java: Configures implementations for dependency injection
* controller/PaymentController.java: Defines interface to transfer amount from one account to another
* controller/BasicPaymentController.java: Basic (simple) implementation of PaymentController
* domain: Defines models/domains used to represent Account & InsufficientAmountException
* persistence: Defines interfaces to perform CRUD operations on Accounts store/database
* persistenance/memory: Defines simple InMemory Accounts store and its repository (DAO)
* service: Defines Accounts & Payments services/REST endpoints & their DTO


## Technologies / Frameworks
* JAX - RS
* Jersey
* Jetty
* Jackson

## Model
* An account is represented and uniquely identified by an account number of type `long`

## APIs
### 1-Get Account details:
    ```
        HTTP Method: GET
        PATH:   /v1/accounts/{account_number}
        Response:
            {
                "accountNumber": <account number>,
                "balance": <account balance>
            }
    ```
  
### 2-Transfer money from one account (source) to another account (target):
      ```
          HTTP Method: POST
          PATH:   v1/payments/transfer
          Request Body:
              {
              	"sourceAccountNumber": <source account number>,     <- long
              	"targetAccountNumber": <target account number>,     <- long
              	"amount": <amount to transfer>                      <- double
              }
              
          Response:
              {
                    "sourceAccountNumber": <source account number>,     <- long
                    "targetAccountNumber": <target account number>,     <- long
                    "amount": <transferred amount>,                     <- double
                    "transactionId": "<transaction id>",                <- type-4 uuid
                    "timestamp": "<timestamp>"                          <- date
              }
      ```

## Build
* `./gradlew clean build` will generate jar file (fat jar - with dependencies) in build/libs named `payments-1.0.0.jar`
* OR
* Project can be imported in java IDE like IntelliJ, and build from IDE tools

## Running
* Running on a specific port: `java -Dserver.port=<port> -jar /build/lib/payments-1.0.0.jar`. If `serve.port` is not set, then API server runs at default port `8080`
* OR Project can be imported in java IDE like IntelliJ, and run from IDE using main class `Application.java`

## Testing
* Internally, we have initialized few test accounts with following balance"
    ```
       AccountNumber:	123,			Balance: 100.0
       AccountNumber:	124,			Balance: 100.0
       AccountNumber:	125,			Balance: 100.0
       AccountNumber:	1234,			Balance: 200.0
       AccountNumber:	1235,			Balance: 200.0
       AccountNumber:	1236,			Balance: 200.0
       AccountNumber:	12345,			Balance: 300.0
       AccountNumber:	12346,			Balance: 300.0
       AccountNumber:	123456,			Balance: 400.0
       AccountNumber:	123457,			Balance: 400.0
  ```
  These accounts can be used for testing given APIs
  
* Money Transfer
    ```
    curl -X POST \
         http://localhost:8080/v1/payments/transfer \
         -H 'Content-Type: application/json' \
         -d '{
       	"sourceAccountNumber": 12345,
       	"targetAccountNumber": 1234,
       	"amount": 50
       }'
  
  
    Response:
        {
            "sourceAccountNumber": 12345,
            "targetAccountNumber": 1234,
            "amount": 50.0,
            "transactionId": "ce4eb55b-a7f2-409b-a0af-da39eea065da",
            "timestamp": "2019-08-05 06:07:58"
        }
  ```
  
* Get Account details:
    ```
        curl -X GET http://localhost:9080/v1/accounts/12345
  
        Response:
            {
                "accountNumber": 12345,
                "balance": 300.0
            }
   ```
  


