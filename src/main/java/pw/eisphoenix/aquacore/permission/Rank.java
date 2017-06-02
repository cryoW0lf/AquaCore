package pw.eisphoenix.aquacore.permission;

import pw.eisphoenix.aquacore.dependency.DependencyInjector;
import pw.eisphoenix.aquacore.dependency.Inject;
import pw.eisphoenix.aquacore.service.PermissionService;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Year: 2017
 *
 * @author Eisphoenix
 */
@Entity(value = "ranks", noClassnameStored = true)
public class Rank {
    @Id
    private ObjectId id;
    @Indexed(options = @IndexOptions(unique = true))
    private String name;
    private int priority;
    private String displayName;
    private String prefix;
    private List<String> inherits;
    private List<String> permissions;
    private transient boolean dirty = true;
    private transient List<String> calculatedPermissions = new ArrayList<>();
    private transient List<String> finalPermissions = new LinkedList<>();
    @Inject
    private transient PermissionService permissionService;

    public Rank() {
        DependencyInjector.inject(this);
    }

    public Rank(final String name, final int priority, final String displayName, final String prefix,
                final List<String> inherits, List<String> permissions) {
        this();
        this.name = name;
        this.priority = priority;
        this.displayName = displayName;
        this.prefix = prefix;
        this.inherits = inherits;
        this.permissions = permissions;
        recalculatePermissions();
    }

    public final String getName() {
        return name;
    }

    public final int getPriority() {
        return priority;
    }

    public final String getDisplayName() {
        return displayName;
    }

    public final String getPrefix() {
        return prefix;
    }

    public final List<String> getInherits() {
        return inherits;
    }

    public final List<String> getPermissions() {
        return permissions;
    }

    public final List<String> getCalculatedPermissions() {
        return calculatedPermissions;
    }

    public final List<String> getFinalPermissions() {
        return finalPermissions;
    }

    @PostLoad
    public final void recalculatePermissions() {
        if (!dirty) {
            return;
        }
        dirty = false;
        final Rank[] ranks = new Rank[2];
        inherits.sort((o1, o2) -> {
            ranks[0] = permissionService.getRank(o1);
            ranks[1] = permissionService.getRank(o2);
            if (ranks[0] == null) {
                    return ranks[1] == null ? 0 : -1;
            }
            if (ranks[1] == null) {
                return 1;
            }
            return ranks[0].getPriority() > ranks[1].getPriority() ? 1 :
                    ranks[0].getPriority() < ranks[1].getPriority() ? -1 : 0;
        });
        calculatedPermissions.clear();
        Rank rank;
        for (final String rankName : inherits) {
            rank = permissionService.getRank(rankName);
            if (rank == null) {
                continue;
            }
            rank.recalculatePermissions();
            calculatedPermissions.addAll(rank.getCalculatedPermissions());
        }
        calculatedPermissions.addAll(permissions);
        recalculateFinalPermissions();
    }

    private void recalculateFinalPermissions() {
        calculatedPermissions.sort(((o1, o2) -> o1.startsWith("-") ? o2.startsWith("-") ? 0 : -1 : 1));
        finalPermissions.clear();
        for (final String perm : calculatedPermissions) {
            if (perm == null || perm.isEmpty()) {
                continue;
            }
            if (perm.startsWith("-")) {
                finalPermissions.remove(perm);
            } else {
                finalPermissions.add(perm);
            }
        }
    }



    public boolean hasPermissions(final String permission) {
        for (final String perm : finalPermissions) {
            if (Pattern.matches(perm, permission)) {
                //cache.put(inName, true);
                return true;
            }
        }
        //cache.put(inName, false);
        return false;
    }
}
