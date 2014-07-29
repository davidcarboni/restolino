Restolino
----------


### What is it?

A brutally opinionated, super-simple REST API framework for Java.

It's not comprehensive, won't give you the flexibility you want and doesn't really care about your special needs.

What it does do is get you started and stays out of your way, because configuring frameworks is a distraction from what you are actually trying to achieve.

If you're a purist, this is not the framework you are looking for. 

And that's it.


### Opinions

Restolino has unreasonable opinions, but if you want to do simple stuff fast you'll find them useful.

#### Getting started

 * You can only `GET` `/`. Why would you `PUT`, `POST` or `DELETE` the root? You wouldn't. If you think you would, your design sucks. Implement the `Home` interface, which provides a single method: `get(req, res)`.
 * Annotate your endpoint classes as `@Endpoint`.
 * Endpoint names are lowercased class names. More complexity would need more of your time. Get over it.
 * Annotate your methods with JAX-RS `@GET`, `@PUT`, `@POST` and `@DELETE`.
 * Method parameters must be `req, res[, request message]`. 
 * The return type of your method can be any type, or void. If you return null, that's OK.
 * Request and response messages are assumed to be JSON and processed by Gson. If you need to add custom type adapters for serialisation, you can access the GsonBuilder via `Serialiser.getBuilder()`.
 * Content Type gets set to `application/json` and character encoding is `UTF8` so you don't have to do that.
 * Assumes no context path. Why would you run more than one webapp in the same container? The container is the webapp.
 * You only need one 404 handler. Implement the `NotFound` interface, which provides a single method: `handle(req, res)`.
 * You only need one error handler, but you do need to know where the error occurred. Implement the `Boom` interface, which provides a single method `handle(req, res, RequestHandler, Throwable)`. A 500 status will be pre-set for you. You can update it if you want.
 * No clever (aka fiddly and time consuming) path/parameter parsing. Simple helper classes are provided instead. See the `com.github.davidcarboni.restolino.helpers` package
 * `OPTIONS` will query the configuration and tell you which of `GET`, `PUT`, `POST` and `DELETE` are implemented for that endpoint. `OPTIONS` on `/` will return GET if you have implemented `Home`.
 * To see all the interfaces and annotations you can use, have a look in the `com.github.davidcarboni.restolino.interfaces` package.
 * Java 1.7. If you're using anything older, try using Bing to look up SOAP. I know, that's not fair. If you're smart enough to be able to use Google, fork and build from source.
 * There are non-private fields in the classes. I consider it visual clutter for little benefit. Like semi-colons in Javascript.

#### What's not done

 * Access to GSON configuration
 * Helper classes
 * Streaming, multipart
 * `HEAD`, `TRACE`
 * ContextInitialised, ContextDestroyed,
 * application context
 * helper classes
 * full implementation of biting sarcasm

#### How it works

 * Any request that has a file extension is delegated to the servlet container
 * Any request that does not have a file extension is mapped to an endpoint
 * Unmapped requests will go to your `NotFound` implementation, or generate a 404 by default.
 * Errored requests will go to your `Boom` implementation, or generate a 500 by default.


### Dependencies:

```xml
    <dependencies>
    
        <dependency>
            <groupId>com.github.davidcarboni</groupId>
            <artifactId>restolino</artifactId>
            <version>0.0.2-SNAPSHOT</version>
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

The configuration below adds Jetty. NB this will also work if you want to deploy to Heroku.
This also configures your project for Java 1.7.

```xml
<build>
    <finalName>myapi</finalName>
    <plugins>
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
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-dependency-plugin</artifactId>
            <version>2.3</version>
            <executions>
                <execution>
                    <phase>package</phase>
                    <goals>
                        <goal>copy</goal>
                    </goals>
                    <configuration>
                        <artifactItems>
                            <artifactItem>
                                <groupId>org.eclipse.jetty</groupId>
                                <artifactId>jetty-runner</artifactId>
                                <version>9.0.4.v20130625</version>
                                <destFileName>jetty-runner.jar</destFileName>
                            </artifactItem>
                        </artifactItems>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```


### Web.xml

Paste this into your `web.xml` (`src/main/webapp/WEB-INF/`) and it should work without modification. 
You don't want to spend time editing this stuff before you get started, right?

```xml
<?xml version="1.0" encoding="UTF-8"?>
    <web-app xmlns="http://java.sun.com/xml/ns/javaee" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://java.sun.com/xml/ns/javaee
                      http://java.sun.com/xml/ns/javaee/web-app_3_0.xsd"
        version="3.0" metadata-complete="true">

        <display-name>Restolino</display-name>
        <description>Simple REST framework.</description>

        <filter>
            <filter-name>filter</filter-name>
            <filter-class>com.github.davidcarboni.restolino.servlet.Filter</filter-class>
        </filter>
        <filter-mapping>
            <filter-name>filter</filter-name>
            <url-pattern>/*</url-pattern>
        </filter-mapping>

        <servlet>
            <servlet-name>api</servlet-name>
            <servlet-class>com.github.davidcarboni.restolino.Api</servlet-class>
            <load-on-startup>1</load-on-startup>
        </servlet>
        <servlet-mapping>
            <servlet-name>api</servlet-name>
            <url-pattern>/api/*</url-pattern>
        </servlet-mapping>

    </web-app>
```


### Run

This is what you want, right? Minimum time-to-working. This runs your api on port 8080 with remote debug on 8000.

    mvn clean package && java -Xdebug -Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=n -jar target/dependency/jetty-runner.jar target/myapi.war

### Is it really that cool?

I was able to create a project from scratch (the hard way, thanks Eclipse) and get to a running api in 10 minutes by cutting and pasting the above snippets. For me that's a lot faster than configuring Spring or even Jersey, not to mention the confusion of debugging the config.

Honestly, I build this in a day so don't expect a miracles, but hopefully it will give you a boost with getting stuff done rather than learning to love someone else's framework.

