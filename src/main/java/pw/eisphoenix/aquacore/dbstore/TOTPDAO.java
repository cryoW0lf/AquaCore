package pw.eisphoenix.aquacore.dbstore;

import pw.eisphoenix.aquacore.totp.TOTPEntry;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.dao.BasicDAO;

/**
 * Year: 2017
 *
 * @author Eisphoenix
 */
public final class TOTPDAO extends BasicDAO<TOTPEntry, String> {
    public TOTPDAO(final Class<TOTPEntry> entityClass, final Datastore ds) {
        super(entityClass, ds);
    }


}
