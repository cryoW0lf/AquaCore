package pw.eisphoenix.aquacore.dbstore;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.dao.BasicDAO;
import pw.eisphoenix.aquacore.chat.Mute;

/**
 * Year: 2017
 *
 * @author Eisphoenix
 */
public final class MuteDAO extends BasicDAO<Mute, String> {
    public MuteDAO(final Class<Mute> entityClass, final Datastore ds) {
        super(entityClass, ds);
    }


}
