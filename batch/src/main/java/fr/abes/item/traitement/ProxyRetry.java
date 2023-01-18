package fr.abes.item.traitement;

import fr.abes.cbs.exception.CBSException;
import fr.abes.item.constant.Constant;
import fr.abes.item.service.service.ServiceProvider;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ProxyRetry {
    @Autowired
    @Getter
    ServiceProvider service;

    /**
     * permet de retenter plusieurs fois la connexion à CBS
     *
     * @param login
     * @throws CBSException
     */
    @Retryable
    public void authenticate(String login) throws CBSException {
        log.warn(Constant.PROXY_AUTHENTICATION_WITH_LOGIN + login);
        getService().getTraitement().authenticate(login);
    }

    /**
     * Méthode de modification d'un exemplaire existant dans le CBS (4 tentatives max)
     * @param noticeTraitee notice modifiée
     * @param epn epn de la notice à modifier
     * @throws CBSException : erreur CBS
     */
    @Retryable(maxAttempts = 4, include = Exception.class,
            exclude = CBSException.class, backoff = @Backoff(delay = 1000, multiplier = 2) )
    public void saveExemplaire(String noticeTraitee, String epn) throws CBSException {
        getService().getTraitement().saveExemplaire(noticeTraitee, epn);
    }

    /**
     * Méthode permettant la création d'un nouvel exemplaire et du bloc de donnée locale dans le CBS (4 tentatives max)
     * @param numEx numéro de l'exemplaire à créer (pour lancer la commande cre exx
     * @param noticeACreer chaine de la notice d'exemplaire à créer
     * @param donneeLocale chaine du bloc de donnée locale à créer ou rajouter au bloc existant
     * @param modDonneeLocale indicateur de modification du bloc de donnée locale
     * @throws CBSException : erreur CBS
     */
    @Retryable(maxAttempts = 4, include = Exception.class,
            exclude = CBSException.class, backoff = @Backoff(delay = 1000, multiplier = 2) )
    public void newExemplaire(String numEx, String noticeACreer, String donneeLocale, boolean modDonneeLocale) throws CBSException {
        //si on a des données locales à créer
        String donneesLocalesToCreate = donneeLocale.substring(1, donneeLocale.length()-1);
        //création de l'exemplaire
        getService().getTraitement().getCbs().creerExemplaire(numEx);
        getService().getTraitement().getCbs().newExemplaire(noticeACreer.substring(1, noticeACreer.length()-1));
        if (!donneesLocalesToCreate.isEmpty()) {
            //s'il y a des données locales existantes, on modifie
            if (modDonneeLocale) {
                getService().getTraitement().getCbs().modLoc(donneesLocalesToCreate);
            } else {
                //s'il n'y a pas de donnée locale dans la notice, on crée le bloc
                getService().getTraitement().getCbs().creerDonneeLocale();
                getService().getTraitement().getCbs().newLoc(donneesLocalesToCreate);
            }
        }
    }

}
