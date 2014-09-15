Restolino
----------


### What is it?

Java for the Web. A brutally opinionated, super-simple REST API framework, plus static file serving.

It's not comprehensive, won't give you the flexibility you want and doesn't really care about your special needs.

What it does do is let you work and stays out of your way, because configuring frameworks is a distraction. Java is a great language with loads of support, but it's never been efficient for Web development. This changes that.

If you're a purist, this is not the framework you are looking for. 

And that's it.


### Opinions

Restolino has unreasonable opinions:
 * Web applications should be APIs: data should not be wrapped in markup.
 * HTML/Javascript is youn API client: then you can add mobile apps, IoT devices, etc.
 * Templating is great: do it in Javascript, not server-side.
 * Efficient development: add classes, change interfaces, whatever, then refresh.
 * Immutable releases: single-jar artifact. To make a change in production, deploy a new build and delete the old one.
 * Stateless requests: production will probably have multiple nodes. Everything gets reinitialised after each development change.
 * Constraints are your path to simplicity. Enjoy them.

#### How it works

The framework does less than you'd expect, and that's better:

 * Runs an embedded Jetty server with raw `Handler` classes. No Servlets, no Filters, no Context. No `web.xml`.
 * Requests that have a file extension are static files. They will be handled by a Jetty `ResourceHandler` subclass (`FilesHandler`).
 * Requests that do not have a file extension are API endpoints.
 * APIs consume and return JSON. You get direct access to `HttpServletRequest` and `HttpServletResponse`.
 * Unmapped requests go to your `NotFound` implementation, or generate a 404 by default.
 * Errored requests go to your `Boom` implementation, or generate a 500 by default.

#### Getting started

 * You can only `GET` `/`. Why would you `PUT`, `POST` or `DELETE` the root? You wouldn't. If you think you would, your design sucks. Implement the `Home` interface, which provides a single method: `get(req, res)` (or subclass `HomeRedirect`).
 * Put all your static files under `files` - i.e. `src/main/resources/files/...`.
 * Annotate your endpoint classes as `@Endpoint`.
 * Endpoint names are lowercased class names. More complexity would need more of your time. Get over it.
 * Annotate your methods with JAX-RS `@GET`, `@PUT`, `@POST` and `@DELETE`.
 * Method parameters must be `req, res[, request message]`. 
 * The return type of your method can be any type, or void. If you return null, that's OK.
 * Request and response messages are assumed to be JSON and processed by Gson. If you need to add custom type adapters for serialisation, you can access the GsonBuilder via `Serialiser.getBuilder()`.
 * There's no context path. Why would you run more than one webapp in the same container? The container is the webapp.
 * You only need one 404 handler. Implement the `NotFound` interface, which provides a single method: `handle(req, res)`.
 * You only need one error handler, but you do need to know where the error occurred. Implement the `Boom` interface, which provides a single method `handle(req, res, RequestHandler, Throwable)`. A 500 status will be pre-set for you. You can update it if you want.
 * No clever (aka fiddly and time consuming) path/parameter parsing. Simple helper classes are provided instead: `Path`, `QueryString` and `Parameter`. See the `com.github.davidcarboni.restolino.helpers` package.
 * `OPTIONS` will query the configuration and tell you which of `GET`, `PUT`, `POST` and `DELETE` are implemented for that endpoint. `OPTIONS` on `/` will return GET if you have implemented `Home` or subclassed `HomeRedirect`.
 * To see the whole framework - all the interfaces and annotations you can use - have a look in the `com.github.davidcarboni.restolino.framework` package. It's intentionally small.
 * Java 1.7. If you're using anything older, try using Bing to look up SOAP. I know, that's not fair. If you're smart enough to be able to use Google, fork and build from source.
 * There are non-private fields in the classes. I consider excess modifiers to be visual clutter for little benefit. Like semi-colons in Javascript.


### Dependencies:

```xml
    <dependencies>
    
        <dependency>
            <groupId>com.github.davidcarboni</groupId>
            <artifactId>restolino</artifactId>
            <version>0.0.9</version>
        </dependency>
    
        <!-- You'll probably want the Servlet API: -->
        <dependency>
            <groupId>javax.servlet</groupId>
            <artifactId>javax.servlet-api</artifactId>
            <version>3.1.0</version>
        </dependency>
            
    </dependencies>
```


### Maven build

The configuration below provides both a -jar-with-dependencies (for production) and a folder of dependencies for reloading in development (`${project.build.directory}/dependency`). NB this should work if you want to deploy to Heroku.

This also configures your project for Java 1.7. NB you could do this more neatly with profiles if you want to:

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
    
    # Remote debug:
    export JAVA_OPTS="-Xdebug -Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=n"
    
    # File reloading:
    export RESTOLINO_STATIC="src/main/resources/files"
    
    # Class reloading
    export RESTOLINO_CLASSES="target/classes"
    # Optional package prefix:
    # export RESTOLINO_PACKAGEPREFIX=com.mycompany.myapp
    
    mvn clean package && \

    # Development: reloadable
    java $JAVA_OPTS -Drestolino.files=$RESTOLINO_STATIC -Drestolino.classes=$RESTOLINO_CLASSES -Drestolino.packageprefix=$RESTOLINO_PACKAGEPREFIX -cp "target/dependency/*" com.github.davidcarboni.restolino.Main
    
    # Production: non-reloadable
    #java $JAVA_OPTS -jar target/*-jar-with-dependencies.jar


Why not reload in production? Simple: you want to be using containers, but as a minimum you should be designing for stateless, immutable nodes. If something needs to change in production, deploy a new build using your flavour of continuous delivery.


### FAQ (Frequently Anticipated Questions)

#### Why not use interfaces for request handlers?

I've tried, but it adds complexity, doesn't add value and makes things harder. Why? Because there may or may not be a request message type and you can return any (or no) response message type. That means an interface would need to define generic types for a vararges parameter (ever tried that?) and return type, or make them Object, which is altogether rather too generic.

Searching for methods with either two or three parameters, where the first two are `HttpServletRequest` and `HttpServletResponse` turns out to be simpler, cleaner and more easily understood. Like I said, if you're a purist this isn't the framework you're looking for. Try it if you like - if you can come up with an elegant, pragmatic and developer-friendly solution then send me a pull request.


#### Is it really that cool?

I was able to create a project from scratch and get to a running api in 10 minutes by cutting and pasting the above snippets. For me that's a lot faster than configuring Spring or even Jersey, not to mention the opaque confusion of debugging config.

Honestly, I originally build this in a day so don't expect a miracles. I hope it gives you a boost with getting stuff done rather than learning to love a fancy framework.

Class reloading took longer and I ditched Servlets for embedded Jetty and raw Handlers along the way. There's something delicious about being able to add new methods, or change annotations, refrest the browser and see your changes straight away.

The code isn't pretty, but it should just (about) work. Let me know if you like it.

