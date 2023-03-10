package cz.cuni.matfyz.core.instance;

import cz.cuni.matfyz.core.category.CategoricalObject;
import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.core.schema.Id;
import cz.cuni.matfyz.core.schema.Key;
import cz.cuni.matfyz.core.schema.SchemaObject;
import cz.cuni.matfyz.core.serialization.JSONConvertible;
import cz.cuni.matfyz.core.serialization.ToJSONConverterBase;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Each object from instance category is modeled as a set of tuples ({@link DomainRow}).
 * @author pavel.koupil, jachym.bartik
 */
public class InstanceObject implements Serializable, CategoricalObject, JSONConvertible {

    private final SchemaObject schemaObject;
    private final Map<Id, Map<IdWithValues, DomainRow>> domain = new TreeMap<>();
    private final Map<Integer, DomainRow> domainByTechnicalIds = new TreeMap<>();

    public InstanceObject(SchemaObject schemaObject) {
        this.schemaObject = schemaObject;
    }

    public Key key() {
        return schemaObject.key();
    }

    public String label() {
        return schemaObject.label();
    }

    public Id superId() {
        return schemaObject.superId();
    }

    /**
     * Immutable.
     */
    public Set<Id> ids() {
        return schemaObject.ids();
    }
    
    // TODO rozlišit id od superId
    public DomainRow getRowById(IdWithValues id) {
        Map<IdWithValues, DomainRow> rowsWithSameTypeId = domain.get(id.id());
        return rowsWithSameTypeId == null ? null : rowsWithSameTypeId.get(id);
    }

    public DomainRow getRowByTechnicalId(Integer technicalId) {
        return domainByTechnicalIds.get(technicalId);
    }

    private void setRow(DomainRow row, Collection<IdWithValues> ids) {
        for (var id : ids) {
            Map<IdWithValues, DomainRow> rowsWithSameTypeId = domain.get(id.id());
            if (rowsWithSameTypeId == null) {
                rowsWithSameTypeId = new TreeMap<>();
                domain.put(id.id(), rowsWithSameTypeId);
            }
            rowsWithSameTypeId.put(id, row);
        }

        for (var technicalId : row.technicalIds)
            domainByTechnicalIds.put(technicalId, row);
    }

    public DomainRow getOrCreateRow(IdWithValues superId) {
        var merger = new Merger();
        return merger.merge(superId, this);
    }

    public DomainRow getOrCreateRowWithMorphism(IdWithValues superId, DomainRow parent, InstanceMorphism baseMorphismFromParent) {
        var merger = new Merger();
        return merger.merge(superId, parent, baseMorphismFromParent);
    }

    DomainRow createRow(IdWithValues superId) {
        Set<Integer> technicalIds = superId.findFirstId(ids()) == null
            ? Set.of(generateTechnicalId())
            : Set.of();
        var ids = superId.findAllIds(ids()).foundIds();
        
        return createRow(superId, technicalIds, ids);
    }

    DomainRow createRow(IdWithValues superId, Set<Integer> technicalIds, Set<IdWithValues> allIds) {
        // TODO this can be optimized - we can discard the references that were referenced in all merged rows.
        // However, it might be quite rare, so the overhead caused by finding such ids would be greater than the savings.
        var row = new DomainRow(superId, technicalIds, referencesToRows.keySet());
        setRow(row, allIds);

        return row;
    }

    public DomainRow getRow(IdWithValues superId) {
        for (var id : superId.findAllIds(ids()).foundIds()) {
            var row = getRowById(id);
            if (row != null)
                return row;
        }

        return null;
    }

    // TODO fix - problém je, že getRowById může stále vracet null - tedy toto je použitelné jenom když víme, že alespoň nějaká část superId už musí být zmergovaná, a proto by se nejspíš měla používat jen ta funkce podtím
    /**
     * Returns the most recent row for the superId or technicalIds.
     */
    public DomainRow getActualRow(IdWithValues superId, Set<Integer> technicalIds) {
        // Simply find the first id of all possible ids (any of them should point to the same row).
        var foundId = superId.findFirstId(schemaObject.ids());
        if (foundId != null)
            return getRowById(foundId);

        // Then the row has to be identified by its technical ids (again, any of them will do).
        var technicalId = technicalIds.stream().findFirst();
        if (technicalId.isPresent())
            return getRowByTechnicalId(technicalId.get());

        // This should not happen.
        throw new UnsupportedOperationException("Actual row not found for superId: " + superId + " and technicalIds: " + technicalIds + " .");
    }

    /**
     * Returns the most recent value of the row (possibly the inpuct object).
     */
    public DomainRow getActualRow(DomainRow row) {
        return getActualRow(row.superId, row.technicalIds);
    }

    // The point of a technical id is to differentiate two rows from each other, but only if they do not have any common id that could differentiate them (or unify them, if both of them have the same value of the id).
    private int lastTechnicalId = 0;

    public int generateTechnicalId() {
        lastTechnicalId++;
        return lastTechnicalId;
    }
    
    public Set<DomainRow> allRows() {
        var output = new TreeSet<DomainRow>();

        for (var innerMap : domain.values())
            output.addAll(innerMap.values());

        return output;
    }

    public IdWithValues findTechnicalSuperId(Set<Integer> technicalIds, Set<DomainRow> outOriginalRows) {
        var builder = new IdWithValues.Builder();

        for (var technicalId : technicalIds) {
            var row = getRowByTechnicalId(technicalId);
            if (!outOriginalRows.contains(row)) {
                outOriginalRows.add(row);
                builder.add(row.superId);
            }
        }

        return builder.build();
    }

    public record FindSuperIdResult(IdWithValues superId, Set<IdWithValues> foundIds) {}

    /**
     * Iteratively get all rows that are identified by the superId (while expanding the superId).
     * @param superId
     * @return
     */
    public FindSuperIdResult findMaximalSuperId(IdWithValues superId, Set<DomainRow> outOriginalRows) {
        // First, we take all ids that can be created for this object, and we find those, that can be filled from the given superId.
        // Then we find the rows that correspond to them and merge their superIds to the superId.
        // If it gets bigger, we try to generate other ids to find their objects and so on ...

        int previousSuperIdSize = 0;
        Set<IdWithValues> foundIds = new TreeSet<>();
        Set<Id> notFoundIds = schemaObject.ids();

        while (previousSuperIdSize < superId.size()) {
            previousSuperIdSize = superId.size();

            var result = superId.findAllIds(notFoundIds);
            foundIds.addAll(result.foundIds());
            notFoundIds = result.notFoundIds();
            
            var foundRows = findNewRows(foundIds, outOriginalRows);
            if (foundRows.isEmpty())
                break; // We have not found anything new.

            superId = IdWithValues.merge(superId, mergeSuperIds(foundRows));
        }

        return new FindSuperIdResult(superId, foundIds);
    }

    public static IdWithValues mergeSuperIds(Collection<DomainRow> rows) {
        var builder = new IdWithValues.Builder();

        for (var row : rows)
            builder.add(row.superId);
        
        return builder.build();
    }

    public static Set<Integer> mergeTechnicalIds(Collection<DomainRow> rows) {
        var output = new TreeSet<Integer>();
        rows.forEach(row -> output.addAll(row.technicalIds));
        return output;
    }

    private Set<DomainRow> findNewRows(Set<IdWithValues> foundIds, Set<DomainRow> outOriginalRows) {
        var output = new TreeSet<DomainRow>();

        for (var id : foundIds) {
            var row = getRowById(id);
            if (row == null || outOriginalRows.contains(row))
                continue;
            
            outOriginalRows.add(row);
            output.add(row);
        }

        return output;
    }

    @Override
    public int compareTo(CategoricalObject categoricalObject) {
        return key().compareTo(categoricalObject.key());
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append("\tKey: ").append(key()).append("\n");
        builder.append("\tValues:\n");
        // Consistent ordering of the keys for testing purposes.
        for (Id id : new TreeSet<>(domain.keySet())) {
            var subdomain = domain.get(id);
            // Again, ordering.
            for (IdWithValues superId : new TreeSet<>(subdomain.keySet()))
                builder.append("\t\t").append(subdomain.get(superId)).append("\n");
        }
        
        return builder.toString();
    }
    
    @Override
    public boolean equals(Object object) {
        return object instanceof InstanceObject instanceObject && schemaObject.equals(instanceObject.schemaObject);
    }

    @Override
    public JSONObject toJSON() {
        return new Converter().toJSON(this);
    }

    public static class Converter extends ToJSONConverterBase<InstanceObject> {

        @Override
        protected JSONObject innerToJSON(InstanceObject object) throws JSONException {
            var output = new JSONObject();
    
            output.put("key", object.key().toJSON());

            var domain = object.allRows().stream().map(row -> row.toJSON()).toList();
            output.put("domain", new JSONArray(domain));
            
            return output;
        }
    
    }
    
    private final Map<Signature, Set<ReferenceToRow>> referencesToRows = new TreeMap<>();

    public void addReferenceToRow(Signature signatureInThis, InstanceMorphism path, Signature signatureInOther) {
        var referencesForSignature = referencesToRows.computeIfAbsent(signatureInThis, x -> new TreeSet<>());
        referencesForSignature.add(new ReferenceToRow(signatureInThis, path, signatureInOther));
    }

    public Set<ReferenceToRow> getReferencesForSignature(Signature signatureInThis) {
        return referencesToRows.get(signatureInThis);
    }

    public static class ReferenceToRow implements Comparable<ReferenceToRow> {

        public final Signature signatureInThis;
        public final InstanceMorphism path;
        public final Signature signatureInOther;

        public ReferenceToRow(Signature signatureInThis, InstanceMorphism path, Signature signatureInOther) {
            this.signatureInThis = signatureInThis;
            this.path = path;
            this.signatureInOther = signatureInOther;
        }

        @Override
        public boolean equals(Object object) {
            if (this == object)
                return true;
            
            return object instanceof ReferenceToRow reference
                && signatureInThis.equals(reference.signatureInThis)
                && path.equals(reference.path)
                && signatureInOther.equals(reference.signatureInOther);
        }

        @Override
        public int compareTo(ReferenceToRow reference) {
            if (this == reference)
                return 0;
            
            var x1 = signatureInThis.compareTo(reference.signatureInThis);
            if (x1 != 0)
                return x1;
            
            var x2 = path.compareTo(reference.path);
            if (x2 != 0)
                return x2;
            
            return signatureInOther.compareTo(reference.signatureInOther);
        }

    }

}
