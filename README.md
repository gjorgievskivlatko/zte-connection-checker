ZTE MF971L 4G router connection checker

ZTE MF971L 4G router has some issues that it gets stuck and the connection is not reestablished.
This apllication essentially runs a Thread which checks if there is a connection to the internet (pings google.com for now)
and runs few commands to restart the router if the connection to the internet can not be established.

Java Swing is used for the UI but there is an option to run it like a cli as well.
Example usage as cli:
```sh
java -jar zte-connection-checker-1.0.0.jar -cli -interval 10000 -domain 192.168.1.1 -password <password>
```
