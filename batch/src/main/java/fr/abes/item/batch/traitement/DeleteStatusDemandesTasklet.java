package fr.abes.item.batch.traitement;

import fr.abes.item.core.configuration.factory.StrategyFactory;
import fr.abes.item.core.constant.Constant;
import fr.abes.item.core.constant.TYPE_DEMANDE;
import fr.abes.item.core.entities.item.Demande;
import fr.abes.item.core.exception.DemandeCheckingException;
import fr.abes.item.core.service.IDemandeService;
import fr.abes.item.core.utilitaire.Utilitaires;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import java.util.List;

@Slf4j
public class DeleteStatusDemandesTasklet implements Tasklet {
    private final StrategyFactory strategyFactory;
    private final TYPE_DEMANDE typeDemande;

    public DeleteStatusDemandesTasklet(StrategyFactory strategyFactory, TYPE_DEMANDE typeDemande) {
        this.strategyFactory = strategyFactory;
        this.typeDemande = typeDemande;
    }

    @Override
    public RepeatStatus execute(@NonNull StepContribution stepContribution, @NonNull ChunkContext chunkContext) throws Exception {
        log.info("Passage en statut supprimé (mais conservation en base) des demandes de " + Utilitaires.getLabelTypeDemande(this.typeDemande) + " lancé");
        log.info(Constant.ENTER_EXECUTE_FROM_GETNEXTDEMANDETODELETESTATUSTASKLET);
        try {
            IDemandeService service = strategyFactory.getStrategy(IDemandeService.class, typeDemande);
            List<? extends Demande> demandes = service.getDemandesToPlaceInDeletedStatus();
            if (demandes == null) {
                log.info(Constant.NO_DEMANDE_TO_PROCESS);
                stepContribution.setExitStatus(new ExitStatus("AUCUNE DEMANDE"));
                return RepeatStatus.FINISHED;
            }
            //Iteration sur chaque demande pour en modifier le statut
            for (Demande demande : demandes) {
                log.info("Passage de la demande d'exemplarisation " + demande.getNumDemande() + " au statut" + Constant.ETATDEM_SUPPRIMEE);
                service.changeState(demande, Constant.ETATDEM_SUPPRIMEE);
            }
            stepContribution.setExitStatus(ExitStatus.COMPLETED);
        } catch (DemandeCheckingException e) {
            log.error("Erreur lors du passage à statut supprimé" + e);
            stepContribution.setExitStatus(ExitStatus.FAILED);
            return RepeatStatus.FINISHED;
        }
        return RepeatStatus.FINISHED;
    }

}
