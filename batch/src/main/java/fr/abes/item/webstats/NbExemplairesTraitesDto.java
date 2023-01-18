package fr.abes.item.webstats;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@NoArgsConstructor
@Getter @Setter
public class NbExemplairesTraitesDto implements Serializable {
    private Integer typeTraitement;
    private String rcr;
    private Integer nbExemplaires;
}
