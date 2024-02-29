package fr.abes.item.traitement;

import fr.abes.cbs.exception.CBSException;
import fr.abes.item.constant.Constant;
import fr.abes.item.service.TraitementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ProxyRetry {
    private final TraitementService traitementService;

    public ProxyRetry(TraitementService traitementService) {
        this.traitementService = traitementService;
    }

    /**
     * permet de retenter plusieurs fois la connexion à CBS
     *
     * @param login login d'authentification au CBS
     * @throws CBSException : erreur d'authentification CBS
     */
    @Retryable
    public void authenticate(String login) throws CBSException {
        log.warn(Constant.PROXY_AUTHENTICATION_WITH_LOGIN + login);
        traitementService.authenticate(login);
    }

    /**
     * Méthode de modification d'un exemplaire existant dans le CBS (4 tentatives max)
     * @param noticeTraitee notice modifiée
     * @throws CBSException : erreur CBS
     */
    @Retryable(maxAttempts = 4, retryFor = Exception.class,
            noRetryFor = CBSException.class, backoff = @Backoff(delay = 1000, multiplier = 2) )
    public void saveExemplaire(String noticeTraitee) throws CBSException {
        traitementService.saveExemplaire(noticeTraitee);
    }

    /**
     * Méthode permettant la création d'un nouvel exemplaire et du bloc de donnée locale dans le CBS (4 tentatives max)
     * @param numEx numéro de l'exemplaire à créer (pour lancer la commande cre exx
     * @param noticeACreer chaine de la notice d'exemplaire à créer
     * @param donneeLocale chaine du bloc de donnée locale à créer ou rajouter au bloc existant
     * @param modDonneeLocale indicateur de modification du bloc de donnée locale
     * @throws CBSException : erreur CBS
     */
    @Retryable(maxAttempts = 4, retryFor = Exception.class,
            noRetryFor = CBSException.class, backoff = @Backoff(delay = 1000, multiplier = 2) )
    public void newExemplaire(String numEx, String noticeACreer, String donneeLocale, boolean modDonneeLocale) throws CBSException {
        //si on a des données locales à créer
        String donneesLocalesToCreate = donneeLocale.substring(1, donneeLocale.length()-1);
        //création de l'exemplaire
        traitementService.getCbs().creerExemplaire(numEx);
        traitementService.getCbs().newExemplaire(noticeACreer.substring(1, noticeACreer.length()-1));
        if (!donneesLocalesToCreate.isEmpty()) {
            //s'il y a des données locales existantes, on modifie
            if (modDonneeLocale) {
                traitementService.getCbs().modLoc(donneesLocalesToCreate);
            } else {
                //s'il n'y a pas de donnée locale dans la notice, on crée le bloc
                traitementService.getCbs().creerDonneeLocale();
                traitementService.getCbs().newLoc(donneesLocalesToCreate);
            }
        }
    }

}
