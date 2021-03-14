cryptoinv
=========

Spring REST application for handling bitcoin transactions.

Purpose
-------
This project is reusing some of the code from gezero/sheriff springboot project. It is hardwired to work against TestNet.
It helps me to start with crypto currency learning.


What can be done
----------------
So far it is creating a pair of source and target P2SH addresses first which can include a invoice balance, and a due date (which defaulted to 5 days if not specified).
It then accepts update with the amount toward refreshing invoice balance as new transactions come in.
When a transaction is sent the invoice balance can be updated to PARTIALLY_PAID or PAID if fully paid. Regardless of the
amount paid, if the due date has past the invoice status will be set as EXPIRED.
It does not validate anything as I run out of time.

Building
========
You need BitcoinJ, get the newest version from the [BitcoinJ github sources](https://github.com/bitcoinj/bitcoinj). Install them in local repo. 

Running the project
===================
If you successfully build the project you can run it using `java -jar build/libs/cryptoinv.jar`.

Usage
=====
Create multisignature  address
------------------------------
Suppose you will create 2 keys yourself like this ones:
Example Key1:

* public: 02c8064852f9e5d30e10997cdc4da43238016eeefbc20b439a8eb136546992786c
* private: 6fb904346234af150ad3b8dfecdc09a8d95497aaa183bcd629fca363fcd22edb

Example Key2

* public: 034d6c013aa68cd2e0a7c247eef627586481c35eb00c7f2e1698d6a054059e0b52
* private: f66641a724879c43444a6ea8aab5da9fd58d5cbffdbf22bbc0ceefc2626cb0c9

Create source  address
------------------------------
You can then send request for a new source address by sending POST request on to the running server on the /rest/addresses url
with the Json content similar to this one:

    {
        "totalKeys":3,
        "requiredKeys":2,
        "keys":["02c8064852f9e5d30e10997cdc4da43238016eeefbc20b439a8eb136546992786c","034d6c013aa68cd2e0a7c247eef627586481c35eb00c7f2e1698d6a054059e0b52"]"]
    }
    
The request should succeed and you should get back response similar to this one, and the invoice due date is default to 5 days later
if not specified above:

    {
       "address": "2N5BBYPsosqz6r5kR92uRrwXTizbmyKgrBM",
       "redeemScript": "522102c8064852f9e5d30e10997cdc4da43238016eeefbc20b439a8eb136546992786c21034d6c013aa68cd2e0a7c247eef627586481c35eb00c7f2e1698d6a054059e0b5221031a97e0b9bad3db3fe8fbe33629c16fa15cceaa8e728acad6c6939fa0e52d8ac653ae",
       "totalKeys": 3,
       "requiredKeys": 2,
       "invoiceBalance": 0,
       "dueDate": "2021-03-18T07:00:00.000+00:00",
       "invoiceStatus": "UNPAID",
       "keys": [
           "02c8064852f9e5d30e10997cdc4da43238016eeefbc20b439a8eb136546992786c",
           "034d6c013aa68cd2e0a7c247eef627586481c35eb00c7f2e1698d6a054059e0b52",
           "031a97e0b9bad3db3fe8fbe33629c16fa15cceaa8e728acad6c6939fa0e52d8ac6"
       ],
       "_links": {
           "self": {
               "href": "http://localhost:8080/rest/addresses/2N5BBYPsosqz6r5kR92uRrwXTizbmyKgrBM"
           }
       } 
    }


Create target address
------------------------------
You can then send request for a new target address by sending POST request on to the running server on the /rest/addresses url
with the Json content similar to this one (Note that the due date is in 'uuuu-MM-dd' format):

    {
        "totalKeys":3,
        "requiredKeys":2,
        "invoiceBalance":50000,
        "dueDate":"2021-03-30",
        "keys":["02c8064852f9e5d30e10997cdc4da43238016eeefbc20b439a8eb136546992786c","034d6c013aa68cd2e0a7c247eef627586481c35eb00c7f2e1698d6a054059e0b52"]"]
    }

The request should succeed and you should get back response similar to this one, and the invoice due date is default to 5 days later
if not specified above, and the invoice status will be initialized to UNPAID:


    {
       address": "2NBMrG4VaHbdhnjUgqkW1qri5pUCD6x4s5R",
       "redeemScript": "522102c8064852f9e5d30e10997cdc4da43238016eeefbc20b439a8eb136546992786c21034d6c013aa68cd2e0a7c247eef627586481c35eb00c7f2e1698d6a054059e0b522102fadcdc1990ca16a7a86a564c01cb80447cbe7fdf036db017d78c34a4d5c5646053ae",
       "totalKeys": 3,
       "requiredKeys": 2,
       "invoiceBalance": 50000,
       "dueDate": "2021-03-30T00:00:00.000+00:00",
       "invoiceStatus": "UNPAID",
       "keys": [
           "02c8064852f9e5d30e10997cdc4da43238016eeefbc20b439a8eb136546992786c",
           "034d6c013aa68cd2e0a7c247eef627586481c35eb00c7f2e1698d6a054059e0b52",
           "02fadcdc1990ca16a7a86a564c01cb80447cbe7fdf036db017d78c34a4d5c56460"
       ],
       "_links": {
           "self": {
               "href": "http://localhost:8080/rest/addresses/2NBMrG4VaHbdhnjUgqkW1qri5pUCD6x4s5R"
           }
       }
    }

Here you can see the newly created address as well as the server public key. You should be now able to send GET request to the link provided to get new status of the address.  
You can now send transaction to the target address. And see that the GET request will update you with the new invoice balance.

Creating transaction
--------------------
Ycan send request for creating new transaction from the address. You want to send
POST request to the URL /rest/transactions with the content similar to this one:

    {
        "targetAddress":"2NBMrG4VaHbdhnjUgqkW1qri5pUCD6x4s5R",
        "amount":10000,
        "sourceAddress":"2N5BBYPsosqz6r5kR92uRrwXTizbmyKgrBM"
    }
Here is the sourceAddress the address that you created in previeous requests.
 
 This should give you response of the following form:
 
    {
        "targetAddress": "2NBMrG4VaHbdhnjUgqkW1qri5pUCD6x4s5R",
        "amount": 10000,
        "sourceAddress": "2N5BBYPsosqz6r5kR92uRrwXTizbmyKgrBM",
        "rawTransaction": "010000000001102700000000000017a914c6b309c9c6a2f28d21765d7209202ba7c92d379b8700000000",
        "_links": {
            "self": {
                "href": "http://localhost:8080/rest/transactions/1"
            }
    }
}
    
Checking the invoice balance
----------------------------
You now can review the invoice balance update with the rest/address/{address id} call:
e.g: call a GET on /rest/transactions/2NBMrG4VaHbdhnjUgqkW1qri5pUCD6x4s5R, you will get:

      {
         "address": "2NBMrG4VaHbdhnjUgqkW1qri5pUCD6x4s5R",
         "redeemScript": "522102c8064852f9e5d30e10997cdc4da43238016eeefbc20b439a8eb136546992786c21034d6c013aa68cd2e0a7c247eef627586481c35eb00c7f2e1698d6a054059e0b522102fadcdc1990ca16a7a86a564c01cb80447cbe7fdf036db017d78c34a4d5c5646053ae",
         "totalKeys": 3,
         "requiredKeys": 2,
         "invoiceBalance": 40000,
         "dueDate": "2021-03-30T00:00:00.000+00:00",
         "invoiceStatus": “PARTIALLY_PAID”,
         "keys": [
            "02c8064852f9e5d30e10997cdc4da43238016eeefbc20b439a8eb136546992786c",
            "034d6c013aa68cd2e0a7c247eef627586481c35eb00c7f2e1698d6a054059e0b52",
            "02fadcdc1990ca16a7a86a564c01cb80447cbe7fdf036db017d78c34a4d5c56460"
           ],
           "_links": {
              "self": {
              "href": "http://localhost:8080/rest/addresses/2NBMrG4VaHbdhnjUgqkW1qri5pUCD6x4s5R"
           }
        }
     }

That is basically what is supported so far.

Swagger 2 Integration
---------------------

The documentation can be generated thru Swagger 2 by:

Generate the json from: http://localhost:8080/v2/api-docs.

View the Swagger UI via: http://localhost:8080/swagger-ui.html#/

