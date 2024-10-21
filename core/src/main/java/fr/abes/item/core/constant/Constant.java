package fr.abes.item.core.constant;

import java.io.Serial;
import java.io.Serializable;
import java.time.format.DateTimeFormatter;


public class Constant implements Serializable {
	@Serial
	private static final long serialVersionUID = 1L;
	//Type exemplarisation Monographie électronique
	public static final int TYPEEXEMP_MONOELEC = 1;
	//type exemplarisation Périodiques électroniques
	public static final int TYPEEXEMP_PERIO = 2;
	//type exemplarisation Autres ressoures (Monographies Imprimées)
	public static final int TYPEEXEMP_AUTRE = 3;

	//Etat initial de la demandeModif, attend un fichierInitial
	public static final int ETATDEM_PREPARATION = 1;
	//Etat après appel de la procédure stockée Oracle
	public static final int ETATDEM_PREPAREE = 2;
	//Etat après récupération du fichier par l'utilisateur, attend un fichierEnrichi
	public static final int ETATDEM_ACOMPLETER = 3;
	//Paramètres d'entrée sélectionné, simulation prête à être lancée
	public static final int ETATDEM_SIMULATION = 4;
	//Simulation terminée, demandeModif en attente de traitement par le batch
	public static final int ETATDEM_ATTENTE = 5;
	//Etat en attente fictif pour gérer les 2 fichiers de sauvegardes pour les demandes de SUPP
	public static final int ETATDEM_ATTENTE_2 = 11;
	//DemandeModif en cours de traitement par le batch
	public static final int ETATDEM_ENCOURS = 6;
	//DemandeModif traiteée et terminée
	public static final int ETATDEM_TERMINEE = 7;
	//DemandeModif en erreur : une intervention technique est requise
	public static final int ETATDEM_ERREUR = 8;
	//DemandeModif archivée
	public static final int ETATDEM_ARCHIVEE = 9;
	//Demande supprimée mais conservée en base
	public static final int ETATDEM_SUPPRIMEE = 10;


	/**Generic messages*/
	public static final String FAILED = "FAILED";
	public static final String COMPLETED = "COMPLETED";
	public static final String BLOCKED = "blocked";
	public static final String EXEMPLAIRE_CREE = "exemplaire créé";
	public static final String EXEMPLAIRE_MODIFIE = "Le traitement a été effectué.";
	public static final String EXEMPLAIRE_SUPPRIME = "exemplaire supprimé";
	public static final String MSG = "message : ";
	public static final String FILE_END = "Fin du fichier.";
	public static final String TEXTE_991_MODIF = "Exemplaire modifié automatiquement";
	public static final String TEXTE_991_CREA = "Exemplaire créé en masse par ITEM";
	public static final String AUCUNE_DEMANDE = "AUCUNE DEMANDE";
	public static final String DEFAULT = "default";

	/**Authentication failed*/
	public static final String UTILISATEUR_ABSENT_BASE = "Cet utilisateur n'existe pas dans la base de données.";
	public static final String ACCES_INTERDIT = "Accès interdit.";
	public static final String ACCES_REFUSE = "Accès refusé.";
	public static final String IP_BLOCKED = "Votre adresse IP est bloquée.";
	public static final String WRONG_LOGIN_AND_OR_PASS = "Mauvais login et / ou mot de passe.";

	/**Prefix name from files put on server*/
	public static final String FIC_INITIAL_NAME = "fichier_initial_";
	public static final String FIC_RESULTAT_NAME = "fichier_resultat_";
	public static final String FIC_VALIDE_NAME = "fichier_valide_";
	public static final String FIC_CORRESPONDANCE_NAME = "fichier_correspondance_";
	public static final String FIC_ENRICHI_NAME = "fichier_enrichi_";
	public static final String FIC_PREPARE_NAME = "fichier_prepare_";
	public static final String FIC_SAUVEGARDE_NAME = "fichier_sauvegarde_";

	/**Application services errors*/
	public static final String STORAGE_SERVICE_INITIALIZATION_ERROR = "Ne peut pas initialiser le stockage, erreur au moment de la creation du repertoire : ";
	public static final String STORAGE_SERVICE_MALFORMED_URL_FILE_STORED = "Nom de fichier malformé : Echec de lecture du fichier stocké : ";
	public static final String UNAVAILABLE_SERVICE = "Service non disponible pour ce type : ";
	public static final String GO_BACK_TO_PREVIOUS_STEP_ON_DEMAND_FAILED = "Impossible de revenir à l'étape précédente de la demande.";
	public static final String GO_BACK_TO_IDENTIFIED_STEP_ON_DEMAND_FAILED = "Impossible de revenir à l'étape spécifiée de la demande.";
	public static final String LINES_TO_BE_PROCESSED_REMAIN = "Il reste des lignes du fichier à traiter dans la demande.";
	public static final String DEMANDE_IS_NOT_IN_STATE = "La demande n'est pas dans l'état : ";

	/**Extensions*/
	public static final String USER_NUM = "userNum";
	public static final String EXTENSIONCSV = ".csv";
	public static final String EXTENSIONTXT = ".txt";

	/**Maximum number of lines allowed in file sent*/
	public static final int MAX_LIGNE_FICHIER_INIT_MODIF = 3000;
	public static final int MAX_LIGNE_FICHIER_INIT_EXEMP = 5000;
	public static final int MAX_LIGNE_FICHIER_INIT_SUPP = 15000;
	public static final int MAX_LIGNE_APPELWS = 300;


	/**Get specific format for dates*/
	public static final DateTimeFormatter formatDate = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public static final String LIGNE_FICHIER_SERVICE_PATTERN = "^(?<ppn>\\d{1,9}X?);(?<rcr>\\d{8,9});(?<epn>\\d{1,9}X?);(?<valeur>.+)?";

	public static final String LIGNE_FICHIER_SERVICE_PATTERN_SANS_VALEUR = "(?<ppn>\\d{1,9}X?)?;(?<rcr>\\d{8,9});(?<epn>\\d{1,9}X?)?";

    /**Specific errors on file format*/
	public static final String ERR_FILE_NOT_FOUND = "Fichier introuvable.";
	public static final String ERR_FILE_NOINDEX = "L'en-tête du fichier est non conforme. Il ne contient pas d’index de recherche.";
	public static final String ERR_FILE_NOREQUESTS = "Le fichier ne contient pas de requêtes. Merci de consulter la documentation utilisateur à cette adresse : <a href=\"http://documentation.abes.fr/aideitem/index.html\" target=\"_blank\" style=\"color:white\">http://documentation.abes.fr/aideitem/index.html</a>";
	public static final String ERR_FILE_NOTRAIT = "Aucun traitement associé à la demande.";
	public static final String ERR_FILE_TOOMUCH_START = "Le fichier dépasse la limite des ";
	public static final String ERR_FILE_TOOMUCH_END = " notices. Merci de consulter la documentation utilisateur à cette adresse : <a href=\"http://documentation.abes.fr/aideitem/index.html\" target=\"_blank\" style=\"color:white\">http://documentation.abes.fr/aideitem/index.html</a>";
	public static final String ERR_FILE_TOOMUCH_MODIF = Constant.ERR_FILE_TOOMUCH_START + Constant.MAX_LIGNE_FICHIER_INIT_MODIF + Constant.ERR_FILE_TOOMUCH_END;
	public static final String ERR_FILE_TOOMUCH_EXEMP = Constant.ERR_FILE_TOOMUCH_START + Constant.MAX_LIGNE_FICHIER_INIT_EXEMP + Constant.ERR_FILE_TOOMUCH_END;
	public static final String ERR_FILE_TOOMUCH_SUPP = Constant.ERR_FILE_TOOMUCH_START + Constant.MAX_LIGNE_FICHIER_INIT_SUPP + Constant.ERR_FILE_TOOMUCH_END;
	public static final String ERR_FILE_3COL_MODIF = "La première ligne du fichier doit contenir 4 colonnes (ppn;rcr;epn;zone).";
	public static final String ERR_FILE_3COL_SUPP = "La première ligne du fichier doit contenir 3 colonnes (ppn;rcr;epn)";
	public static final String ERR_FILE_3COL_SUPP_ANY_LINE = "La ligne ne doit contenir que trois colones";
	public static final String ERR_FILE_ERRLINE = "Erreur ligne ";
	public static final String ERR_FILE_ONLYONEPPN = "la ligne ne doit contenir qu'un ppn (sur 9 caractères).";
	public static final String ERR_FILE_HEAD4TH = "La valeur en-tête de la quatrieme colonne n'est pas valide.";
	public static final String ERR_FILE_LINELENGTH = " : Il y a un problème lié à la longueur de la ligne.";
	public static final String ERR_FILE_TYPEFILE = "Type de fichier inconnu: ";
	public static final String ERR_FILE_TYPEDEMANDE = " pour le type de demande ";
	public static final String ERR_FILE_4COLNONVIDE = "La valeur de la 4è colonne ne doit pas être vide.";
	public static final String ERR_FILE_4COLZONE = "impossible de lancer un traitement sur la zone ";
	public static final String ERR_FILE_4COLVIDE = "La valeur de la 4è colonne doit être vide.";
	public static final String ERR_FILE_WRONGRCR = "La valeur du rcr ne correspond pas au rcr de la demande.";
	public static final String ERR_FILE_WRONGPPN = "Le PPN n'est pas conforme.";
	public static final String ERR_FILE_WRONGEPN = "La valeur de l'epn n'est pas conforme.";

	public static final String ERR_FILE_DOLLARFORBID = "le caractère $ est interdit dans la 4è colonne.";
	public static final String ERR_FILE_LINEFILE = "Erreur de traitement sur la ligne du fichier.";
	public static final String ERR_FILE_FORMAT = "Erreur de format de fichier : le fichier doit être au format .txt ou .csv.";
	public static final String ERR_FILE_NOTICE_EPN_NUMBER = "Erreur de communication avec le serveur sur la récupération de la notice.";
	public static final String ERR_FILE_NOTICE_NOT_FOUND = "Aucune notice ne correspond à la recherche.";
	public static final String ERR_FILE_MULTIPLES_NOTICES_FOUND = "Plusieurs PPN correspondent à la requête : ";
	public static final String ERR_FILE_SEARCH_INDEX_NOT_COMPLIANT = "Impossible de générer la requête, l'index de recherche n'est pas conforme";
	public static final String ERR_FILE_SEARCH_INDEX_NOT_RECOGNIZED_FOR_DEMANDE = "Index non reconnu pour ce type de demande";
	public static final String ERR_FILE_SEARCH_INDEX_CODE_NOT_COMPLIANT = "Le codeIndex de recherche n'est pas valide";
	public static final String WARN_NOTICE_EPN_INEXISTANT = "Exemplaire inexistant";


	/**Specific errors on storage file*/
	public static final String ERR_FILE_STORAGE_EMPTY_FILE = "Echec de stockage du fichier car fichier vide : ";
	public static final String ERR_FILE_STORAGE_FILE = "Echec de stockage du fichier suivant : ";
	public static final String ERR_FILE_STORAGE_FILE_UNREADABLE = "Fichier illisible, verifier que le format du fichier est bien un fichier texte.";
	public static final String ERR_FILE_READING = "Ne peut pas lire le fichier : ";

	/**Specific errors on file checking*/
	public static final String ERR_FILE_LIGNE_ANORNALE = "L'en-tête du fichier est non conforme : il contient des caractères parasites. Merci de consulter la documentation utilisateur à cette adresse : <a href=\"http://documentation.abes.fr/aideitem/index.html\" target=\"_blank\" style=\"color:white\">http://documentation.abes.fr/aideitem/index.html</a>";
	public static final String ERR_FILE_INDEXINCONNU = "L'index choisi est inconnu. Merci de consulter la documentation utilisateur à cette adresse : <a href=\"http://documentation.abes.fr/aideitem/index.html\" target=\"_blank\" style=\"color:white\">http://documentation.abes.fr/aideitem/index.html</a>";
	public static final String ERR_FILE_ZONENONAUTORISEE = "Zone(s) non autorisée(s) : ";
	public static final String ERR_FILE_SOUSZONENONAUTORISEE = "Sous-zone(s) non autorisée(s) : ";
	public static final String ERR_FILE_MANDATORY_ZONE_MISSING = "La zone suivante est obligatoire : ";
	public static final String ERR_FILE_WRONGNBCOLUMNS = "Le nombre de colonnes du fichier ne correspond pas au nombre de données de l'en-tête.";
	public static final String ERR_FILE_DATEAUTEURTITRE_TITREMANQUANT = "Il manque le titre sur cette ligne pour votre recherche par Date/Auteur/Titre.";
	public static final String ERR_FILE_DATENOK = "Le champ date doit contenir 4 caractères numériques.";
	public static final String ERR_FILE_NOZONE = "L'en-tête du fichier est non conforme : il ne contient pas de données d’exemplaires Merci de consulter la documentation utilisateur à cette adresse : ";
	public static final String ERR_FILE_ZONEINCOMPLETE = "L'en-tête du fichier est non conforme. Les données d’exemplaires sont incomplètes : il manque une ou plusieurs sous-zones. Merci de consulter la documentation utilisateur à cette adresse : <a href=\"http://documentation.abes.fr/aideitem/index.html\" target=\"_blank\" style=\"color:white\">http://documentation.abes.fr/aideitem/index.html</a>";
	public static final String ERR_FILE_CARACTERES = "L'en-tête du fichier est non conforme : il ne contient pas de données d’exemplaires Merci de consulter la documentation utilisateur à cette adresse : <a href=\"http://documentation.abes.fr/aideitem/index.html\" target=\"_blank\" style=\"color:white\">http://documentation.abes.fr/aideitem/index.html</a>";

	/**Sudoc pass*/
	public static final String PASSSUDOC = "pabnot6";

	/**PPN, RCR, EPN max size*/
	public static final int TAILLEMAX = 9;

	/**Specific messages on mails sents and stats*/
	public static final String STAT_NBDEMANDESTRAITEES_FILENAME = "demandesTraiteesRCR";
	public static final String STAT_NBEXEMPLAIRESTRAITES_FILENAME = "exemplairesTraitesTraitementRCR";
	public static final String DEMANDE_MODIFICATION_START = "Demande de modification d'exemplaires ";
	public static final String DEMANDE_SUPPRESSION_START = "Demande de suppression d'exemplaires ";
	public static final String DEMANDE_EXEMPLARISATION_START = "Votre exemplarisation - ";
	public static final String DEMANDE_EXEMPLARISATION_END = "Résultat de votre exemplarisation - ";
	public static final String DEMANDE_RECOUVREMENT_START = "Votre taux de recouvrement - N°";
	public static final String DEMANDE_RECOUVREMENT_END = "Résultat de votre taux de recouvrement  - Recouvrement N°";
	public static final String DEMANDE_MAIL_DEBUT = " - Début du traitement";
	public static final String DEMANDE_MAIL_ECHEC = "Incident technique";
	public static final String HTML_BALISE_BR = "<br>";
	public static final String ANNEE = "annee";
	public static final String MOIS = "mois";

	//PATTERN_VALEUR ppn : 8 caractères alphabétiques + 1 chiffre ou X
	public static final String PATTERN_INDEX_PPN = "^(?<index>\\d{8,9}X?)";

	public static final String REG_EXP_ZONES_SOUS_ZONES = "(?<zone>([(^A)|(E)|(L)]?)[\\d]{3})((?<espace>\\s)?)+(?<indicateurs>[#\\d]{2})?(((?<sousZone1>[$][a-z\\d]))[\\s]*[;]*)? *(((?<sousZone2>[$][a-z\\d]))[\\s]*[;]*)? *(((?<sousZone3>[$][a-z\\d]))[\\s]*[;]*)? *(((?<sousZone4>[$][a-z\\d]))[\\s]*[;]*)? *(((?<sousZone5>[$][a-z\\d]))[\\s]*[;]*)? *(((?<sousZone6>[$][a-z\\d]))[\\s]*[;]*)? *(((?<sousZone7>[$][a-z\\d]))[\\s]*[;]*)? *(((?<sousZone8>[$][a-z\\d]))[\\s]*[;]*)? *(((?<sousZone9>[$][a-z\\d]))[\\s]*[;]*)? *(((?<sousZone10>[$][a-z\\d]))[\\s]*[;]*)?";
	public static final String REG_EXP_ZONE_SOUS_ZONE = "(?<zone>([(^A)|(E)|(^L)]?)[\\d]{3})+((?<espace>\\s)?)?(?<indicateurs>[#\\d]{2})?((?<espace2>\\s)*)?(?<sousZone>[$][a-z\\d])?";
	public static final String REG_EXP_ZONE_EXX = "e(?<numEx>[\\d]{2})\\s\\$a\\d{2}-\\d{2}-\\d{2}\\$b(.{1,8})";
	public static final String REG_EXP_EXEMPLAIRE = "(?<exemplaire>(e[\\d]{2})\\s\\$a\\d{2}-\\d{2}-\\d{2}\\$b(.{1,8})\\r(.*\\r)*)";
	public static final String REG_EXP_DONNEELOCALE = "(?<zone>([(^A)|(^E)|(L)]?)[\\d]{3})(?<espace>\\s)?+(?<indicateurs>[#\\d]{2})?(?<sousZone>[$][a-z\\d])";
	public static final String REG_EXP_DATE_A_4_DECIMALES = "\\d{4}";

	//PATTERN_LIGNE_ANORMALE : ligne avec seulement des points virgules sans données
	public static final String REG_EXP_LIGNE_ANORMALE = "^((?!\\w).)*$";

	/**Strategy factory errors*/
	public static final String SINGLE_STRATEGY = "Il ne peut y avoir qu'une seule strategie unique pour chaque type, trouvé multiples stratégies pour le type et le profil suivant : ";
	public static final String STRATEGY_NO_FOUND = "Pas de strategie trouvée pour le type : ";

	/**Application log messages */
	public static final String PROXY_AUTHENTICATION_WITH_LOGIN = "dans authenticate du ProxyRetry avec login = ";
	public static final String STRATEGY_ANNOTATION_FOR_BEAN_FAILED ="Ne peut résoudre l'annotation strategy pour ce bean.";
	public static final String STRATEGY_RETURN_DEFAULT_STRATEGY_NO_TYPE_DEMANDE_SELECTED = "No type_demande selected, returning default strategy";
	public static final String STRATEGY_RETURN_DEFAULT_STRATEGY_NO_TYPE_DEMANDE_SPECIFIC_STRATEGY_SELECTED = "No type_demande specific strategy found, returning default strategy";
	public static final String ERROR_OPENING_FILE_FOR_NUMBER_OF_REQUESTS_PROCESSED_BY_RCR = "Erreur dans l'ouverture du fichier pour nb demandeModifs traitées par RCR.";
	public static final String ERROR_OPENING_FILE_FOR_NUMBER_OF_EXEMPLARIES_PROCESSES_BY_RCR_AND_TREATMENT = "Erreur dans l'ouverture du fichier pour nb exemplaires traites par RCR et Traitement.";
	public static final String ERROR_RESPONDING_WITH_UNAUTHORIZED_ERROR = "Erreur : répond avec une erreur d'autorisation : message - {0}";
	public static final String ERROR_ACCESS_DATABASE = "Erreur acces a la base";
	public static final String ERROR_GENERIC_TECHNICAL_PROBLEMS = "Nous rencontrons actuellement des problèmes techniques.";
	public static final String ERROR_ACCOUNT_BLOCKED = "Le compte est bloqué. Veuillez patienter 15 minutes.";
	public static final String ERROR_ACCESS_RESSOURCE_NOT_ALLOWED = "Vous ne pouvez pas accéder à cette ressource.";
	public static final String JOB_TRAITER_LIGNE_FICHIER_START = "debut du job jobTraiterLigneFichier pour demandes de ";
	public static final String JOB_EXPORT_STATISTIQUES_START = "debut du job jobExportStatistiques...";
	public static final String SPRING_BATCH_TOTAL_TIME_EXECUTION_MILLISECONDS = "temps total execution (ms) = ";
	public static final String SPRING_BATCH_TOTAL_TIME_EXECUTION_MINUTES = "temps total execution (minutes) = ";
	public static final String ERROR_SENDING_MAIL_END_OF_TREATMENT = "Erreur lors de l'envoi du mail de fin traitement : ";
	public static final String POUR_LA_DEMANDE = "...pour la demande ";
	public static final String ENTER_DOFILTERINTERNAL = "ENTREE DANS doFilterInternal.............................";
	public static final String ERROR_ATTACHMENT_NOT_FOUND = "Fichier PJ introuvable : ";
	public static final String ERROR_ATTACHMENT_UNATTACHABLE = "Impossible d'ajouter la PJ au mail : ";
	public static final String ERROR_CONVERSION_MAIL_TO_JSON = "Erreur lors de la conversion du mail en JSON : ";
	public static final String ERROR_SUDOC_CONNECTION = "impossible de se connecter au Sudoc : ";
	public static final String ENTER_EXECUTE_FROM_GENEREFICHIER = "entrée dans execute de genereFichier....";
	public static final String ERROR_WHILE_GENERATING_THE_FILE_OR_WHILE_SENDING_MAIL = "erreur lors de la génération du fichier et / ou envoi de mail = ";
	public static final String ERROR_WHILE_CREATING_RESULT_FILE_IN_EXECUTE = "impossible de créer le fichier resultat dans execute de tasklet : ";
	public static final String NO_DEMANDE_TO_PROCESS = "aucune demande à traiter.";
	public static final String ENTER_EXECUTE_FROM_GETNEXTDEMANDETASKLET = "entrée dans execute de GetNextDemandeTasklet...";
	public static final String ENTER_EXECUTE_FROM_GETNEXTDEMANDETODELETESTATUSTASKLET = "entrée dans execute de DeleteStatusDemandesTasklet";
	public static final String ENTER_EXECUTE_FROM_LIRELIGNEFICHIERTASKLET = "entrée dans execute de LireLigneFichierTasklet...";
	public static final String ERROR_FROM_SUDOC_REQUEST_OR_METHOD_SAVEXEMPLAIRE = "erreur lors de la requête au Sudoc ou du saveExemplaire";
	public static final String ERROR_FROM_RECUP_NOTICETRAITEE =	"erreur lors de la recup de la noticetraitee : ";
	public static final String ERROR_TREATMENT_LIGNE_FICHIER_WHEN_UPDATE_DEMANDE_STATE = "erreur lors du passage de la demande en etat TERMINEE : ";
	public static final String ERROR_CAUGHT = "error caught: ";
	public static final String ERROR_MAJ_LIGNE_FICHIER_WRITE = "erreur lors de majLigneFichier dans write : ";
	public static final String LIGNE_TRAITEE = "ligne traitee pour ";
	public static final String ERROR_MAJ_LIGNE = "erreur lors de la maj de la ligne ";
	public static final String JOB_EXPORT_END_FOR_PERIOD = "job export des statistiques terminé pour la période ";
	public static final String ERROR_MONTH_RANGE = "Le mois doit être compris entre 1 et 12";
	public static final String ERROR_YEAR_RANGE = "L'année ne peut pas être inférieure à l'année courante";
	public static final String ERROR_UNABLE_TO_CREATE_FILE = "impossible de créer le fichier ppn;rcr;epn";
	public static final String ERROR_UNABLE_TO_CREATE_SORTED_FILE = "impossible de créer le fichier de correspondance trié";
	public static final String ERROR_UNKNOWN_REST_CONTROLLER = "unknown error caught in RESTController, {}";
	public static final String REST_RESPONDING_WITH_STATUS = "Response REST avec statut {}";
	public static final String ERROR_FIRST_LINE_OF_FILE_NULL = "la première ligne du fichier est nulle";
	public static final String ENTER_AUTORISER_ACCES_DEMANDE_ILN = "entree dans autoriserAccesDemandeParILN..........";
	public static final String ENTER_CREATION_DEMANDE_BY_USERNUM = "entree dans autoriserCreationDemandeParUserNum..........";
	public static final String ENTER_AUTORISER_MAJ_UTILISATEUR_BY_USERNUM = "entree dans autoriserMajUtilisateurParUserNum..........";
	public static final String ENTER_AUTHENTICATE = "entree dans authenticate...";
	public static final String ERROR_SUDOC_WS_AUTHENTICATION = "rejet du service web d'authentification Sudoc";
	public static final String ERROR_AUTHENTICATION_IN_SECURITY_CONTEXT = "Could not set user authentication in security context";
	public static final String ERROR_BLOCKED_IP = "dans isblocked IP, attemptsCache.get(key) = ";
	public static final String NUMBER_IP_TENTATIVES = "NB de tentatives pour ip =";
	public static final String ENTER_LOGIN_FAILED = "entree dans loginFailed...";
	public static final String ENTER_LOGIN_SUCCEED = "entree dans loginSucceeded pour IP = ";
	public static final String JWT_CLAIMS_STRING_EMPTY = "JWT claims string is empty : {}";
	public static final String JWT_TOKEN_UNSUPPORTED = "Unsupported JWT token : {}";
	public static final String JWT_TOKEN_EXPIRED = "Expired JWT token : {}";
	public static final String JWT_TOKEN_INVALID = "Invalid JWT token : {}";
	public static final String JWT_SIGNATURE_INVALID = "Invalid JWT signature : {}";
}
