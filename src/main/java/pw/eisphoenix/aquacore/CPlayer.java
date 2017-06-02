package pw.eisphoenix.aquacore;

import pw.eisphoenix.aquacore.permission.Rank;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Year: 2017
 *
 * @author Eisphoenix
 */
@Entity(value = "players", noClassnameStored = true)
public final class CPlayer {
    @Id
    private ObjectId id;
    @Indexed(options = @IndexOptions(unique = true))
    private UUID uuid;
    @Indexed
    private String actualUsername;
    private List<String> userNames = new ArrayList<>();
    @Reference
    private Rank rank;

    public CPlayer() {
        userNames.add(getActualUsername());
    }

    public CPlayer(final String actualUsername, final UUID uuid, final Rank rank) {
        this.actualUsername = actualUsername;
        this.userNames.add(actualUsername);
        this.uuid = uuid;
        this.rank = rank;
    }

    public final String getActualUsername() {
        return actualUsername;
    }

    public final void setActualUsername(final String actualUsername) {
        this.actualUsername = actualUsername;
    }

    public final List<String> getUserNames() {
        return userNames;
    }

    public final UUID getUuid() {
        return uuid;
    }

    public final void setUuid(final UUID uuid) {
        this.uuid = uuid;
    }

    public final Rank getRank() {
        return rank;
    }

    public final void setRank(final Rank rank) {
        this.rank = rank;
    }
}
