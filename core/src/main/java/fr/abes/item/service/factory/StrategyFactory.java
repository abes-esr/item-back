package fr.abes.item.service.factory;

import fr.abes.item.constant.Constant;
import fr.abes.item.constant.TYPE_DEMANDE;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.Advised;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.util.*;

@Slf4j
@Component
public class StrategyFactory {

    private final ApplicationContext applicationContext;

    private Map<Class, List<Object>> annotatedTypes = new HashMap<>();
    private Map<Class, Strategy> strategyCache = new HashMap<>();

    public StrategyFactory(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * Finds all beans annotated with Strategy. Does a quick sanity
     * check so only one strategy exists for each profile.
     * @see Strategy
     */
    @PostConstruct
    public void init() {

        Map<String, Object> annotatedBeanClasses = applicationContext.getBeansWithAnnotation(Strategy.class);

        sanityCheck(annotatedBeanClasses.values());

        for (Object bean : annotatedBeanClasses.values()) {
            Strategy strategyAnnotation = strategyCache.get(bean.getClass());
            getBeansWithSameType(strategyAnnotation).add(bean);
        }

    }

    /**
     * Checks to make sure there is only one strategy of each type(Interface) annotated for each profile.
     * Will throw an exception on startup if multiple strategies are mapped to the same profile.
     * @param annotatedBeanClasses a list of beans from the spring application context
     */
    private void sanityCheck(Collection<Object> annotatedBeanClasses) {

        Set<String> usedStrategies = new HashSet<>();

        for (Object bean : annotatedBeanClasses) {

            Strategy strategyAnnotation = AnnotationUtils.findAnnotation(bean.getClass(), Strategy.class);
            if (strategyAnnotation == null){
                try {
                    Object target = ((Advised) bean).getTargetSource().getTarget();
                    strategyAnnotation = AnnotationUtils.findAnnotation(target.getClass(), Strategy.class);
                } catch (Exception e) {
                    log.error(Constant.STRATEGY_ANNOTATION_FOR_BEAN_FAILED, e);
                }
            }
            strategyCache.put(bean.getClass(), strategyAnnotation);

            if (isDefault(strategyAnnotation)) {
                ifNotExistAdd(strategyAnnotation.type(), Constant.DEFAULT, usedStrategies);
            }

            for (TYPE_DEMANDE type_demande : strategyAnnotation.typeDemande()) {
                ifNotExistAdd(strategyAnnotation.type(), type_demande, usedStrategies);
            }

        }
    }

    private void ifNotExistAdd(Class type, TYPE_DEMANDE typeDemande, Set<String> usedStrategies) {
        ifNotExistAdd(type, typeDemande.name(), usedStrategies);
    }

    private void ifNotExistAdd(Class type, String typeDemande, Set<String> usedStrategies) {
        if (usedStrategies.contains(createKey(type, typeDemande))) {
            throw new RuntimeException(Constant.SINGLE_STRATEGY + type + typeDemande);
        }
        usedStrategies.add(createKey(type, typeDemande));
    }

    private String createKey(Class type, String typeDemande) {
        return (type+"_"+typeDemande).toLowerCase();
    }

    private List<Object> getBeansWithSameType(Strategy strategyAnnotation) {
        List<Object> beansWithSameType = annotatedTypes.get(strategyAnnotation.type());
        if (beansWithSameType != null) {
            return beansWithSameType;
        } else {
            List<Object> newBeansList = new ArrayList<>();
            annotatedTypes.put(strategyAnnotation.type(), newBeansList);
            return newBeansList;
        }
    }

    private boolean isDefault(Strategy strategyAnnotation) {
        return (strategyAnnotation.typeDemande().length == 0);
    }

    public <T> T getStrategy(Class<T> strategyType, TYPE_DEMANDE currentTypeDemande) {

        List<Object> strategyBeans = annotatedTypes.get(strategyType);
        Assert.notEmpty(strategyBeans, "No strategies found of type '"+ strategyType.getName()+"', are the strategies marked with @Strategy?");

        Object profileStrategy = findStrategyMatchingTypeDemande(strategyBeans, currentTypeDemande);
        if (profileStrategy == null) {
            throw new RuntimeException(Constant.STRATEGY_NO_FOUND + strategyType);
        }
        //noinspection unchecked
        return (T)profileStrategy;
    }

    private Object findStrategyMatchingTypeDemande(List<Object> strategyBeans, TYPE_DEMANDE currentTypeDemande) {

        Object defaultStrategy = null;
        for (Object bean : strategyBeans) {
            Strategy strategyAnnotation = strategyCache.get(bean.getClass());
            if(currentTypeDemande != null) {
                //Only iterate the profiles if a profile has been selected
                for (TYPE_DEMANDE type_demande : strategyAnnotation.typeDemande()) {
                    if (type_demande == currentTypeDemande) {
                        log.debug(Constant.STRATEGY_OF_TYPE_FOUND + strategyAnnotation.type() + Constant.STRATEGY_MATCHING_PROFILE + currentTypeDemande);
                        return bean;
                    }
                }
            }

            if (isDefault(strategyAnnotation)) {
                defaultStrategy = bean;
                if(currentTypeDemande == null) {
                    //In this case we can return the default and stop iterating, since we are only
                    //interested in the default strategy when no profile is selected. May save us a clock cycle or two.
                    if (log.isDebugEnabled()) {
                        log.debug(Constant.STRATEGY_RETURN_DEFAULT_STRATEGY_NO_TYPE_DEMANDE_SELECTED);
                    }
                    return defaultStrategy;
                }
            }
        }
        if (log.isDebugEnabled()) {
            if (defaultStrategy != null) {
                log.debug(Constant.STRATEGY_RETURN_DEFAULT_STRATEGY_NO_TYPE_DEMANDE_SPECIFIC_STRATEGY_SELECTED);
            }
        }
        return defaultStrategy;
    }

}