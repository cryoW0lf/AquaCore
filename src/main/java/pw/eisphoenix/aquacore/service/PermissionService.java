package pw.eisphoenix.aquacore.service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import pw.eisphoenix.aquacore.AquaCore;
import pw.eisphoenix.aquacore.dbstore.RankDAO;
import pw.eisphoenix.aquacore.dependency.Inject;
import pw.eisphoenix.aquacore.dependency.Injectable;
import pw.eisphoenix.aquacore.dependency.InjectionHook;
import pw.eisphoenix.aquacore.permission.Rank;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static pw.eisphoenix.aquacore.AquaCore.EXECUTOR_SERVICE;

/**
 * Year: 2017
 *
 * @author Eisphoenix
 */
@Injectable
public final class PermissionService implements InjectionHook {
    @Inject
    private DatabaseService databaseService;
    @Inject
    private ObserverService observerService;
    private RankDAO rankStore;

    @Override
    public final void postInjection() {
        rankStore = new RankDAO(Rank.class, databaseService.getDatastore());
        CompletableFuture.supplyAsync(() -> rankStore.count() < 1, EXECUTOR_SERVICE).thenAccept(
                result -> {
                    if (result) {
                        registerDefaultRanks();
                    }
                }
        );
    }

    public final CompletableFuture<Rank> getRank() {
        return getRank("default");
    }

    public final CompletableFuture<Rank> getRank(final String name) {
        return CompletableFuture.supplyAsync(() -> rankStore.findOne("name", name));
    }

    public final Rank getRankSync(final String name) {
        return rankStore.findOne("name", name);
    }

    public final CompletableFuture<List<Rank>> getRanks() {
        return CompletableFuture.supplyAsync(() -> rankStore.find().asList());
    }

    private void registerDefaultRanks() {
        final JsonObject rootObject = AquaCore.JSON_PARSER.parse(new InputStreamReader(
                getClass().getResourceAsStream("/ranks.json")
        )).getAsJsonObject();
        JsonObject rankObject;
        for (final Map.Entry<String, JsonElement> entry : rootObject.entrySet()) {
            rankObject = entry.getValue().getAsJsonObject();
            //noinspection unchecked
            rankStore.save(new Rank(entry.getKey(),
                    rankObject.getAsJsonPrimitive("priority").getAsInt(),
                    rankObject.getAsJsonPrimitive("displayName").getAsString(),
                    rankObject.getAsJsonPrimitive("prefix").getAsString(),
                    AquaCore.GSON.fromJson(rankObject.get("inherits"), ArrayList.class),
                    AquaCore.GSON.fromJson(rankObject.get("permissions"), ArrayList.class)
            ));
        }
    }
}
