package pw.eisphoenix.aquacore.dbstore;

import pw.eisphoenix.aquacore.ban.BanEntry;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.dao.BasicDAO;

/**
 * Year: 2017
 *
 * @author Eisphoenix
 */
public final class BanDAO extends BasicDAO<BanEntry, String> {
    public BanDAO(final Class<BanEntry> entityClass, final Datastore ds) {
        super(entityClass, ds);
    }


}
