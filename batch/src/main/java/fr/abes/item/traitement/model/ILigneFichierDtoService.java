package fr.abes.item.traitement.model;

import fr.abes.item.entities.item.Demande;

public interface ILigneFichierDtoService {
    String getValeurToWriteInFichierResultat(Demande demande, Integer nbPpnInFileResult);
}
