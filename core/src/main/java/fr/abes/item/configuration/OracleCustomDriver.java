package fr.abes.item.configuration;

import org.hibernate.dialect.Oracle12cDialect;

public class OracleCustomDriver extends Oracle12cDialect {
    public String getQuerySequencesString() {
        return "select * from all_sequences";
    }

    public SequenceInformationExtractor getSequenceInformationExtractor() {
        return SequenceInformationExtractor.INSTANCE;
    }
}
