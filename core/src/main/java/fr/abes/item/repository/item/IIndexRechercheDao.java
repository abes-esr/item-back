package fr.abes.item.repository.item;

import fr.abes.item.configuration.ItemConfiguration;
import fr.abes.item.entities.item.IndexRecherche;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@ItemConfiguration
public interface IIndexRechercheDao extends JpaRepository<IndexRecherche, Integer> {
    /**
     * @param libelle type de recherche
     * @return Liste d'index de recherche correspondant au libellé
     */
    @Query("select i from IndexRecherche i where i.libelle like :libelle")
    List<IndexRecherche> getIndexRecherchesByLibelle(@Param("libelle") String libelle);
}
