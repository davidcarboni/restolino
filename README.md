Restolino
----------


### What is it?

Java for the Web. A brutally opinionated, super-simple REST API framework, plus static file serving.

It's not comprehensive, won't give you the flexibility you want and doesn't really care about your special needs.

What it does do is let you work and stays out of your way, because configuring frameworks is a distraction. Java is a great language with loads of support, but it's never been efficient for Web development. This changes that.

If you're a Java purist, this is not the framework you are looking for. 

And that's it.


### Opinions

Restolino has unreasonable opinions:
 * Web applications should be APIs: data should not be wrapped in markup.
 * HTML/Javascript is youn API client: then you can add mobile apps, IoT devices, etc.
 * Templating is great: do it in Javascript, not server-side.
 * Efficient development: add classes, change interfaces, whatever, then refresh.
 * Immutable releases: single-jar artifact. To make a change to the deployed version, deploy a new version and delete the old vesion (blue-green and all that).
 * Stateless requests: when deployed, you'll probably have multiple nodes. Everything gets reinitialised after each change in development. That makes stetefulness difficult, which is a good thing.
 * Restolino will cause your design to hurt and adapt. You'll be better for it in the long run.
 * Constraints are your path to simplicity. Enjoy them.

#### How it works

The framework does less than you'd expect, and that's better:

 * Runs an embedded Jetty server with raw `Handler` classes. No Servlets, no Filters, no Context. No `web.xml`.
 * Requests that have a file extension are static files. They will be handled by a subclass of the Jetty `ResourceHandler` called `FilesHandler`.
 * Requests that do not have a file extension go to your API.
 * APIs consume and return JSON. Accept a parameter of any type, return a result of any type. Serialisation is done for you.
 * You get direct access to `HttpServletRequest` and `HttpServletResponse`.
 * If you need to return something other than Json, use `void` or return `null`.
 * Unmapped requests go to your implementation of the `NotFound` interface, or generate a 404 by default.
 * Errored requests go to your implementation of the `ServerError` interface, or generate a 500 by default.

#### Getting started

 * You can only `GET` `/`. Why would you `PUT`, `POST` or `DELETE` the root? You wouldn't. If you think you would, your design sucks. Implement the `Home` interface, which provides a single method: `get(req, res)` (or subclass `HomeRedirect`).
 * Put all your static files under `files` - i.e. `src/main/resources/files/...`.
 * Annotate your endpoint classes as `@Endpoint`.
 * Endpoint names are lowercased class names. More complexity would need more of your time. Get over it.
 * Annotate your methods with JAX-RS `@GET`, `@PUT`, `@POST` and `@DELETE`.
 * Method parameters must be `req, res[, request message]` (`HttpServletRequest`, `HttpServletResponse` and optionally any type you want Gson to attempt to deserialise from the request body). 
 * The return type of your method can be any type you want Gson to attempt to serialise into the response. Returns of `void` and `null` are fine, Restolino won't change your response.
 * Request and response messages are [de]serialised as JSON using Gson. If you need to add custom type adapters for serialisation, you can access the `GsonBuilder` via `Serialiser.getBuilder()`.
 * There's no context path. Why would you run more than one app in the same server process? The Jetty process is one-to-one with your app.
 * You only need one not-found handler. Implement the `NotFound` interface. It provides a single method: `handle(req, res)`. A 404 status will be pre-set for you. You can update it if you want.
 * You only need one error handler, but you do need to know where the error occurred. Implement the `ServerError` interface, which provides a single method `handle(req, res, RequestHandler, Throwable)`. A 500 status will be pre-set for you. You can update it if you want.
 * No clever (read: fiddly and time consuming) path/parameter parsing. Simple helper classes are provided instead: `Path`, `QueryString` and `Parameter`. See the `com.github.davidcarboni.restolino.helpers` package.
 * `OPTIONS` will query the configuration and tell you which of `GET`, `PUT`, `POST` and `DELETE` are implemented for that endpoint. `OPTIONS` on `/` will return GET if you have implemented `Home` or subclassed `HomeRedirect`.
 * To see the whole framework - all the interfaces and annotations you can use - have a look in the `com.github.davidcarboni.restolino.framework` package. It's intentionally small.
 * Java 1.7. If you're using anything older, try using Bing to look up SOAP. I know, that's not fair. If you're smart enough to be able to use Google, fork and build from source.
 * There are non-private fields in the classes. Like semi-colons in Javascript, the usual modifiers are visual clutter that provide too little benefit. That's my opinion. I'm not asking you to agree if you don' want to.

#### Looking under the hood

Restolino aims to be simple enough for you to understand the code. This is not intended as an academic achievement of subtle software engineering prowess. It's meant to be basic, pragmatic, helpful and uncomplicated. Sort of like the girl/boy next door you realise is a far deeper friend than cool kids you've been trying to impress.

Here is the list of packages and what they mean. There are no more than a pizza's worth of classes in each:

 * `com.github.davidcarboni.restolino.`**framework** - The stuff you need to define your API - annotations and interfaces. 
 * `com.github.davidcarboni.restolino.`**helpers** - Convenience classes you may (or may not) want to use to make your life easier.
  * `com.github.davidcarboni.restolino.`**json** - Json serialisation
 * `com.github.davidcarboni.restolino.`**api** - The classes that set up your API.
 * `com.github.davidcarboni.restolino.`**reload** - The classes that detect file changes and trigger reloading.
 * `com.github.davidcarboni.restolino.`**jetty** - The Jetty handlers that make up Restolino. `MainHandler` receives requests, decides whether a request is for an API or a file and delegates to `ApiHandler` or `FilesHandler`. There's also a handler for HTTP Basic authentication.
 * `com.github.davidcarboni.restolino` - The `Main` and `Configuration` classes used to start the server.


### Dependencies:

```xml
    <dependencies>
    
        <dependency>
            <groupId>com.github.davidcarboni</groupId>
            <artifactId>restolino</artifactId>
            <version>0.1.0</version>
        </dependency>
            
    </dependencies>
```


### Maven build

The configuration below provides both a -jar-with-dependencies (for deployment) and a folder of dependencies for reloading in development (`${project.build.directory}/dependency`). NB this should work if you want to deploy to Heroku.

This also configures your project for Java 1.7. You could do this with profiles if you want to:

```xml
	<build>
		<plugins>
			
			<!-- Needs Java 1.7. You're not still using 1.6 - are you: -->
			<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-compiler-plugin</artifactId>
				<version>3.2</version>
				<configuration>
					<source>1.7</source>
					<target>1.7</target>
					<encoding>UTF-8</encoding>
				</configuration>
			</plugin>
			
			<!-- An assembly that includes all dependencies is produced for deployment: -->
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
				<version>2.9</version>
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

This is what you want, right? Minimum time-to-working. This runs your API on port 8080 with remote debug on 8000.

    #!/bin/bash
    
    # Remote debug:
    export JAVA_OPTS="-Xdebug -Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=n"
    
    # File reloading:
    export RESTOLINO_STATIC="src/main/resources/files"
    
    # Class reloading
    export RESTOLINO_CLASSES="target/classes"
    # Optional package prefix:
    # export RESTOLINO_PACKAGEPREFIX=com.mycompany.myapp
    
    # Basic authentication
    #export USERNAME=java
    #export PASSWORD=fortheweb
    # Optional: Practically speaking, any value you like that describes your app.
    #           see also: http://stackoverflow.com/questions/9311353/java-ee-security-realms
    #export REALM=niceapp
    
    mvn clean package && \

    # Development: reloadable
    java $JAVA_OPTS -Drestolino.files=$RESTOLINO_STATIC -Drestolino.classes=$RESTOLINO_CLASSES -Drestolino.packageprefix=$RESTOLINO_PACKAGEPREFIX -cp "target/dependency/*" com.github.davidcarboni.restolino.Main
    
    # Deployment: non-reloadable
    #java $JAVA_OPTS -jar target/*-jar-with-dependencies.jar


Why not reload when deployed? You want to be using containers, but as a minimum you should be designing for stateless, immutable nodes. If something needs to change in your deployment, update a the build using your flavour of continuous delivery. Changing running servers manually is a brew of risk that ends in pain.


### FAQ (Frequently Anticipated Questions)

#### Is it good enough for commercial projects?

I used Restolino to implement a significant [Alpha](https://www.gov.uk/service-manual/phases/alpha.html) project for the UK government. The focus was creating a working prototype so speed of iteration was very important. It worked. The best thing about Restolino on this project was that nobody talked about Restolino - it stayed out of the way and never became the conversation.


#### Is it fast/secure/productionised?

I don't know. What I do know is that both the server and the framework are doing much less before your code gets to see a request. That's a good indicator for performance (less layers) and security (less places for bugs to hide), so chances are good.

Restolino takes more away (Servlets, Filters, Context, etc.) than it adds. If you trust Jetty and want to make simple things freely, Restolino will probably work for you. (see also [http://stackoverflow.com/questions/16063576/lightweight-servlet-container-for-production-use](http://stackoverflow.com/questions/16063576/lightweight-servlet-container-for-production-use))


#### Can I switch to Jersey for production?

Yes. Restolino intentionally doesn't stray too far from JAX-RS. If you add `@Path(<classname>)` to your classes and `@Context` to your `HttpServletRequest` and `HttpServletResponse` method parameters, your code should slot straight in to a Jersey application.


#### Why not use interfaces to define request handlers?

I've tried, but it adds complexity, doesn't add value and makes life harder. Why? Because the interface has to take into account that there may or may not be a request message type (so generic varargs would be needed - ever tried that?) and you can return any (or no) response message type (so the retun type is variable). That means either an altogether too generic interface (everything is has to be Object, even if you'd prefer void) or individual interfaces for every permutation. 

Using reflection to search for methods with either two or three parameters, where the first two are `HttpServletRequest` and `HttpServletResponse` turns out to be simpler, cleaner and more easily understood. Like I said, if you're a purist this isn't the framework you're looking for. Try it if you like - if you can come up with an elegant, pragmatic and developer-friendly solution, send me a pull request.


#### Is it really this cool?

I was able to create a project from scratch and get to a running API in 10 minutes by cutting and pasting the above snippets. For me that's a lot faster than configuring Spring or even Jersey, not to mention the time I've spent debugging config.

Honestly, I originally build this in a day so don't expect a miracles. I hope it gives you a boost with getting stuff done rather than learning to love a fancy framework. Class reloading took longer and I ditched Servlets for embedded Jetty with raw Handlers along the way. 

I'm still aced that I can edit code, adding new fields, methods, annotations and even classes, and it's up and running when I refresh the browser. After years of productivity-sapping build-redeploy cycles, there's something utterly delicious and deeply freeing about it.

The code isn't perfect, but it should just work - and let you work. Let me know if you like it.

