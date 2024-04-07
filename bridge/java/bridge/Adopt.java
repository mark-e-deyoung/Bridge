package bridge;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * An annotation for editing class properties
 */
@Target(ElementType.TYPE)
public @interface Adopt {

    /**
     * Removes existing class metadata when <code>true</code>
     *
     * @return Clean status
     */
    boolean clean() default false;

    /**
     * Overwrites the <code>extends</code> clause
     *
     * @implNote Compatible super constructor(s) must exist within the specified class for them to be usable
     * @return Parent class
     */
    Class<?> parent() default Bridges.class;

    /**
     * Appends to the <code>implements</code> clause
     *
     * @return Parent interfaces
     */
    Class<?>[] interfaces() default {};

    /**
     * Adds generic type data to the class definition
     *
     * @return Signature string
     */
    String signature() default "";
}
