Dumbster Fake SMTP Server
=========================
The Dumbster is a very simple fake SMTP server designed for unit and system
testing applications that send email messages.

The following are key features of this SMTP test double
* Understands ESMTP protocol and can receive emails sent using javax.mail
* Can store emails using a variety of mechanisms
** Files: stores the emails in XML files
** In memory: stores them in memory for quick access
** SQLite: stores them in an SQLite database
* Exposes the emails to TCP clients using its own HTTP-like API protocol (XML over TCP)
* Uses [[netty] http://netty.io] for dealing with networking 
* The two servers (SMTP and API) can be started on any port

For more details on how to start the server, please look at MockServerApp.java.

## Credits
This project is based on the original implementation of Dumbster available at:
[http://quintanasoft.com/dumbster/]