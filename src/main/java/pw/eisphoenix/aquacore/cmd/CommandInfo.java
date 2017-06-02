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
@Target({ElementType.TYPE})
public @interface CommandInfo {
    String name();

    String description() default "";

    String permission() default "";

    String usage() default "/%CMD%";

    CommandOption options() default @CommandOption;

    String[] aliase() default {};
}
