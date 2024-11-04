package fr.abes.item.core.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class StatusService {
    @Autowired
    private JdbcTemplate itemJdbcTemplate;

    /**
     * @return true si la requete SQL a fonctionné, ce qui signifie que la base ITEM est opérationnelle
     * false si la connection à la base ITEM à échoué
     */
    public Boolean getItemDataBaseStatus(){
        try {
            this.itemJdbcTemplate.queryForRowSet("SELECT user FROM role limit 1"); //Micro requête pour un tps de réponse très rapide (juste première occurence)
            return true;
        } catch (DataAccessException e) {
            log.error(e.getMessage());
            return false;
        }
    }
}
