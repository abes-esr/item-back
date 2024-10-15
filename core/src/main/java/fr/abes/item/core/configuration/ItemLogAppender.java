package fr.abes.item.core.configuration;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;

@Plugin(name = "ItemLogAppender", category = "core", elementType = Appender.ELEMENT_TYPE)
public class ItemLogAppender extends AbstractAppender {

    // Utilisation d'un ThreadLocal pour stocker l'heure de début de chaque méthode
    private static final ThreadLocal<Long> startTime = new ThreadLocal<>();

    // Seuil en ms au dessus duquel on veut afficher un message
    private final int msSeuilDetection = 0;

    protected ItemLogAppender(String name, Filter filter) {
        super(name, filter, null);
    }

    // Méthode à appeler pour démarrer le chronométrage
    public static void startMethodExecution() {
        startTime.set(System.currentTimeMillis());
    }

    /**
     * Appender permettant une normalisation des logs
     * Pour obtenir le temps d'execution d'une méthode, ajoutez
     * ItemLogAppender.startMethodExecution();
     * au début d'une méthode. L'appender calcule par méthode sans
     * besoin d'une borne d'arrêt ou de fin
     * @param event
     */
    @Override
    public void append(LogEvent event) {
        if (event.getLevel().isMoreSpecificThan(Level.INFO)) {
            String originalMessage = event.getMessage().getFormattedMessage();
            String demandeId = ThreadContext.get("demandeId");
            String typeDemande = ThreadContext.get("typeDemande");
            if (demandeId != null && typeDemande != null) {
                // Calcul du temps d'exécution
                Long startTimeValue = startTime.get();
                long executionTime = startTimeValue != null ? System.currentTimeMillis() - startTimeValue : -1;

                if (executionTime < msSeuilDetection) {
                    System.out.println("DEM_" + typeDemande + "_" + demandeId + " / "
                            + event.getSource().getClassName() + " / "
                            + event.getSource().getMethodName() + " : "
                            + originalMessage);
                }
                //Uniquement si un ItemLogAppender.startMethodExecution(); à été placé au début d'une méthode
                else{
                    System.out.println("DEM_" + typeDemande + "_" + demandeId + " / "
                            + event.getSource().getClassName() + " / "
                            + event.getSource().getMethodName() + " : "
                            + originalMessage + " / Execution time: " + executionTime + " ms");
                }
            }
        }
    }

    // Méthode statique pour créer l'instance de l'appender via le fichier de configuration
    @PluginFactory
    public static ItemLogAppender createAppender(
            @PluginAttribute("name") String name,
            @PluginElement("Filter") Filter filter) {
        return new ItemLogAppender(name, filter);
    }
}
