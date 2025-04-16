REAL TIME AUCTION SYSTEM 
 
This is a backend application built using scala and akka http which allows users to place bids on different auction items.
This system ensures that only the highest bid is accepted and finalizes the winner after the some fixed duration.
Using websockets for real time bid updates and webhooks for notifying the external services.

First, an item will be created, followed by the creation of a user. Next, an auction will be initiated using the existing item. 
Users will then place bids in the auction, and after a fixed period, the highest bid will be determined, marking the end of the auction.

Scala 2.13.15
Akka HTTP 10.2.10
Slick for Database interaction
PostgresSQL as database
Postman for api endpoint testing
Mockito and scalatest for unit testing

API Endpoints

->Item APIs
POST    /item      Create a new auction item

curl --location 'http://localhost:8080/item' \
--header 'Content-Type: application/json' \
--data '{
"itemId":1,
"name": "test-item",
"starting_price": 500,
"description": "test-desc"
}
'

GET     /items      Fetch all auction items

curl --location 'http://localhost:8080/items' \
--data ''

GET     /item/{itemId}   Get a specific auction item by ID

curl --location 'http://localhost:8080/item/1' \
--data ''


PUT     /item/{itemId} Update an auction item

curl --location --request PUT 'http://localhost:8080/item/1' \
--header 'Content-Type: application/json' \
--data '{
"itemId":1,
"name":"testItem",
"starting_price":400,
"description": "description"
}'

DELETE /item/{itemId}   Delete an auction item

curl --location --request DELETE 'http://localhost:8080/item/2' \
--data ''

->User APIs
POST    /user      Create a new user

curl --location 'http://localhost:8080/user' \
--header 'Content-Type: application/json' \
--data '{
"userId":1,
"name": "test-name"
}
'

GET     /users     Fetch all users

curl --location 'http://localhost:8080/users' \
--data ''

GET     /user/{userId}   Get a specific user by ID

curl --location 'http://localhost:8080/user/1' \
--data ''

PUT     /user/{userId} Update user details

curl --location --request PUT 'http://localhost:8080/user/1' \
--header 'Content-Type: application/json' \
--data '{
"userId":1,
"name":"test"
}'

DELETE /user/{userId}   Delete a user

curl --location --request DELETE 'http://localhost:8080/user/2' \
--data ''


->Bid APIs
POST    /auction/create    creates auction with bidId and itemId

curl --location 'http://localhost:8080/auction/create' \
--header 'Content-Type: application/json' \
--data '{
"bidId":1,
"itemId":1
}'

POST    /bid      Place a new bid (only higher bids are accepted)

curl --location 'http://localhost:8080/bid' \
--header 'Content-Type: application/json' \
--data '{
"bidId":1,
"itemId":1,
"userId":1,
"bidAmount":500
}'

GET     /bids     Fetch all bids

curl --location 'http://localhost:8080/bids' \
--data ''

GET     /bid/{itemId}   Get bids for a specific auction item

curl --location 'http://localhost:8080/bid/1' \
--data ''

DELETE /bid/{itemId}   Delete all bids for an item

curl --location --request DELETE 'http://localhost:8080/bid/1' \
--data ''

WebSocket implementation
WebSockets are used to broadcast real-time bid updates to all connected clients.
Endpoint: ws://localhost:8080/ws/auction
When a bid is placed, all connected clients receive a message in the format:


Webhook Integration
A webhook is triggered after 3 minutes when an auction ends.
The highest bid is declared as the winner and an HTTP POST request is sent to the webhook URL.

Running the Project
1. Setup Database
Ensure (pgAdmin) is running and update application.conf with database credentials.

2. Run these sql scripts in pgAdmin

CREATE EXTENSION IF NOT EXISTS hstore;

CREATE SCHEMA IF NOT EXISTS auctions;

CREATE TABLE IF NOT EXISTS auctions."User" (
user_id BIGSERIAL PRIMARY KEY,
name VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS auctions."Item" (
item_id BIGSERIAL PRIMARY KEY,
name VARCHAR(255) NOT NULL,
starting_price NUMERIC(10,2) NOT NULL,
description TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS auctions."Bid" (
bid_id BIGSERIAL,
user_id BIGINT NOT NULL,
item_id BIGINT NOT NULL,
bid_amount NUMERIC(10,2) NOT NULL,
bid_time TIMESTAMP DEFAULT NOW(),
CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES auctions."User"(user_id) ON DELETE CASCADE,
CONSTRAINT fk_item FOREIGN KEY (item_id) REFERENCES auctions."Item"(item_id) ON DELETE CASCADE
);

3. Open the docker and run and also pgAdmin
   docker-compose up

4. Run the Project
   sbt run

5. Access API Endpoints
Use Postman to interact with APIs.

6. Connect to WebSocket
Use a WebSocket client like wscat:
wscat -c ws://localhost:8080/ws/auction

7. Running Unit Test cases
sbt test