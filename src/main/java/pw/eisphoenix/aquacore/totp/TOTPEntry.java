package pw.eisphoenix.aquacore.totp;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.IndexOptions;
import org.mongodb.morphia.annotations.Indexed;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

/**
 * Year: 2017
 *
 * @author Eisphoenix
 */
@Entity(value = "totpEntries", noClassnameStored = true)
public final class TOTPEntry {
    @Id
    private ObjectId id;
    @Indexed(options = @IndexOptions(unique = true))
    private UUID uuid;
    private String key;

    public TOTPEntry(final UUID uuid, final String key) {
        this.uuid = uuid;
        this.key = key;
    }

    public TOTPEntry() {
    }

    public final UUID getUuid() {
        return uuid;
    }

    public final String getKey() {
        return new String(Base64.getDecoder().decode(key));
    }

    public final void setKey(final String key) {
        this.key = new String(Base64.getEncoder().encode(key.getBytes(StandardCharsets.UTF_8)));
    }
}
