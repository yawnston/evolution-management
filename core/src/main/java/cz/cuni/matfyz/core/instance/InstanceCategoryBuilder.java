package cz.cuni.matfyz.core.instance;

import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.core.schema.Key;
import cz.cuni.matfyz.core.schema.SchemaCategory;
import cz.cuni.matfyz.core.schema.SchemaMorphism;
import cz.cuni.matfyz.core.schema.SchemaObject;

import java.util.Map;
import java.util.TreeMap;

/**
 * @author jachymb.bartik
 */
public class InstanceCategoryBuilder {

    private SchemaCategory schemaCategory;
    private InstanceCategory result;
    
    private Map<Key, InstanceObject> objects = new TreeMap<>();
    private Map<Signature, InstanceMorphism> morphisms = new TreeMap<>();
    
    public InstanceCategoryBuilder setSchemaCategory(SchemaCategory schemaCategory) {
        this.schemaCategory = schemaCategory;
        return this;
    }
    
    public InstanceCategory build() {
        result = new InstanceCategory(schemaCategory, objects, morphisms);
        
        for (SchemaObject schemaObject : schemaCategory.allObjects()) {
            InstanceObject instanceObject = createObject(schemaObject);
            objects.put(instanceObject.key(), instanceObject);
        }

        // The base moprhisms must be created first because the composite ones use them.
        var baseMorphisms = schemaCategory.allMorphisms().stream().filter(SchemaMorphism::isBase).toList();
        for (SchemaMorphism schemaMorphism : baseMorphisms) {
            InstanceMorphism instanceMorphism = createMorphism(schemaMorphism);
            morphisms.put(schemaMorphism.signature(), instanceMorphism);
        }

        var compositeMorphisms = schemaCategory.allMorphisms().stream().filter(morphism -> !morphism.isBase()).toList();
        for (SchemaMorphism schemaMorphism : compositeMorphisms) {
            InstanceMorphism instanceMorphism = createMorphism(schemaMorphism);
            morphisms.put(schemaMorphism.signature(), instanceMorphism);
        }
        
        return result;
    }
    
    private InstanceObject createObject(SchemaObject schemaObject) {
        return new InstanceObject(schemaObject);
    }
    
    private InstanceMorphism createMorphism(SchemaMorphism schemaMorphism) {
        InstanceObject domain = objects.get(schemaMorphism.dom().key());
        InstanceObject codomain = objects.get(schemaMorphism.cod().key());
        
        return new InstanceMorphism(schemaMorphism, domain, codomain, result);
    }

}
