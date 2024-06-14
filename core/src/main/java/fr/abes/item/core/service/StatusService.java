package fr.abes.item.core.service;

import fr.abes.cbs.exception.CBSException;
import fr.abes.cbs.process.ProcessCBS;
import fr.abes.item.core.constant.Constant;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.IOException;

@Slf4j
@Service
public class StatusService {
    @Getter
    private ProcessCBS cbs;

    @Value("${sudoc.login}")
    private String login;

    @Value("${sudoc.serveur}")
    private String serveurSudoc;

    @Value("${sudoc.port}")
    private String portSudoc;

    @Autowired
    private DataSource baseXmlDataSource;

    @Autowired
    private JdbcTemplate itemJdbcTemplate;

    public StatusService() {
        this.cbs = new ProcessCBS();
    }
    /**
     * Service sondant le status de la connexion au CBS
     * @return true si le client CBS repond, false dans le cas contraire
     */
    public Boolean getCbsConnectionStatus(){
        try {
            cbs.authenticate(serveurSudoc, portSudoc, login, Constant.PASSSUDOC);
            return true;
        } catch (CBSException | IOException e) {
            log.error("serveur " + serveurSudoc + " : " + e.getMessage());
            return false;
        }
    }

    /**
     * @return true si la requete SQL a fonctionné, ce qui signifie que la base XML est opérationnelle
     * false si la connection à la base XML à échoué
     */
    public Boolean getXmlConnectionStatus(){
        JdbcTemplate jdbcTemplateBaseXml;
        jdbcTemplateBaseXml = new JdbcTemplate(baseXmlDataSource);

        try {
            SqlRowSet objectTest = jdbcTemplateBaseXml.queryForRowSet("select current_date");
            return objectTest.first();
        } catch (DataAccessException e){
            log.error(e.getMessage());
            return false;
        }
    }

    /**
     * @return true si la requete SQL a fonctionné, ce qui signifie que la base ITEM est opérationnelle
     * false si la connection à la base ITEM à échoué
     */
    public Boolean getKopyaDataBaseStatus(){
        try {
            this.itemJdbcTemplate.queryForRowSet("SELECT user FROM role limit 1"); //Micro requête pour un tps de réponse très rapide (juste première occurence)
            return true;
        } catch (DataAccessException e) {
            log.error(e.getMessage());
            return false;
        }
    }
}
