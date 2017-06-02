package pw.eisphoenix.aquacore.service;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import pw.eisphoenix.aquacore.AquaCore;
import pw.eisphoenix.aquacore.Configuration;
import pw.eisphoenix.aquacore.dependency.Injectable;

import java.util.Collections;

/**
 * Year: 2017
 *
 * @author Eisphoenix
 */
@Injectable
public final class DatabaseService {
    private final MongoClient mongoClient;
    private final Morphia morphia;
    private final Datastore datastore;

    public DatabaseService() {
        final Configuration configuration = AquaCore.getConfiguration();
        mongoClient = new MongoClient(new ServerAddress(configuration.DBHOST, configuration.DBPORT),
                Collections.singletonList(
                        MongoCredential.createCredential(configuration.DBUSER, configuration.DBAUTHDB,
                                configuration.DBPASS.toCharArray())
                )
        );
        morphia = new Morphia();
        morphia.getMapper().getOptions().setStoreEmpties(true);
        morphia.getMapper().getOptions().setStoreNulls(true);
        morphia.mapPackage("pw.eisphoenix.aquacore");
        mongoClient.getDatabase(AquaCore.getConfiguration().DBNAME).getCollection("messages").drop();
        datastore = morphia.createDatastore(mongoClient, AquaCore.getConfiguration().DBNAME);
        datastore.ensureIndexes();
    }

    public final MongoDatabase getDatabase() {
        return mongoClient.getDatabase(AquaCore.getConfiguration().DBNAME);
    }

    public final Datastore getDatastore() {
        return datastore;
    }
}
