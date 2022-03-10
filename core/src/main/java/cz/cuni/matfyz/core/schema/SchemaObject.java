package cz.cuni.matfyz.core.schema;

import cz.cuni.matfyz.core.category.CategoricalObject;
import cz.cuni.matfyz.core.serialization.FromJSONBuilderBase;
import cz.cuni.matfyz.core.serialization.Identified;
import cz.cuni.matfyz.core.serialization.ToJSONConverterBase;
import cz.cuni.matfyz.core.serialization.JSONConvertible;

import java.util.Objects;
import java.util.Set;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
import java.util.TreeSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author pavel.koupil, jachymb.bartik
 */
public class SchemaObject implements CategoricalObject, JSONConvertible, Identified<Key>
{
	//private static final Logger LOGGER = LoggerFactory.getLogger(SchemaObject.class);
	
	private final Key key; // Identifies the object, in the paper it's a number >= 100
	private final String label;
	private final Id superId; // Should be a union of all ids (super key).
	private final Set<Id> ids; // Each id is a set of signatures so that the correspondig set of attributes can unambiguosly identify this object (candidate key).

    /*
	public SchemaObject(Key key)
    {
		this(key, "", new Id(), new TreeSet<>());
	}

	public SchemaObject(Key key, String label)
    {
		this(key, label, new Id(), new TreeSet<>());
	}
    */

	public SchemaObject(Key key, String label, Id superId, Set<Id> ids)
    {
		//LOGGER.debug("Creating object...");
		this.key = key;
		this.label = label;
		this.superId = superId;
		this.ids = ids;
	}

    public Key key()
    {
        return key;
    }

	@Override
	public Key identifier()
	{
		return key;
	}

	@Override
	public int objectId()
    {
		return key.getValue();
	}

	public String label() {
		return label;
	}

	public Id superId() {
		return superId;
	}

	public Set<Id> ids() {
		return ids;
	}

	@Override
	public int compareTo(CategoricalObject categoricalObject)
    {
        return objectId() - categoricalObject.objectId();
	}

	@Override
	public boolean equals(Object obj)
    {
        return obj instanceof CategoricalObject categoricalObject && compareTo(categoricalObject) == 0;
	}

    /**
     * Auto-generated, constants doesn't have any special meaning.
     * @return 
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + Objects.hashCode(this.key);
        return hash;
    }

	@Override
	public String toString()
    {
        throw new UnsupportedOperationException();
	}

	@Override
    public JSONObject toJSON() {
        return new Converter().toJSON(this);
    }

    public static class Converter extends ToJSONConverterBase<SchemaObject> {

        @Override
        protected JSONObject _toJSON(SchemaObject object) throws JSONException {
            var output = new JSONObject();

			output.put("key", object.key.toJSON());
			output.put("label", object.label);
			output.put("superId", object.superId.toJSON());

			var ids = new JSONArray(object.ids.stream().map(id -> id.toJSON()).toList());
			output.put("ids", ids);
            
            return output;
        }

	}

	public static class Builder extends FromJSONBuilderBase<SchemaObject> {

        @Override
        protected SchemaObject _fromJSON(JSONObject jsonObject) throws JSONException {
            var key = new Key.Builder().fromJSON(jsonObject.getJSONObject("key"));
			var label = jsonObject.getString("label");
			var superId = new Id.Builder().fromJSON(jsonObject.getJSONObject("superId"));

            var idsArray = jsonObject.getJSONArray("ids");
            var ids = new TreeSet<Id>();
            var builder = new Id.Builder();
            for (int i = 0; i < idsArray.length(); i++)
                ids.add(builder.fromJSON(idsArray.getJSONObject(i)));

            return new SchemaObject(key, label, superId, ids);
        }

    }
}
