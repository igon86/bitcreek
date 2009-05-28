
.PHONY: all prova clean

all:
	javac -Xlint ./condivisi/*.java
	javac -Xlint ./server/*.java


prova:
	java -Djavax.net.ssl.keyStore=mySrvKeystore -Djavax.net.ssl.keyStorePassword=123456 server.BitCreekServer


clean:
	-rm -f ./condivisi/*.class ./condivisi/*~
	-rm -f ./server/*.class ./server/*~
	-rm -f ./MetaInfo/*
