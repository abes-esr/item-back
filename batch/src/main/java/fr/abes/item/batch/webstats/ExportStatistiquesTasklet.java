package fr.abes.item.batch.webstats;

import com.opencsv.CSVWriter;
import fr.abes.item.batch.LogTime;
import fr.abes.item.core.constant.Constant;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.FileWriter;
import java.util.Date;
import java.util.List;

@Slf4j
public class ExportStatistiquesTasklet implements Tasklet, StepExecutionListener {
    private final JdbcTemplate itemJdbcTemplate;

    private Integer annee;
    private Integer mois;
    private Date dateDebut;
    private Date dateFin;

    @Value("${files.upload.statistiques.path}")
    private String uploadPath;

    public ExportStatistiquesTasklet(JdbcTemplate itemJdbcTemplate) {
        this.itemJdbcTemplate = itemJdbcTemplate;
    }


    @Override
    public void beforeStep(StepExecution stepExecution) {
            this.dateDebut = (Date) stepExecution.getJobExecution().getExecutionContext().get("dateDebut");
            this.dateFin = (Date) stepExecution.getJobExecution().getExecutionContext().get("dateFin");
            this.annee = (int) stepExecution.getJobExecution().getExecutionContext().get("annee");
            this.mois = (int) stepExecution.getJobExecution().getExecutionContext().get("mois");

           }

    @Override
    public RepeatStatus execute(@NonNull StepContribution stepContribution, @NonNull ChunkContext chunkContext) throws Exception {
        exportStatistiquesDemandesModif();
        exportStatistiquesDemandesSupp();
        return RepeatStatus.FINISHED;
    }

    private void exportStatistiquesDemandesModif() {
        try (CSVWriter writer = new CSVWriter(new FileWriter(getFilename(Constant.STAT_NBDEMANDESMODIFTRAITEES_FILENAME)), ';', CSVWriter.NO_QUOTE_CHARACTER)){
            List<NbDemandesTraiteesDto> listeDemandesTraitees = getNbDemandesTraiteesModif(dateDebut, dateFin);
            for (NbDemandesTraiteesDto demande : listeDemandesTraitees) {
                writer.writeNext(new String[]{demande.getRcr(), demande.getNbDemandesTraitees().toString()});
            }
        } catch (Exception e) {
            log.error(Constant.ERR_FILE_STAT_MODIF_DEMANDES);
        }
        try (CSVWriter writer = new CSVWriter(new FileWriter(getFilename(Constant.STAT_NBEXEMPLAIRESMODIFTRAITES_FILENAME)), ';', CSVWriter.NO_QUOTE_CHARACTER)) {
            List<NbExemplairesTraitesDto> listeExemplairesTraites = getNbExemplairesTraitesModif(dateDebut, dateFin);
            for (NbExemplairesTraitesDto exemp : listeExemplairesTraites) {
                writer.writeNext(new String[]{exemp.getRcr(), exemp.getTypeTraitement().toString(), exemp.getNbExemplaires().toString()});
            }
        } catch (Exception e) {
            log.error(Constant.ERR_FILE_STAT_MODIF_EXEMP);
        }
    }

    private void exportStatistiquesDemandesSupp() {
        try (CSVWriter writer = new CSVWriter(new FileWriter(getFilename(Constant.STAT_NBDEMANDESSUPPTRAITEES_FILENAME)), ';', CSVWriter.NO_QUOTE_CHARACTER)){
            List<NbDemandesTraiteesDto> listeDemandesTraitees = getNbDemandesTraiteesSupp(dateDebut, dateFin);
            for (NbDemandesTraiteesDto demande : listeDemandesTraitees) {
                writer.writeNext(new String[]{demande.getRcr(), demande.getNbDemandesTraitees().toString()});
            }
        } catch (Exception e) {
            log.error(Constant.ERR_FILE_STAT_SUPP_DEMANDES);
        }
        try (CSVWriter writer = new CSVWriter(new FileWriter(getFilename(Constant.STAT_NBEXEMPLAIRESSUPPTRAITES_FILENAME)), ';', CSVWriter.NO_QUOTE_CHARACTER)) {
            List<NbExemplairesSuppTraitesDto> listeExemplairesTraites = getNbExemplairesTraitesSupp(dateDebut, dateFin);
            for (NbExemplairesSuppTraitesDto exemp : listeExemplairesTraites) {
                writer.writeNext(new String[]{exemp.getRcr(), exemp.getNbExemplaires().toString()});
            }
        } catch (Exception e) {
            log.error(Constant.ERR_FILE_STAT_SUPP_EXEMP);
        }
    }

    private List<NbDemandesTraiteesDto> getNbDemandesTraiteesModif(Date dateDebut, Date dateFin) {
        String query = "select RCR, count(distinct d.NUM_DEMANDE) from DEMANDE_MODIF d join JOURNAL_DEMANDE_MODIF j on j.JOU_DEM_ID = d.NUM_DEMANDE where j.JOU_ETA_ID IN (7, 9) and j.DATE_ENTREE between ? and ? group by d.RCR";
        return itemJdbcTemplate.query(query, new Object[] {dateDebut, dateFin}, new NbDemandesTraiteesMapper());
    }

    private List<NbExemplairesTraitesDto> getNbExemplairesTraitesModif(Date dateDebut, Date dateFin) {
        String query = "select d.DEM_TRAIT_ID, d.RCR, count(*) " +
                "from JOURNAL_DEMANDE_MODIF j, DEMANDE_MODIF d, LIGNE_FICHIER_MODIF lf " +
                "where j.DATE_ENTREE between ? and ? "+
                "and j.JOU_ETA_ID=6 and j.JOU_DEM_ID = d.NUM_DEMANDE and d.NUM_DEMANDE = lf.REF_DEMANDE and lf.TRAITEE=1 " +
                "group by d.DEM_TRAIT_ID, d.RCR";
        return itemJdbcTemplate.query(query, new Object[] {dateDebut, dateFin}, new NbExemplairesTraitesMapper());
    }

    private List<NbDemandesTraiteesDto> getNbDemandesTraiteesSupp(Date dateDebut, Date dateFin) {
        String query = "select RCR, count(distinct d.NUM_DEMANDE) from DEMANDE_SUPP d join JOURNAL_DEMANDE_SUPP j on j.JOU_DEM_ID = d.NUM_DEMANDE where j.JOU_ETA_ID IN (7, 9) and j.DATE_ENTREE between ? and ? group by d.RCR";
        return itemJdbcTemplate.query(query, new Object[] {dateDebut, dateFin}, new NbDemandesTraiteesMapper());
    }

    private List<NbExemplairesSuppTraitesDto> getNbExemplairesTraitesSupp(Date dateDebut, Date dateFin) {
        String query = "select d.RCR, count(*) " +
                "from JOURNAL_DEMANDE_SUPP j, DEMANDE_SUPP d, LIGNE_FICHIER_SUPP lf " +
                "where j.DATE_ENTREE between ? and ? "+
                "and j.JOU_ETA_ID=6 and j.JOU_DEM_ID = d.NUM_DEMANDE and d.NUM_DEMANDE = lf.REF_DEMANDE and lf.TRAITEE=1 " +
                "group by d.RCR";
        return itemJdbcTemplate.query(query, new Object[] {dateDebut, dateFin}, new NbExemplairesSuppTraitesMapper());
    }

    private String getFilename(String filename) {
        return uploadPath + annee + ((mois < 10) ? '0' + mois.toString() : mois.toString()) + "_" + filename + Constant.EXTENSIONCSV;
    }

    @Override
    public ExitStatus afterStep(@NonNull StepExecution stepExecution) {
        LogTime.logFinTraitement(stepExecution);
        return null;
    }
}
