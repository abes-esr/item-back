package fr.abes.item.batch.traitement.traiterlignesfichierchunk;

import fr.abes.item.batch.traitement.ProxyRetry;
import fr.abes.item.batch.traitement.model.LigneFichierDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReader;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class LignesFichierReader implements ItemReader<LigneFichierDto>, StepExecutionListener {

    private List<LigneFichierDto> lignesFichier;
    AtomicInteger i = new AtomicInteger();
    private final ProxyRetry proxyRetry;

    public LignesFichierReader(ProxyRetry proxyRetry) {
        this.proxyRetry = proxyRetry;
    }

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
    public LigneFichierDto read() {
        LigneFichierDto ligne = null;
        if (i.intValue() < this.lignesFichier.size()) {
            ligne = this.lignesFichier.get(i.getAndIncrement());
        }
        return ligne;
    }
}
