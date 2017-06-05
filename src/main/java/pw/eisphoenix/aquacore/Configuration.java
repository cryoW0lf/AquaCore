package pw.eisphoenix.aquacore;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Year: 2017
 *
 * @author Eisphoenix
 */
public final class Configuration {
    public String DBHOST = "127.0.0.1";
    public int DBPORT = 27017;
    public String DBUSER = "";
    public String DBAUTHDB = "";
    public String DBPASS = "";
    public String DBNAME = "test";

    private Configuration() {
    }

    public static Configuration load() {
        final File file = new File(AquaCore.getInstance().getDataFolder(), "config.json");
        if (!file.exists()) {
            return new Configuration();
        }
        try {
            final Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
            final Configuration configuration = AquaCore.PRETTY_GSON.fromJson(reader, Configuration.class);
            reader.close();
            return configuration;
        } catch (final IOException e) {
            e.printStackTrace();
            return new Configuration();
        }
    }

    public final void save() throws IOException {
        final File parent = AquaCore.getInstance().getDataFolder();
        if (!(parent.exists())) {
            parent.mkdirs();
        }
        final File file = new File(parent, "config.json");
        if (!file.exists()) {
            file.createNewFile();
        }
        final Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
        AquaCore.PRETTY_GSON.toJson(this, writer);
        writer.close();
    }
}
