package com.myren.processor;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;
import org.junit.jupiter.api.Test;

import javax.tools.JavaFileObject;

import static com.google.testing.compile.CompilationSubject.assertThat;

class DeprecatedAnnotationProcessorTest {

    private final String TestClassFullyQualifiedName = "com.myren.processor.TestClass";
    private final String TestClassTemplate = """
        import java.lang.annotation.*;
        class TestClass {
            @Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.TYPE, ElementType.ANNOTATION_TYPE})
            @Retention(RetentionPolicy.RUNTIME)
            @interface Schema {
                String name() default "";
                @Deprecated
                boolean required() default false;
            }
            %s
        }
    """;

    @Test
    void detectsDeprecatedAttributesInClassAnnotation() {
        JavaFileObject source = JavaFileObjects.forSourceString(TestClassFullyQualifiedName,
                String.format(TestClassTemplate, """
                    @Schema(name = "class", required = true)
                    class InnerClass {
                        @Schema(name = "field", required = true)
                        private String deprecatedField;
                        @Schema(name = "method", required = true)
                        public void deprecatedMethod() {}
                        public void methodWithDeprecated(@Schema(name = "parameter", required = true) String any) {}
                    }
                """));

        Compilation compilation = Compiler.javac()
                .withProcessors(new DeprecatedAnnotationProcessor())
                .compile(source);

        // Check for warnings
        assertThat(compilation).hadWarningContaining("Usage of deprecated attribute");
        assertThat(compilation).hadWarningCount(4);
    }

    @Test
    void detectsDeprecatedAttributesInAllAnnotation() {
        JavaFileObject source = JavaFileObjects.forSourceString(TestClassFullyQualifiedName,
                String.format(TestClassTemplate, """
                    @Schema(name = "class")
                    class InnerClass {
                        @Schema(name = "field")
                        private String deprecatedField;
                        @Schema(name = "method")
                        public void deprecatedMethod() {}
                        public void methodWithDeprecated(@Schema(name = "parameter") String any) {}
                    }
                """));

        Compilation compilation = Compiler.javac()
                .withProcessors(new DeprecatedAnnotationProcessor())
                .compile(source);

        // Check for warnings
        assertThat(compilation).succeededWithoutWarnings();
    }

    @Test
    void detectsDeprecatedAttributesInNoAnnotation() {
        JavaFileObject source = JavaFileObjects.forSourceString(TestClassFullyQualifiedName,
                String.format(TestClassTemplate, """
                    class InnerClass {
                        private String deprecatedField;
                        public void deprecatedMethod() {}
                        public void methodWithDeprecated(String any) {}
                    }
                """));

        Compilation compilation = Compiler.javac()
                .withProcessors(new DeprecatedAnnotationProcessor())
                .compile(source);

        // Check for warnings
        assertThat(compilation).succeededWithoutWarnings();
    }

}
