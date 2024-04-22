package fr.abes.item.core.configuration;

import org.hibernate.dialect.OracleDialect;

public class OracleCustomDriver extends OracleDialect {
    public String getQuerySequencesString() {
        return "select * from all_sequences";
    }

    public SequenceInformationExtractor getSequenceInformationExtractor() {
        return SequenceInformationExtractor.INSTANCE;
    }
}
