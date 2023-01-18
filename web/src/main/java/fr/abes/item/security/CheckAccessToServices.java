package fr.abes.item.security;

import fr.abes.item.constant.Constant;
import fr.abes.item.dao.impl.DaoProvider;
import fr.abes.item.entities.baseXml.LibProfile;
import fr.abes.item.entities.item.Utilisateur;
import fr.abes.item.exception.ForbiddenException;
import fr.abes.item.exception.UserExistException;
import fr.abes.item.service.service.ServiceProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
public class CheckAccessToServices {

   @Autowired
   ServiceProvider service;
   @Autowired
   private DaoProvider dao;

    /**
     * On ne peut accéder qu'aux demandeModifs de son ILN
     * @param id de la demandeModif
     * @param userNum id de l'utilisateur
     * @throws UserExistException si le user n'existe pas dans la base de données
     * @throws ForbiddenException si le user n'a pas accès à cette demandeModif
     */
    public void autoriserAccesDemandeParIln(Integer id, String userNum) throws UserExistException, ForbiddenException {
        log.debug(Constant.ENTER_AUTORISER_ACCES_DEMANDE_ILN);
        if (service.getUtilisateur().findById(Integer.parseInt(userNum)) == null) {
            log.error(Constant.UTILISATEUR_ABSENT_BASE);
            throw new UserExistException(Constant.UTILISATEUR_ABSENT_BASE);
        }

        String iln;
        if (service.getDemandeExemp().findById(id) != null) {
            iln = service.getDemandeExemp().findById(id).getIln();
        } else if (service.getDemandeModif().findById(id) != null) {
            iln = service.getDemandeModif().findById(id).getIln();
        } else {
            iln = service.getDemandeRecouv().findById(id).getIln();
        }
        if (!iln.equals(service.getUtilisateur().findById(Integer.parseInt(userNum)).getIln())
                // iln 1 can access to all
                && !service.getUtilisateur().findById(Integer.parseInt(userNum)).getIln().equals("1")) {
            throw new ForbiddenException(Constant.ACCES_INTERDIT);
        }

    }

    /**
     * L'admin peut accéder à tous les fichiers de toutes les demandes
     * @param id identifiant de la demande
     * @param userNum identifiant de l'utilisateur
     * @throws ForbiddenException
     * @throws UserExistException
     */
    public void autoriserAccessFichierDemandePourAdmin(Integer id, String userNum) throws ForbiddenException, UserExistException {
        log.debug(Constant.ENTER_AUTORISER_ACCES_FICHIER_DEMANDE_ADMIN);
        Utilisateur utilisateur = service.getUtilisateur().findById(Integer.parseInt(userNum));
        if (utilisateur == null) {
            log.error(Constant.USERNUM_NOT_PRESENT_ON_DATABASE);
            throw new UserExistException(Constant.UTILISATEUR_ABSENT_BASE);
        }
        String iln = (service.getDemandeExemp().findById(id) != null) ? service.getDemandeExemp().findById(id).getIln() : service.getDemandeModif().findById(id).getIln();
        if (!iln.equals(utilisateur.getIln()) || (service.getUtilisateur().isAdmin(utilisateur))) {
            throw new ForbiddenException(Constant.ACCES_INTERDIT);
        }
    }

    /**
     *     On ne peut créer une demande que pour son iln
     * @param rcr
     * @param userNum
     * @throws UserExistException si le user n'existe pas dans la base de données
     * @throws ForbiddenException si le user n'a pas accès à la création de la demande pour ce rcr
     */

    public void autoriserCreationDemandeParUserNum(String rcr, String userNum) throws UserExistException, ForbiddenException {

        log.debug(Constant.ENTER_CREATION_DEMANDE_BY_USERNUM);

        // verification que le user existe dans la base
        if (service.getUtilisateur().findById(Integer.parseInt(userNum)) == null) {
            log.error(Constant.UTILISATEUR_ABSENT_BASE);
            throw new UserExistException(Constant.UTILISATEUR_ABSENT_BASE);
        }

        // recupération de l'iln du user
        String ilnUser = service.getUtilisateur().findById(Integer.parseInt(userNum)).getIln();

        // récupération de l'iln du rcr
        Optional<LibProfile> library = dao.getLibProfile().findById(rcr);
        if (library.isPresent()) {
            String ilnRcr = library.get().getIln();
            if (!ilnUser.equals(ilnRcr)) {
                throw new ForbiddenException(Constant.ACCES_INTERDIT);
            }
        }
        else {
            throw new ForbiddenException(Constant.ACCES_INTERDIT);
        }
    }

    /**
     *  on vérifie qu'id de l'utilisateur (passé en paramètre de l'url) est bien le même
     *     que celui du token pour permettre la mise à jour des infos du user (adresse mail)
     * @param id id du user passé en paramètre par le client
     * @param userNum id du user présent dans le token
     * @throws ForbiddenException si le user n'a pas accès aux infos utilisateur
     */
    public void autoriserMajUtilisateurParUserNum(Integer id, String userNum) throws ForbiddenException {
        log.debug(Constant.ENTER_AUTORISER_MAJ_UTILISATEUR_BY_USERNUM);

        if (!id.equals(Integer.parseInt(userNum))) {
            throw new ForbiddenException(Constant.ACCES_INTERDIT);
        }
    }
}
