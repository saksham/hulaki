Hulaki - A Fake SMTP Server
=========================
![Build status](https://travis-ci.org/saksham/hulaki.svg)

Hulaki (postman in Nepali) is a very simple fake SMTP server designed for unit and system testing applications that send email messages.

Following are the key features of this SMTP test double

* Understands SMTP protocol and can receive emails sent using standard frameworks such as javax.mail or Unix Mail
* All emails sent to the server can be stored irrespective of whether or not the recipient address or mailbox exists.
* Can store emails using a variety of mechanisms. All emails are indexed using the recipient address for rapid retrieval.
  * Files: stores the emails in XML files
  * In memory: stores them in memory for quick access
  * SQLite: stores them in an SQLite database 
* The SMTP server can be started within a process or in a separate machine from where the tests can send and retrieve emails.
* When the SMTP server is run in a separate process or a separate machine, the emails can be retrieved using an HTTP-like API protocol (XML over TCP)
* Provides serialization and de-serialization methods on all requests and responses for constructing API clients
* Also provides a client interface library in Java that can talk with the API server
* Can selectively relay emails sent to specific email addresses. These addresses too can be configured via the API.
* Uses [netty](http://netty.io) for dealing with networking 
* The two servers (SMTP and API) can be started on any port


The API
=======
Current version of the API supports the following actions:

* *CLEAR*
  * Deletes the emails for a given recipient.
  * Can be used without parameters to delete all saved emails.
  * For more details: see: ClearRequest.java
* *SERVER_STATUS*
  * Queries the server status.
  * Can query status of mail-processor or the SMTP server
  * For more details, see: ServerStatusRequest.java
  * Examples: SERVER_STATUS MAIL_PROCESSOR and SERVER_STATUS SMTP_SERVER
* *GET*
  * Retrieves the saved emails.
  * Supports specifying recipient address to selectively download emails.
  * For more details, see: GetRequest.java
* *COUNT*
  * Counts the saved emails.
  * Supports specifying recipient address to selectively counting emails.
  * For more details, see: CountRequest.java
* *RELAY*
  * Configures the relay behavior.
  * Can add/remove relay recipients.
  * For more details, see: RelayRequest.java
 
Using Hulaki
============
#### Running standalone Server
For more details on how to start the server, please look at ServerApplication.java. Once the server starts up, you can start sending emails to it. Furthermore, you can use tools like _netcat_ to send API commands to the API server port.
 
#### API client as a dependency
Refer to the [release notes](https://github.com/saksham/hulaki/releases) to check out the latest version of the project and how to include it as a dependency. Refer to ApiClient.java on how to use the API client library in your code. 

#### Credits
Part of this project (specifically, the SMTP state machine) is based on the original implementation of Dumbster available at: http://quintanasoft.com/dumbster/ 
