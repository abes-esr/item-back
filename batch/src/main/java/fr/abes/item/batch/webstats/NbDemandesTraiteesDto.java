package fr.abes.item.batch.webstats;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@NoArgsConstructor
@Getter
@Setter
public class NbDemandesTraiteesDto implements Serializable {
    private String rcr;
    private Integer nbDemandesTraitees;
}
