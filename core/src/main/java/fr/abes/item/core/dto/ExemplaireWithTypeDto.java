package fr.abes.item.core.dto;

import fr.abes.cbs.notices.Exemplaire;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ExemplaireWithTypeDto {
    private List<Exemplaire> exemplaires = new ArrayList<>();
    private String type;

    public void addExemplaires(List<Exemplaire> exemplaires) {
        this.exemplaires.addAll(exemplaires);
    }
}
