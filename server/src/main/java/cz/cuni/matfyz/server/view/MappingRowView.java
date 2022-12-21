package cz.cuni.matfyz.server.view;

import cz.cuni.matfyz.core.category.Morphism;
import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.core.instance.InstanceObject;
import cz.cuni.matfyz.core.instance.MappingRow;
import cz.cuni.matfyz.core.instance.InstanceMorphism;
import cz.cuni.matfyz.core.schema.SchemaMorphism;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.io.Serializable;
import java.util.SortedSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jachym.bartik
 */
public class MappingRowView {
    public DomainRowView domainRow;
    public DomainRowView codomainRow;

    public MappingRowView(MappingRow mappingRow) {
        this.domainRow = new DomainRowView(mappingRow.domainRow());
        this.codomainRow = new DomainRowView(mappingRow.codomainRow());
    }
}
