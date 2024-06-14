package fr.abes.item.batch.traitement.model;

import fr.abes.item.core.entities.item.Demande;

public interface ILigneFichierDtoService {
    String getValeurToWriteInFichierResultat(Demande demande, Integer nbPpnInFileResult);
}
