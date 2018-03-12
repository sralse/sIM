## Simple InterMessaging

What is this?  
Well it's simple, would you like to set up a (local) chatserver? Then this might be the solution for you!  
This repository includes 2 parts, **client** and **server**. Only UTF-8 is supported. 

The server and client communicate using 128bit AES encryption over TCP. 
In the build folder you can find the latest builds of both JAR's. 
The server package can communicate with MySQL or use SQLite for data management.
The client is equipped with a console and GUI version. The server only has a console manager. 
You are able to use multiple users from 1 console and the server has some basic anti-bot/spam detection. 

Usage case of the server jar:  
`java -jar server.jar <port> -u [sql user] -p [sql password] -s [mysql port] -d [sql database]`  

If you decline to use the client in it's GUI form you can always use the console  
Usage case of the client jar:  
`java -jar client.jar`