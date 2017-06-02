package pw.eisphoenix.aquacore.cmd;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Year: 2017
 *
 * @author Eisphoenix
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE})
public @interface CommandOption {
    boolean forPlayer() default true;

    boolean forConsole() default true;

    boolean forOthers() default false;
}
