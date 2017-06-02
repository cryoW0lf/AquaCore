package pw.eisphoenix.aquacore.chat;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.IndexOptions;
import org.mongodb.morphia.annotations.Indexed;

import java.util.UUID;

/**
 * Year: 2017
 *
 * @author Eisphoenix
 */
@Entity(noClassnameStored = true, value = "mutes")
public final class Mute {
    @Id
    private ObjectId id;
    @Indexed(options = @IndexOptions(unique = true))
    private UUID uuid;
    private UUID player;
    private UUID source;
    private long timestamp;
    private long length;
    private String reason;
    private boolean valid = true;
    private String notes = "";

    public Mute() {
    }

    public Mute(final UUID player, final UUID source, final long length, final String reason) {
        this.uuid = UUID.randomUUID();
        this.player = player;
        this.source = source;
        this.timestamp = System.currentTimeMillis();
        this.length = length;
        this.reason = reason;
    }

    public final UUID getUuid() {
        return uuid;
    }

    public final UUID getPlayer() {
        return player;
    }

    public final UUID getSource() {
        return source;
    }

    public final long getTimestamp() {
        return timestamp;
    }

    public final long getLength() {
        return length;
    }

    public final String getReason() {
        return reason;
    }

    public final boolean getValid() {
        return valid;
    }

    public final void setValid(final boolean valid) {
        this.valid = valid;
    }

    public final String getNotes() {
        return notes;
    }

    public final void setNotes(final String notes) {
        this.notes = notes;
    }

    public final boolean isCurrent() {
        return valid && (length == 0 || System.currentTimeMillis() <= timestamp + length);
    }
}
