Restolino
----------


### What is it?

A brutally opinionated, super-simple REST API framework for Java.

It's not comprehensive, won't give you the flexibility you want and doesn't really care about your special needs.

What it does do is get you started and stays out of your way, because configuring frameworks is a distraction from what you are actually trying to achieve. Java is a great language with loads of support, but it's not efficient for the Web. This is my way of changing that. It's good for REST and it's also a nice quick-and-dirty static file server.

If you're a purist, this is not the framework you are looking for. 

And that's it.


### Opinions

Restolino has unreasonable opinions, but if you want to do simple stuff fast you'll find them useful.

#### Getting started

 * You can only `GET` `/`. Why would you `PUT`, `POST` or `DELETE` the root? You wouldn't. If you think you would, your design sucks. Implement the `Home` interface, which provides a single method: `get(req, res)` (or subclass `HomeRedirect`).
 * Put all your static files under `files` - i.e. `src/main/resources/files/...`.
 * Annotate your endpoint classes as `@Endpoint`.
 * Endpoint names are lowercased class names. More complexity would need more of your time. Get over it.
 * Annotate your methods with JAX-RS `@GET`, `@PUT`, `@POST` and `@DELETE`.
 * Method parameters must be `req, res[, request message]`. 
 * The return type of your method can be any type, or void. If you return null, that's OK.
 * Request and response messages are assumed to be JSON and processed by Gson. If you need to add custom type adapters for serialisation, you can access the GsonBuilder via `Serialiser.getBuilder()`.
 * Content Type gets set to `application/json` and character encoding is `UTF8` so you don't have to do that.
 * There's no context path. Why would you run more than one webapp in the same container? The container is the webapp.
 * You only need one 404 handler. Implement the `NotFound` interface, which provides a single method: `handle(req, res)`.
 * You only need one error handler, but you do need to know where the error occurred. Implement the `Boom` interface, which provides a single method `handle(req, res, RequestHandler, Throwable)`. A 500 status will be pre-set for you. You can update it if you want.
 * No clever (aka fiddly and time consuming) path/parameter parsing. Simple helper classes are provided instead: `Path`, `QueryString` and `Parameter`. See the `com.github.davidcarboni.restolino.helpers` package.
 * `OPTIONS` will query the configuration and tell you which of `GET`, `PUT`, `POST` and `DELETE` are implemented for that endpoint. `OPTIONS` on `/` will return GET if you have implemented `Home` or subclassed `HomeRedirect`.
 * To see all the interfaces and annotations you can use, have a look in the `com.github.davidcarboni.restolino.interfaces` package.
 * Java 1.7. If you're using anything older, try using Bing to look up SOAP. I know, that's not fair. If you're smart enough to be able to use Google, fork and build from source.
 * There are non-private fields in the classes. I consider excess modifiers to be visual clutter for little benefit. Like semi-colons in Javascript.

#### What's not done

 * Streaming, multipart
 * `HEAD`, `TRACE`
 * full implementation of sarcasm

#### How it works

 * Runs and embedded Jetty server, using raw `Handler` classes.
 * Any request that has a file extension is delegated to a `ResourceHandler` subclass to be served as a static file.
 * Any request that does not have a file extension is mapped to an endpoint you define.
 * Unmapped requests will go to your `NotFound` implementation, or generate a 404 by default.
 * Errored requests will go to your `Boom` implementation, or generate a 500 by default.


### Dependencies:

```xml
    <dependencies>
    
        <dependency>
            <groupId>com.github.davidcarboni</groupId>
            <artifactId>restolino</artifactId>
            <version>0.0.6</version>
        </dependency>
    
        <!-- You'll probably want the Servlet API: -->
        <dependency>
            <groupId>javax.servlet</groupId>
            <artifactId>javax.servlet-api</artifactId>
            <version>3.1.0</version>
            <scope>provided</scope>
        </dependency>
            
    </dependencies>
```


### Maven build

The configuration below provides both a -jar-with-dependencies (for production) and a folder of dependencies for reloading in development (${project.build.directory}/dependency). NB this should work if you want to deploy to Heroku.
This also configures your project for Java 1.7.

```xml
	<build>
		<plugins>
			
			<!-- Needs Java 1.7. You're not still using 1.6 - are you: -->
			<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-compiler-plugin</artifactId>
				<version>3.1</version>
				<configuration>
					<source>1.7</source>
					<target>1.7</target>
					<encoding>UTF-8</encoding>
				</configuration>
			</plugin>
			
			<!-- An assembly that includes all dependencies is produced to run in production: -->
			<plugin>
				<artifactId>maven-assembly-plugin</artifactId>
				<version>2.4.1</version>
				<configuration>
					<descriptorRefs>
						<descriptorRef>jar-with-dependencies</descriptorRef>
					</descriptorRefs>
					<archive>
						<manifest>
							<mainClass>com.github.davidcarboni.restolino.Main</mainClass>
							<addDefaultImplementationEntries>true</addDefaultImplementationEntries>
							<addDefaultSpecificationEntries>true</addDefaultSpecificationEntries>
						</manifest>
					</archive>
				</configuration>
				<executions>
					<execution>
						<id>make-assembly</id>
						<phase>package</phase>
						<goals>
							<goal>single</goal>
						</goals>
					</execution>
				</executions>
			</plugin>

			<!-- A folder of dependencies is used in development when reloading classes: -->
			<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-dependency-plugin</artifactId>
				<version>2.8</version>
				<executions>
					<execution>
						<id>copy-dependencies</id>
						<phase>package</phase>
						<goals>
							<goal>copy-dependencies</goal>
						</goals>
						<!-- Default output folder is ${project.build.directory}/dependency -->
					</execution>
				</executions>
			</plugin>

		</plugins>
	</build>
```


### Run

This is what you want, right? Minimum time-to-working. This runs your api on port 8080 with remote debug on 8000.

    #!/bin/bash
    
    export JAVA_OPTS="-Xdebug -Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=n"
    
    export RESTOLINO_STATIC="src/main/resources/files"
    export RESTOLINO_CLASSES="target/classes"
    export RESTOLINO="-Drestolino.files=$RESTOLINO_STATIC -Drestolino.classes=$RESTOLINO_CLASSES"
    
    mvn clean package && \

    # Development: reloadable
    java $JAVA_OPTS -Drestolino.files=$RESTOLINO_STATIC -Drestolino.classes=$RESTOLINO_CLASSES -cp "target/dependency/*" com.github.davidcarboni.restolino.Main
    
    # Production: non-reloadable
    #java $JAVA_OPTS -jar target/*-jar-with-dependencies.jar

Yunoreload in production? Simple: you want to be using containers, but as a minimum you should be designing for stateless, immutable nodes. If something needs to change in production, deploy a new build using your flavour of continuous delivery.


### Is it really that cool?

I was able to create a project from scratch (the hard way, thanks Eclipse) and get to a running api in 10 minutes by cutting and pasting the above snippets. For me that's a lot faster than configuring Spring or even Jersey, not to mention the confusion of debugging the config.

Honestly, I build this in a day so don't expect a miracles, but hopefully it will give you a boost with getting stuff done rather than learning to love someone else's framework.

Class reloading took quite a bit longer and I ditched the Servlet/Filter design for embedded Jetty with raw Handlers along the way, but it's worth it. There's something delicious about being able to add new methods, or change annotations, refrest the browser and see changes straight away.

The code isn't too pretty, but it should just (about) work. Let me know if you like it.

