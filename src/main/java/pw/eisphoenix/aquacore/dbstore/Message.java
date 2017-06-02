package pw.eisphoenix.aquacore.dbstore;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.IndexOptions;
import org.mongodb.morphia.annotations.Indexed;

/**
 * Year: 2017
 *
 * @author Eisphoenix
 */
@Entity(value = "messages", noClassnameStored = true)
public final class Message {
    @Id
    private ObjectId id;
    @Indexed(options = @IndexOptions(unique = true))
    private String key;
    private String message;

    public Message() {
        key = "MESSAGE INIT";
        message = "MESSAGE INIT";
    }

    public Message(final String key, final String message) {
        this.key = key;
        this.message = message;
    }

    public final String getKey() {
        return key;
    }

    public final String getMessage() {
        return message;
    }
}
