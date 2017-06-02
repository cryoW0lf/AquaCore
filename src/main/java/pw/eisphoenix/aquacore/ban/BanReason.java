package pw.eisphoenix.aquacore.ban;

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
@Entity(value = "banReasons", noClassnameStored = true)
public final class BanReason {
    @Id
    private ObjectId id;
    @Indexed(options = @IndexOptions(unique = true))
    private String name;
    private String[] aliase;
    private String displayName;
    private int[] durations;
    private boolean permanent;

    public BanReason() {
        name = "";
        aliase = new String[0];
        displayName = "";
        durations = new int[0];
        permanent = false;
    }

    public BanReason(final String name, final String[] aliase, final String displayName, final int[] durations, final boolean permanent) {
        this.name = name;
        this.aliase = aliase;
        this.displayName = displayName;
        this.durations = durations;
        this.permanent = permanent;
    }

    public final String getName() {
        return name;
    }

    public final String[] getAliase() {
        return aliase;
    }

    public final String getDisplayName() {
        return displayName;
    }

    public final int getDurations(final int often) {
        if (durations.length > often)
            return durations[often];
        if (!permanent)
            return durations[durations.length - 1];
        return Integer.MAX_VALUE;
    }

    public final boolean getPermanent() {
        return permanent;
    }

    public final boolean isName(final String name) {
        if (name.equalsIgnoreCase(this.name) || displayName.equalsIgnoreCase(name)) {
            return true;
        }
        for (final String alias : aliase) {
            if (name.equalsIgnoreCase(alias)) {
                return true;
            }
        }
        return false;
    }
}
