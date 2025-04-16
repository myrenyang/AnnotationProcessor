# Annotation Processor
This project is to generate AnnotationProcessor.jar

To use the processor, there are several options.

# Build

`mvn compile package`

`javac -Xlint:all -d . src/main/java/com/myren/processor/DeprecatedAnnotationProcessor.java`

`jar cf annotation-processor.jar com/myren/processor/DeprecatedAnnotationProcessor.class`

# Usage

## Javac

`javac -processor com.myren.processor.DeprecatedAnnotationProcessor -processorpath "[path to processor]" [source files]`

`javac -cp "[class path including processor]" [source files]`

Note: Because the processor is properly registered with META-INF/services/javax.annotation.processing.Processor, then Javac will automatically discover it, even without -processor or -processorpath.

e.g.: `javac -cp "c:\.m2\repository-M3\com\myren\annotation-processor\1.0.0\annotation-processor-1.0.0.jar;C:\.m2\repository-M3\io\swagger\core\v3\swagger-annotations-jakarta\2.2.9\swagger-annotations-jakarta-2.2.9.jar" ds-app/src/main/java/TryDeprecation.java`

## Maven Compiler Plugin Configuration
```xml
<project>
    <properties>
        <showWarnings>false</showWarnings>
        <failOnWarning>false</failOnWarning>
        <enableDeprecatedProcessor>false</enableDeprecatedProcessor>
    </properties>

    <pluginManagement>
        <build>
            <plugins>
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-compiler-plugin</artifactId>
                    <configuration>
                        <showDeprecation>${showWarnings}</showDeprecation>
                        <showWarnings>${showWarnings}</showWarnings>
                        <failOnWarning>${failOnWarning}</failOnWarning>
                        <annotationProcessors combine.children="append">
                            ${enableDeprecatedProcessor ? '<annotationProcessor>
                            com.myren.processor.DeprecatedAnnotationProcessor</annotationProcessor>'
                            : ''}
                        </annotationProcessors>
                    </configuration>
                </plugin>
            </plugins>
        </build>
    </pluginManagement>

    <profiles>
        <profile>
            <id>compile-showWarning</id>
            <activation>
                <activeByDefault>true</activeByDefault>
            </activation>
            <properties>
                <showWarnings>true</showWarnings>
                <enableDeprecatedProcessor>true</enableDeprecatedProcessor>
            </properties>
        </profile>
        <profile>
            <id>compile-failOnWarning</id>
            <activation>
                <activeByDefault>true</activeByDefault>
            </activation>
            <properties>
                <showWarnings>true</showWarnings>
                <enableDeprecatedProcessor>true</enableDeprecatedProcessor>
                <failOnWarning>true</failOnWarning>
            </properties>
        </profile>
    </profiles>

</project>
```

## Provide javax.annotation.processing.Processor to the Project

Manually create META-INF/services/javax.annotation.processing.Processor in the resources folder, and put following content:
`com.myren.processor.DeprecatedAnnotationProcessor`

So compiler will automatically use the processor

## Automatically generate javax.annotation.processing.Processor

Config maven pom.xml file:
```xml
<build>
    <plugins>
        <!-- Plugin to generate META-INF/services/javax.annotation.processing.Processor -->
        <!-- Put this to the usage project, or manually create above file with <processor> value -->
        <plugin>
            <groupId>org.bsc.maven</groupId>
            <artifactId>maven-processor-plugin</artifactId>
            <version>${maven-processor-plugin.version}</version>
            <executions>
                <execution>
                    <id>generate-processor-services</id>
                    <phase>generate-resources</phase>
                    <goals>
                        <goal>process</goal>
                    </goals>
                    <configuration>
                        <processors>
                            <processor>com.myren.processor.DeprecatedAnnotationProcessor</processor>
                        </processors>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```
