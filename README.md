Restolino
----------


### What is it?

A brutally opinionated, super-simple REST framework for Java.

It's not comprehensive, won't give you the flexibility you want and doesn't reall care about your special needs.

What it does want to do is get you started and get out of your way, because configuring frameworks is a distraction from building your idea.

And that's it.

```xml
    <properties>
        <webulizor.groupid>com.github.davidcarboni</webulizor.groupid>
        <webulizor.artifactid>webulizor</webulizor.artifactid>
        <webulizor.version>0.7.4</webulizor.version>
    </properties>
```

 * Restolino dependencies:

```xml
    <dependencies>
    
        <dependency>
            <groupId>com.github.davidcarboni</groupId>
            <artifactId>restolino</artifactId>
            <version>0.0.1</version>
        </dependency>
    
        <!-- You'll probably want the Servlet API: -->
        <dependency>
            <groupId>javax.servlet</groupId>
            <artifactId>servlet-api</artifactId>
            <version>2.5</version>
            <scope>provided</scope>
        </dependency>
        <!-- or -->
        <dependency>
            <groupId>javax.servlet</groupId>
            <artifactId>javax.servlet-api</artifactId>
            <version>3.1.0</version>
        </dependency>
            
    </dependencies>
```

 * WAR overlay configuration:

```xml
     <build>
        <finalName>mywebapp</finalName>
        
        <plugins>
            
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-war-plugin</artifactId>
                <version>2.3</version>
                <configuration>
                    
                    <!-- If you inherit the webulizor web.xml you won't one: -->
                    <failOnMissingWebXml>false</failOnMissingWebXml>
                    
                    <overlays>
                        <overlay>
                            <groupId>${webulizor.groupid}</groupId>
                            <artifactId>${webulizor.artifactid}</artifactId>
                        </overlay>
                    </overlays>
                    
                </configuration>
            </plugin>
                
        </plugins>
        
    </build>
```
