#!/bin/bash

export JAVA_OPTS="-Xdebug -Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=n"

export RESTOLINO_FILES="src/main/resources/files"
export RESTOLINO_CLASSES="target/classes"

mvn clean package && \
#java $JAVA_OPTS -Drestolino.files=$RESTOLINO_STATIC -Drestolino.classes=$RESTOLINO_CLASSES -cp "target/dependency/*" -jar target/*.jar
#java $JAVA_OPTS -Drestolino.files=$RESTOLINO_STATIC -Drestolino.classes=$RESTOLINO_CLASSES -cp "target/classes:target/dependency/*" com.github.davidcarboni.restolino.Main
java $JAVA_OPTS -Drestolino.files=$RESTOLINO_STATIC -Drestolino.classes=$RESTOLINO_CLASSES -cp "target/classes:target/dependency/*" com.github.davidcarboni.restolino.Main
