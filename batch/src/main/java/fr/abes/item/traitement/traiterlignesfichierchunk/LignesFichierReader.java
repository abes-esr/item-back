package fr.abes.item.traitement.traiterlignesfichierchunk;

import fr.abes.item.traitement.ProxyRetry;
import fr.abes.item.traitement.model.LigneFichierDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class LignesFichierReader implements ItemReader<LigneFichierDto>, StepExecutionListener {

    private List<LigneFichierDto> lignesFichier;
    AtomicInteger i = new AtomicInteger();
    @Autowired
    ProxyRetry proxyRetry;

    @Override
    public void beforeStep(StepExecution stepExecution) {
        ExecutionContext executionContext = stepExecution
                .getJobExecution()
                .getExecutionContext();
        this.lignesFichier = (List<LigneFichierDto>) executionContext.get("lignes");
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        return null;
    }

    @Override
    public LigneFichierDto read() throws Exception {
        LigneFichierDto ligne = null;
        if (i.intValue() < this.lignesFichier.size()) {
            ligne = this.lignesFichier.get(i.getAndIncrement());
        }
        return ligne;
    }
}
