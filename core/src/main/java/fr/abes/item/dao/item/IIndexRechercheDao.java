package fr.abes.item.dao.item;

import fr.abes.item.entities.item.IndexRecherche;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IIndexRechercheDao extends JpaRepository<IndexRecherche, Integer> {
    /**
     * @param libelle type de recherche
     * @return Liste d'index de recherche correspondant au libellé
     */
    @Query("select i from IndexRecherche i where i.libelle like :libelle")
    List<IndexRecherche> getIndexRecherchesByLibelle(@Param("libelle") String libelle);
}
