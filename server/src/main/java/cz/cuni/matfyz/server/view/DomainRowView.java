package cz.cuni.matfyz.server.view;

import cz.cuni.matfyz.core.category.Morphism;
import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.core.instance.InstanceObject;
import cz.cuni.matfyz.core.instance.MappingRow;
import cz.cuni.matfyz.core.instance.InstanceMorphism;
import cz.cuni.matfyz.core.schema.SchemaMorphism;
import cz.cuni.matfyz.core.instance.DomainRow;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.io.Serializable;
import java.util.SortedSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jachym.bartik
 */
public class DomainRowView {
    public IdWithValuesView superId;
    public Set<Integer> technicalIds;

    public DomainRowView(DomainRow domainRow) {
        this.superId = new IdWithValuesView(domainRow.superId);
        this.technicalIds = domainRow.technicalIds;
    }
}
