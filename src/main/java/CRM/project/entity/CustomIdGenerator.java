package CRM.project.entity;

import CRM.project.utils.ReportDataConverter;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;

import java.io.Serializable;
import java.util.UUID;

public class CustomIdGenerator implements IdentifierGenerator {
    private static final String PREFIX = "CRM-";
    private static final int TOTAL_LENGTH = 10;

    @Override
    public Serializable generate(SharedSessionContractImplementor session, Object obj) {
        String uuid = UUID.randomUUID().toString(); // Generate UUID
        String customId = PREFIX + uuid;

        if (customId.length() > TOTAL_LENGTH) {
            customId = customId.substring(0, TOTAL_LENGTH);
        } else if (customId.length() < TOTAL_LENGTH) {
            customId = String.format("%-" + TOTAL_LENGTH + "s", customId).replace(' ', '0'); // Pad with zeros
        }

        return customId;
    }
}
