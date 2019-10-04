# money-transfer

The api will be running on port 4567  
It is assumed that account ids are simply integers and the monetary value is represented as an integer.

## Endpoints
* GET /accounts - return all the available accounts  
  Sample response:
  ```json 
  [{ 
    "accountId": 10,
    "balance": 500
  }, {
    "accountId": 20,
    "balance": 200
  }]
   ``` 
* POST /accounts - creates a new account with provided balance, returning the created account  
  Sample request body:  
  ```json 
  { 
    "balance": 500
  }
  ```  
  Sample response:
  ```json 
  { 
    "accountId": 10,
    "balance": 500
  }
   ``` 
* GET /accounts/[id] - returns the account specified by the id  
  Sample response:
  ```json 
  { 
    "accountId": 10,
    "balance": 500
  }
   ``` 
* `POST` /transactions - perform a money transfer between two accounts, and returns the updated account details  
  Sample request body:  
  ```json 
  { 
    "fromAccountId": 10,
    "toAccountId": 20,
    "amount": 5
  }
  ```  
  Sample response:  
  ```json 
  [{ 
    "accountId": 10,
    "balance": 500
  }, {
    "accountId": 20,
    "balance": 200
  }]
  ``` 

## Running
To build a runnable fat jar:
```
./gradlew jar
```
The generated jar can be found at:
```
build/libs/money-transfer-1.0-SNAPSHOT.jar
```
Run with (tested with java 8):
```
java -jar money-transfer-1.0-SNAPSHOT.jar
```

## Running Tests
To run unit tests:  
```
./gradlew test
```
To run integration tests:
```
./gradlew integrationTest
```
To run functional tests:
```
./gradlew functionalTest
```