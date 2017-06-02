package pw.eisphoenix.aquacore.dbstore;

import pw.eisphoenix.aquacore.ban.BanReason;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.dao.BasicDAO;

/**
 * Year: 2017
 *
 * @author Eisphoenix
 */
public final class BanReasonDAO extends BasicDAO<BanReason, String> {
    public BanReasonDAO(final Class<BanReason> entityClass, final Datastore ds) {
        super(entityClass, ds);
    }
}