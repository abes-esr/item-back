package fr.abes.item.service;

/**
 * Service permettant de savoir si le CBS et la base XML sont KO ou pas
 * - getStatusOfServices : fait une tentative d'authentification sur les 2 services.
 *      en fonction du retour, indique KO ou pas.
 *
 */
public interface IStatusService {
    //TODO à supprimer ensuite
    String getStatusOfServices();

    Boolean getXmlConnectionStatus();
    Boolean getCbsConnectionStatus();
    Boolean getKopyaDataBaseStatus();
}
