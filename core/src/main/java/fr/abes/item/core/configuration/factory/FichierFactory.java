package fr.abes.item.core.configuration.factory;

import fr.abes.item.core.components.Fichier;
import fr.abes.item.core.constant.Constant;
import fr.abes.item.core.constant.TYPE_DEMANDE;
import fr.abes.item.core.exception.FileTypeException;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FichierFactory {
	private final List<Fichier> fichiers;
	
	private static final Map<Integer, Fichier> fichierCacheModif = new HashMap<>();
	private static final Map<Integer, Fichier> fichierCacheExemp = new HashMap<>();
	private static final Map<Integer, Fichier> fichierCacheRecouv = new HashMap<>();

	public FichierFactory(List<Fichier> fichiers) {
		this.fichiers = fichiers;
	}

	@PostConstruct
	public void initFichierCache() {
		for (Fichier fichier : fichiers) {
			switch (fichier.getDemandeType()) {
				case MODIF:
					fichierCacheModif.put(fichier.getType(), fichier);
					break;
				case EXEMP:
					fichierCacheExemp.put(fichier.getType(), fichier);
					break;
				default:
					fichierCacheRecouv.put(fichier.getType(), fichier);
			}
		}
	}
	
	/**
	 * Méthode récuperant le type de fichier à créer en fonction du type
	 * @param type etat d'avancement de la demande dans le traitement, trouvable dans la base de donnée sur la table ETAT_DEMANDE
	 * @param typeDemande enum qui spécifie si la demande est une demande de modification ou une demande d'exemplarisation
	 * @return un fichier qui contient le nom du fichier, le repertoire du fichier, le type de fichier, le type de demande
	 * (MODIF, EXEMP) associée au fichier, le nom du fichier généré par une méthode avec un préfixe et suffixe
	 * @throws FileTypeException le type de fichier est incorrect, non supporté pour le traitement
	 */
	public static Fichier getFichier(Integer type, TYPE_DEMANDE typeDemande) throws FileTypeException{
		switch (typeDemande) {
			case MODIF:
				Fichier fichierModif = fichierCacheModif.get(type);
				if (fichierModif == null)
					throw new FileTypeException(Constant.ERR_FILE_TYPEFILE + type + Constant.ERR_FILE_TYPEDEMANDE + typeDemande.name());
				return fichierModif;
			case EXEMP:
				Fichier fichierExemp = fichierCacheExemp.get(type);
				if (fichierExemp == null)
					throw new FileTypeException(Constant.ERR_FILE_TYPEFILE + type + Constant.ERR_FILE_TYPEDEMANDE + typeDemande.name());
				return fichierExemp;
			case RECOUV:
				Fichier fichierRecouv = fichierCacheRecouv.get(type);
				if (fichierRecouv == null)
					throw new FileTypeException(Constant.ERR_FILE_TYPEFILE + type + Constant.ERR_FILE_TYPEDEMANDE + typeDemande.name());
				return fichierRecouv;
			default:
				throw new FileTypeException(Constant.ERR_FILE_TYPEFILE + "Type demande inconnu");
		}
	}
}
