package fr.abes.item.core.configuration;

import org.hibernate.tool.schema.extract.internal.SequenceInformationExtractorOracleDatabaseImpl;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SequenceInformationExtractor extends SequenceInformationExtractorOracleDatabaseImpl {
    public static final SequenceInformationExtractor INSTANCE = new SequenceInformationExtractor();

    private static final BigDecimal LONG_MIN_VALUE_AS_DECIMAL = BigDecimal.valueOf(Long.MIN_VALUE);

    private static final BigDecimal LONG_MAX_VALUE_AS_DECIMAL = BigDecimal.valueOf(Long.MAX_VALUE);

    @Override
    protected String sequenceMaxValueColumn() {
        return "max_value";
    }

    @Override
    public Long resultSetMinValue(final ResultSet resultSet) throws SQLException {
        final BigDecimal asDecimal = resultSet.getBigDecimal(this.sequenceMinValueColumn());
        if (asDecimal.compareTo(SequenceInformationExtractor.LONG_MIN_VALUE_AS_DECIMAL) < 0) {
            return Long.MIN_VALUE;
        }
        return asDecimal.longValue();
    }

    @Override
    public Long resultSetMaxValue(final ResultSet resultSet) throws SQLException {
        final BigDecimal asDecimal = resultSet.getBigDecimal(this.sequenceMaxValueColumn());
        if (asDecimal.compareTo(SequenceInformationExtractor.LONG_MAX_VALUE_AS_DECIMAL) > 0) {
            return Long.MAX_VALUE;
        }
        return asDecimal.longValue();
    }
}
