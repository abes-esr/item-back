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

    protected ItemLogAppender(String name, Filter filter) {
        super(name, filter, null);
    }

    @Override
    public void append(LogEvent event) {
        if (event.getLevel().isMoreSpecificThan(Level.INFO)) {
            String originalMessage = event.getMessage().getFormattedMessage();
            String demandeId = ThreadContext.get("demandeId");
            String typeDemande = ThreadContext.get("typeDemande");
            if (demandeId != null && typeDemande != null) {
                System.out.println("DEM_" + typeDemande + "_" + demandeId + " : " + originalMessage);
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
