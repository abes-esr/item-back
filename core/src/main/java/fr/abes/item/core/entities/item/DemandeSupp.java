package fr.abes.item.core.entities.item;

import fr.abes.item.core.constant.TYPE_DEMANDE;
import fr.abes.item.core.constant.TYPE_SUPPRESSION;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "DEMANDE_SUPP")
@NoArgsConstructor
@Getter
@Setter
public class DemandeSupp extends Demande {
    @Column(name = "TYPE_SUPPRESSION")
    @Enumerated(EnumType.STRING)
    private TYPE_SUPPRESSION typeSuppression;
    @Override
    public TYPE_DEMANDE getTypeDemande() {
        return TYPE_DEMANDE.SUPP;
    }
}
