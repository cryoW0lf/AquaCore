package pw.eisphoenix.aquacore.dbstore;

import pw.eisphoenix.aquacore.CPlayer;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.dao.BasicDAO;

/**
 * Year: 2017
 *
 * @author Eisphoenix
 */
public final class PlayerDAO extends BasicDAO<CPlayer, String> {
    public PlayerDAO(final Class<CPlayer> entityClass, final Datastore dataStore) {
        super(entityClass, dataStore);
    }
}
