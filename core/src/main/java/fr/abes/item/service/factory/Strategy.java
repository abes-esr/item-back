package fr.abes.item.service.factory;

import fr.abes.item.constant.TYPE_DEMANDE;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

@Documented
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface Strategy {
    Class type();

    TYPE_DEMANDE[] typeDemande() default {};
}
