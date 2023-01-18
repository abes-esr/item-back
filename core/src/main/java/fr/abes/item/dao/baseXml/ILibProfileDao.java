package fr.abes.item.dao.baseXml;

import fr.abes.item.entities.baseXml.LibProfile;
import fr.abes.item.entities.item.DemandeExemp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.util.List;

public interface ILibProfileDao extends JpaRepository<LibProfile, String> {
    @Query("select l from LibProfile l where l.rcr in (:listRcr)")
    List<LibProfile> getShortnameAndIlnFromRcr(@Param("listRcr") List<String> listRcr);
}
