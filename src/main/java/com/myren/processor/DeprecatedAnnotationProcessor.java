package com.myren.processor;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This is a processor for compiling, to raise warning message if Deprecated fields used in annotation of
 * class, property, method, parameter, etc.
 * Usage: javac -processor com.myren.processor.DeprecatedAnnotationProcessor -processorpath "[class path]" [java sources]
 */
@SupportedAnnotationTypes("*")
public class DeprecatedAnnotationProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element root : roundEnv.getRootElements()) {
            checkElementRecursively(root);
        }
        return false;
    }

    // Automatically use the latest available JDK version
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    private void checkElementRecursively(Element element) {
        // Check annotations on this element (class, field, method)
        checkDeprecatedAnnotationAttributes(element);

        // Recurse into members (fields, methods)
        for (Element enclosed : element.getEnclosedElements()) {
            checkElementRecursively(enclosed);

            if (ElementKind.METHOD == enclosed.getKind()) {
                ExecutableElement method = (ExecutableElement) enclosed;
                // Check annotations on each parameter of this method element
                for (VariableElement parameter : method.getParameters()) {
                    checkDeprecatedAnnotationAttributes(parameter);
                }
            }
        }
    }

    private void checkDeprecatedAnnotationAttributes(Element element) {
        for (AnnotationMirror annotation : element.getAnnotationMirrors()) {
            TypeElement annotationType = (TypeElement) annotation.getAnnotationType().asElement();

            // Find deprecated attributes of this annotation type
            Set<String> deprecatedAttrs = new HashSet<>();
            for (ExecutableElement method : ElementFilter.methodsIn(annotationType.getEnclosedElements())) {
                if (method.getAnnotation(Deprecated.class) != null) {
                    deprecatedAttrs.add(method.getSimpleName().toString());
                }
            }

            // Check if this attribute is any of the deprecated ones
            for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry :
                    annotation.getElementValues().entrySet()) {
                String attr = entry.getKey().getSimpleName().toString();
                if (deprecatedAttrs.contains(attr)) {
                    processingEnv.getMessager().printMessage(
                            Diagnostic.Kind.WARNING,
                            String.format("Usage of deprecated attribute '%s' in annotation @%s.",
                                    attr, annotationType.getSimpleName()),
                            element
                    );
                }
            }
        }
    }
}
