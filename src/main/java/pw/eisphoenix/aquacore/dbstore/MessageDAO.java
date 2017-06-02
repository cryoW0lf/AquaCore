package pw.eisphoenix.aquacore.dbstore;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.dao.BasicDAO;

/**
 * Year: 2017
 *
 * @author Eisphoenix
 */
public final class MessageDAO extends BasicDAO<Message, String> {
    public MessageDAO(final Class<Message> entityClass, final Datastore ds) {
        super(entityClass, ds);
    }
}
