package cz.cuni.matfyz.server.view;

import cz.cuni.matfyz.core.category.Morphism;
import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.core.instance.InstanceObject;
import cz.cuni.matfyz.core.instance.MappingRow;
import cz.cuni.matfyz.core.instance.InstanceMorphism;
import cz.cuni.matfyz.core.instance.IdWithValues;
import cz.cuni.matfyz.core.schema.SchemaMorphism;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.io.Serializable;
import java.util.SortedSet;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jachym.bartik
 */
public class IdWithValuesView {
    public Map<Signature, String> tuples;

    public IdWithValuesView(IdWithValues idWithValues) {
        this.tuples = idWithValues.tuples;
    }
}
