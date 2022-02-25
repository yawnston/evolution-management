package cz.cuni.matfyz.wrapperMongodb;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

/**
 *
 * @author jachymb.bartik
 */
public class MongoDBDatabaseProvider implements DatabaseProvider
{
    private String host;
    private String port;
    private String database;
    private String username;
    private String password;

    private MongoDatabase databaseInstance;

    public MongoDBDatabaseProvider(String host, String port, String database, String username, String password)
    {
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
    }

    public MongoDatabase getDatabase()
    {
        if (databaseInstance == null)
            buildDatabase();

        return databaseInstance;
    }

    public void buildDatabase()
    {
        this.databaseInstance = createDatabaseFromCredentials(host, port, database, username, password);
    }

    private static MongoDatabase createDatabaseFromCredentials(String host, String port, String database, String username, String password)
    {
        var connectionBuilder = new StringBuilder();
        var connectionString = connectionBuilder
            .append("mongodb://")
            .append(username)
            .append(":")
            .append(password)
            .append("@")
            .append(host)
            .append(":")
            .append(port)
            .append("/")
            .append(database)
            .toString();

        var client = MongoClients.create(connectionString);
        return client.getDatabase(database);
    }

    public void executeScript(String pathToFile) throws Exception
    {
        String beforePasswordString = new StringBuilder()
            .append("mongo --username ")
            .append(username)
            .append(" --password ").toString();

        String afterPasswordString = new StringBuilder()
            .append(" ")
            .append(host)
            .append(":")
            .append(port)
            .append("/")
            .append(database)
            .append(" ")
            .append(pathToFile)
            .toString();

        System.out.println("Executing: " + beforePasswordString + "********" + afterPasswordString);

        String commandString = beforePasswordString + password + afterPasswordString;
        Runtime runtime = Runtime.getRuntime();
        Process process = runtime.exec(commandString);
        process.waitFor();

        BufferedReader bufferReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = bufferReader.readLine()) != null)
            System.out.println(line);
    }
}