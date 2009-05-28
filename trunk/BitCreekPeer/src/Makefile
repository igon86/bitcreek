
.PHONY: all prova clean

all:
	javac -Xlint ./condivisi/*.java
	javac -Xlint ./gui/*.java
	javac -Xlint ./peer/*.java


prova:
	java -Djavax.net.ssl.trustStore=mySrvKeystore -Djavax.net.ssl.trustStorePassword=123456 gui.BitCreekGui


clean:
	-rm -f ./condivisi/*.class ./condivisi/*~
	-rm -f ./gui/*.class ./gui/*~
	-rm -f ./peer/*.class ./peer/*~
	-rm -f ./FileCondivisi/*
	-rm -f ./MetaInfo/*
	-rm -f ./avviato.on
