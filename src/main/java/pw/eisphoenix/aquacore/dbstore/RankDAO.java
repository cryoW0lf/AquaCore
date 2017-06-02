package pw.eisphoenix.aquacore.dbstore;

import pw.eisphoenix.aquacore.permission.Rank;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.dao.BasicDAO;

/**
 * Year: 2017
 *
 * @author Eisphoenix
 */
public final class RankDAO extends BasicDAO<Rank, String> {
    public RankDAO(final Class<Rank> entityClass, final Datastore ds) {
        super(entityClass, ds);
    }
}
