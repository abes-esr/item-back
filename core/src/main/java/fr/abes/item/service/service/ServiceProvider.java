package fr.abes.item.service.service;


import fr.abes.item.service.*;
import lombok.Getter;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Contient l'ensemble des services qui vont permettre d'accéder à la couche DAO
 * pour effectuer des opérations de lecture / écriture en base de donnée.
 *
 * Chaque service représente un type d'opération.
 */
@Service
@Getter
public class ServiceProvider {
    @Resource
    private IReferenceService reference;

    @Resource
    private IJournalService journal;

    @Resource
    private IStorageService storage;

    @Resource
    private ITraitementService traitement;

    @Resource
    private IUtilisateurService utilisateur;

    @Resource
    private IStatusService status;

    @Resource
    private IDemandeModifService demandeModif;

    @Resource
    private IDemandeExempService demandeExemp;

    @Resource
    private IDemandeRecouvService demandeRecouv;

    @Resource
    private ILigneFichierModifService ligneFichierModif;

    @Resource
    private ILigneFichierExempService ligneFichierExemp;

    @Resource
    private ILigneFichierRecouvService ligneFichierRecouv;

}
