package cz.cuni.matfyz.core.schema;

import cz.cuni.matfyz.core.category.Morphism;
import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.core.serialization.FromJSONLoaderBase;
import cz.cuni.matfyz.core.serialization.Identified;
import cz.cuni.matfyz.core.serialization.JSONConvertible;
import cz.cuni.matfyz.core.serialization.ToJSONConverterBase;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author pavel.koupil
 */
public class SchemaMorphism implements Serializable, Morphism, JSONConvertible, Identified<Signature> {
    
    private Signature signature;
    private SchemaObject dom;
    private SchemaObject cod;
    private Min min;
    private Max max;

    public static Min combineMin(Min min1, Min min2) {
        return (min1 == Min.ONE && min2 == Min.ONE) ? Min.ONE : Min.ZERO;
    }

    public static Max combineMax(Max max1, Max max2) {
        return (max1 == Max.ONE && max2 == Max.ONE) ? Max.ONE : Max.STAR;
    }

    private SchemaCategory category;

    /*
    public static SchemaMorphism dual(SchemaMorphism morphism) {
        return SchemaMorphism.dual(morphism, 1, 1);
    }
    */

    /*
    public SchemaMorphism createDual(Min min, Max max) {
        SchemaMorphism result = new SchemaMorphism(signature.dual(), cod, dom, min, max);
        return result;
    }
    */

    //private SchemaMorphism(Signature signature, SchemaObject dom, SchemaObject cod, Min min, Max max)
    private SchemaMorphism(SchemaObject dom, SchemaObject cod) {
        //this.signature = signature;
        this.dom = dom;
        this.cod = cod;
        //this.min = min;
        //this.max = max;
    }

    public void setCategory(SchemaCategory category) {
        this.category = category;
    }

    @Override
    public SchemaObject dom() {
        return dom;
    }

    @Override
    public SchemaObject cod() {
        return cod;
    }

    @Override
    public Min min() {
        return min;
    }

    @Override
    public Max max() {
        return max;
    }

    public boolean isArray() {
        return max == Max.STAR;
    }

    public boolean isBase() {
        return signature.isBase();
    }

    @Override
    public SchemaMorphism dual() {
        return category.dual(signature);
    }

    @Override
    public Signature signature() {
        return signature;
    }

    @Override
    public Signature identifier() {
        return signature;
    }

    @Override
    public JSONObject toJSON() {
        return new Converter().toJSON(this);
    }

    public static class Converter extends ToJSONConverterBase<SchemaMorphism> {

        @Override
        protected JSONObject innerToJSON(SchemaMorphism object) throws JSONException {
            var output = new JSONObject();

            output.put("signature", object.signature.toJSON());
            output.put("domIdentifier", object.dom.identifier().toJSON());
            output.put("codIdentifier", object.cod.identifier().toJSON());
            output.put("min", object.min());
            output.put("max", object.max());

            return output;
        }

    }

    public static class Builder extends FromJSONLoaderBase<SchemaMorphism> {

        //private final UniqueContext<SchemaObject, Key> context;

        /*
        public Builder(UniqueContext<SchemaObject, Key> context) {
            //this.context = context;
        }
        */

        public SchemaMorphism fromJSON(SchemaObject dom, SchemaObject cod, JSONObject jsonObject) {
            var morphism = new SchemaMorphism(dom, cod);
            loadFromJSON(morphism, jsonObject);
            return morphism;
        }

        public SchemaMorphism fromJSON(SchemaObject dom, SchemaObject cod, String jsonValue) {
            var morphism = new SchemaMorphism(dom, cod);
            loadFromJSON(morphism, jsonValue);
            return morphism;
        }

        @Override
        protected void innerLoadFromJSON(SchemaMorphism morphism, JSONObject jsonObject) throws JSONException {
            morphism.signature = new Signature.Builder().fromJSON(jsonObject.getJSONObject("signature"));

            // var domKey = new Key.Builder().fromJSON(jsonObject.getJSONObject("domIdentifier"));
            // SchemaObject dom = context.getUniqueObject(domKey);

            // var codKey = new Key.Builder().fromJSON(jsonObject.getJSONObject("codIdentifier"));
            // SchemaObject cod = context.getUniqueObject(codKey);

            morphism.min = Min.valueOf(jsonObject.getString("min"));
            morphism.max = Max.valueOf(jsonObject.getString("max"));

            //return new SchemaMorphism(signature, dom, cod, min, max);

        }

        public SchemaMorphism fromArguments(Signature signature, SchemaObject dom, SchemaObject cod, Min min, Max max) {
            var morphism = new SchemaMorphism(dom, cod);
            morphism.signature = signature;
            morphism.min = min;
            morphism.max = max;
            return morphism;
        }

        public SchemaMorphism fromDual(SchemaMorphism dualMorphism, Min min, Max max) {
            var dom = dualMorphism.cod;
            var cod = dualMorphism.dom;
            return fromArguments(dualMorphism.signature.dual(), dom, cod, min, max);
        }

    }
}
