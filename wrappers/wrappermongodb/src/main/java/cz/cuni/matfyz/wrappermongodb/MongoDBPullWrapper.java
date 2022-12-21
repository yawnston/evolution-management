package cz.cuni.matfyz.wrappermongodb;

import cz.cuni.matfyz.abstractwrappers.AbstractPullWrapper;
import cz.cuni.matfyz.abstractwrappers.PullWrapperOptions;
import cz.cuni.matfyz.core.mapping.AccessPath;
import cz.cuni.matfyz.core.mapping.ComplexProperty;
import cz.cuni.matfyz.core.mapping.DynamicName;
import cz.cuni.matfyz.core.mapping.Name;
import cz.cuni.matfyz.core.mapping.SimpleProperty;
import cz.cuni.matfyz.core.mapping.StaticName;
import cz.cuni.matfyz.core.record.ComplexRecord;
import cz.cuni.matfyz.core.record.ForestOfRecords;
import cz.cuni.matfyz.core.record.RecordName;
import cz.cuni.matfyz.core.record.RootRecord;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.List;

import com.mongodb.client.MongoCollection;
import org.bson.Document;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * @author jachymb.bartik
 */
public class MongoDBPullWrapper implements AbstractPullWrapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoDBPullWrapper.class);

    private DatabaseProvider databaseProvider;
    
    public void injectDatabaseProvider(DatabaseProvider databaseProvider) {
        this.databaseProvider = databaseProvider;
    }

    private Iterator<Document> getDocumentIterator(PullWrapperOptions options) {
        if (options.query != null) {
            LOGGER.error("MongoDB command: " + options.query);
            Pattern pattern = Pattern.compile("db\\.(.*)\\.aggregate\\((.*)\\)");
            Matcher matcher = pattern.matcher(options.query);
            matcher.find();
            String collection = matcher.group(1);
            String aggregationCommands = matcher.group(2).replaceAll("\\\\\"", "\"");
            LOGGER.error("MongoDB collection: " + collection);
            LOGGER.error("MongoDB commands: " + aggregationCommands);

            try {
                var jsonArray = new JSONArray(aggregationCommands);
                List<Document> list = new ArrayList<>();
                for (int i = 0; i < jsonArray.length(); i++) {
                    Object item = jsonArray.get(i);
                    Document document = Document.parse(item.toString());
                    list.add(document);
                }

                return databaseProvider.getDatabase().getCollection(collection).aggregate(list).iterator();
            } catch (JSONException e) {
                LOGGER.error("JSON Exception: ", e);
            }
        }
        String kindName = options.getKindName();
        var database = databaseProvider.getDatabase();
        var find = database.getCollection(kindName).find();

        if (options.hasOffset())
            find = find.skip(options.getOffset());
        
        if (options.hasLimit())
            find = find.limit(options.getLimit());

        return find.iterator();
    }

    @Override
    public ForestOfRecords pullForest(ComplexProperty path, PullWrapperOptions options) throws Exception {
        var forest = new ForestOfRecords();
        var iterator = getDocumentIterator(options);
        
        while (iterator.hasNext()) {
            Document document = iterator.next();
            var rootRecord = new RootRecord();
            getDataFromDocument(rootRecord, document, path);
            forest.addRecord(rootRecord);
        }
        
        return forest;
    }

    private void getDataFromDocument(ComplexRecord parentRecord, Document document, ComplexProperty path) throws Exception {
        boolean hasSubpathWithDynamicName = false;
        
        for (AccessPath subpath : path.subpaths()) {
            if (subpath.name() instanceof StaticName staticName)
                getFieldFromObjectForSubpath(parentRecord, document, staticName.getStringName(), subpath);
            else
                hasSubpathWithDynamicName = true;
        }
        
        if (hasSubpathWithDynamicName)
            getDataFromDynamicFieldsOfObject(parentRecord, document, path);
    }
    
    private void getDataFromDynamicFieldsOfObject(ComplexRecord parentRecord, Document document, ComplexProperty path) throws Exception {
        // First we find all names that belong to the subpaths with non-dynamic names and also the subpath with the dynamic name.
        AccessPath subpathWithDynamicName = null;
        Set<String> otherSubpathNames = new TreeSet<>();
        
        for (AccessPath subpath : path.subpaths()) {
            if (subpath.name() instanceof StaticName staticName)
                otherSubpathNames.add(staticName.getStringName());
            else
                subpathWithDynamicName = subpath;
        }
        
        // For all keys in the object where the key is not a known static name do ...
        for (String key : document.keySet()) {
            if (!otherSubpathNames.contains(key))
                getFieldFromObjectForSubpath(parentRecord, document, key, subpathWithDynamicName);
        }
    }
    
    private void getFieldFromObjectForSubpath(ComplexRecord parentRecord, Document document, String key, AccessPath subpath) throws Exception {
        // TODO Check for the null values if necessary.
        /*
        if (document.isNull(key)) // Returns if the value is null or if the value doesn't exist.
            return;
        */
        
        var value = document.get(key);
        
        if (subpath instanceof ComplexProperty complexSubpath)
            getFieldFromObjectForComplexSubpath(parentRecord, key, value, complexSubpath);
        else if (subpath instanceof SimpleProperty simpleSubpath)
            getFieldFromObjectForSimpleSubpath(parentRecord, key, value, simpleSubpath);
    }

    private void getFieldFromObjectForComplexSubpath(ComplexRecord parentRecord, String key, Object value, ComplexProperty complexSubpath) throws Exception {
        if (value instanceof ArrayList<?> childArray) {
            for (int i = 0; i < childArray.size(); i++)
                if (childArray.get(i) instanceof Document childDocument)
                    addComplexValueToRecord(parentRecord, childDocument, key, complexSubpath);
        }
        else if (value instanceof Document childDocument)
            addComplexValueToRecord(parentRecord, childDocument, key, complexSubpath);
    }

    private void getFieldFromObjectForSimpleSubpath(ComplexRecord parentRecord, String key, Object value, SimpleProperty simpleSubpath) {
        if (value instanceof ArrayList<?> simpleArray) {
            var values = new ArrayList<String>();
            
            for (int i = 0; i < simpleArray.size(); i++)
                values.add(simpleArray.get(i).toString());
            
            parentRecord.addSimpleArrayRecord(toRecordName(simpleSubpath.name(), key), simpleSubpath.value().signature(), values);
        }
        else {
            RecordName recordName = toRecordName(simpleSubpath.name(), key);
            parentRecord.addSimpleValueRecord(recordName, simpleSubpath.value().signature(), value.toString());
        }
    }
    
    private void addComplexValueToRecord(ComplexRecord parentRecord, Document value, String key, ComplexProperty complexProperty) throws Exception {
        // If the path is an auxiliary property, we skip it and move all it's childrens' values to the parent node.
        // We do so by passing the parent record instead of creating a new one.
        if (complexProperty.isAuxiliary())
            getDataFromDocument(parentRecord, value, complexProperty);
        else {
            ComplexRecord childRecord = parentRecord.addComplexRecord(toRecordName(complexProperty.name(), key), complexProperty.signature());
            getDataFromDocument(childRecord, value, complexProperty);
        }
    }

    private RecordName toRecordName(Name name, String valueIfDynamic) {
        if (name instanceof DynamicName dynamicName)
            return dynamicName.toRecordName(valueIfDynamic);
        
        var staticName = (StaticName) name;
        return staticName.toRecordName();
    }

    public String readCollectionAsStringForTests(String selectAll) {
        var database = databaseProvider.getDatabase();
        String kindName = selectAll.substring("database.getCollection(\"".length(), selectAll.length() - "\");".length());
        MongoCollection<Document> collection = database.getCollection(kindName);
        Iterator<Document> iterator = collection.find().iterator();
        
        var output = new StringBuilder();
        while (iterator.hasNext())
            output.append(iterator.next().toString());

        return output.toString();
    }
}
