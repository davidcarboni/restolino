#!/bin/bash

export JAVA_OPTS="-Xdebug -Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=y"

export RESTOLINO_STATIC="./src/main/webapp"
export RESTOLINO_CLASSES="./target/classes"
export RESTOLINO="-Drestolino.static=$RESTOLINO_STATIC -Drestolino.classes=$RESTOLINO_CLASSES"

mvn clean package && \
#java $JAVA_OPTS $RESTOLINO -cp "target/dependency/*" -jar target/*.jar
java $JAVA_OPTS $RESTOLINO -cp "target/classes:target/dependency/*" com.github.davidcarboni.restolino.Main
