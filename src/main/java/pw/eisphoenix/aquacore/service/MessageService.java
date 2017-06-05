package pw.eisphoenix.aquacore.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import pw.eisphoenix.aquacore.AquaCore;
import pw.eisphoenix.aquacore.dbstore.Message;
import pw.eisphoenix.aquacore.dbstore.MessageDAO;
import pw.eisphoenix.aquacore.dependency.Inject;
import pw.eisphoenix.aquacore.dependency.Injectable;
import pw.eisphoenix.aquacore.dependency.InjectionHook;

import java.io.InputStreamReader;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static pw.eisphoenix.aquacore.AquaCore.EXECUTOR_SERVICE;

/**
 * Year: 2017
 *
 * @author Eisphoenix
 */
@Injectable
public final class MessageService implements InjectionHook {
    @Inject
    private DatabaseService databaseService;
    private MessageDAO messageDAO;

    @Override
    public void postInjection() {
        messageDAO = new MessageDAO(Message.class, databaseService.getDatastore());
        CompletableFuture.supplyAsync(() -> messageDAO.count() < 1, EXECUTOR_SERVICE).thenAccept(result -> {
            if (result) {
                registerDefaultMessages();
            }
        });
    }

    public final CompletableFuture<String> getMessage(final String key) {
        return CompletableFuture.supplyAsync(() -> {
            final Message message = messageDAO.findOne(
                    databaseService.getDatastore().createQuery(Message.class).filter("key", key));
            if (message == null) {
                return "MESSAGE ERROR";
            }
            return message.getMessage();
        }, EXECUTOR_SERVICE);
    }

    private void registerMessage(final String key, final String value) {
        messageDAO.save(new Message(key, value));
    }

    private void registerDefaultMessages() {
        final JsonObject rootObject = AquaCore.JSON_PARSER.parse(new InputStreamReader(
                getClass().getResourceAsStream("/messages.json")
        )).getAsJsonObject();
        final StringBuilder[] builder = new StringBuilder[1];
        final boolean[] lineStart = new boolean[1];
        JsonElement messageEntry;
        JsonArray messageArray;
        for (final Map.Entry<String, JsonElement> entry : rootObject.entrySet()) {
            messageEntry = entry.getValue();
            if (messageEntry.isJsonPrimitive() && messageEntry.getAsJsonPrimitive().isString()) {
                registerMessage(entry.getKey(), messageEntry.getAsString());
            } else if (messageEntry.isJsonArray()) {
                messageArray = messageEntry.getAsJsonArray();
                builder[0] = new StringBuilder();
                lineStart[0] = true;
                messageArray.forEach(jsonElement -> {
                    if (jsonElement.isJsonPrimitive() && jsonElement.getAsJsonPrimitive().isString()) {
                        if (!lineStart[0]) {
                            builder[0].append("\n");
                        } else {
                            lineStart[0] = false;
                        }
                        builder[0].append(jsonElement.getAsString());
                    }
                });
                registerMessage(entry.getKey(), builder[0].toString());
            }
        }
    }

    public final CompletableFuture<String> getMessage(final String key, final MessageType messageType) {
        return getMessage("message." + messageType.name().toLowerCase()).thenCombine(getMessage(key), (message, text) ->
                message.replaceAll("%MESSAGE%", text)
        ).thenCombine(getMessage("prefix"), (message, prefix) ->
                message.replaceAll("%PPREFIX%", prefix)
        );
    }

    public enum MessageType {
        INFO, WARNING, ERROR
    }
}