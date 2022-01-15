package cz.cuni.matfyz.transformations;

import cz.cuni.matfyz.core.record.ForestOfRecords;
import cz.cuni.matfyz.core.record.RootRecord;
import cz.cuni.matfyz.core.record.ComplexRecord;
import cz.cuni.matfyz.core.record.SimpleRecord;
import cz.cuni.matfyz.core.record.SimpleValueRecord;
import cz.cuni.matfyz.core.record.SimpleArrayRecord;
import cz.cuni.matfyz.core.category.*;
import cz.cuni.matfyz.core.instance.*;
import cz.cuni.matfyz.core.mapping.*;
import cz.cuni.matfyz.core.schema.*;
import cz.cuni.matfyz.core.utils.Debug;

import java.util.*;
import org.javatuples.Pair;

/**
 *
 * @author pavel.koupil, jachym.bartik
 */
public class ModelToCategory
{
    private SchemaCategory schema; // TODO
    //private InstanceCategory instance; // TODO
    private ForestOfRecords forest; // TODO
    private Mapping mapping; // TODO
    private ComplexProperty rootAccessPath;
            
    private InstanceFunctor instanceFunctor;
    
    public void input(SchemaCategory schema, InstanceCategory instance, ForestOfRecords forest, Mapping mapping)
    {
        this.schema = schema;
        //this.instance = instance;
        this.forest = forest;
        this.mapping = mapping;
        this.rootAccessPath = mapping.accessPath().copyWithoutAuxiliaryNodes();
        
        instanceFunctor = new InstanceFunctor(instance, schema);
    }
    
	public void algorithm()
    {
        if (Debug.shouldLog(4))
            System.out.println("# ALGORITHM");
        
        for (RootRecord rootRecord : forest)
        {
            if (Debug.shouldLog(4))
                System.out.println("################ Process of Root Record ################");
			// preparation phase
			Stack<StackTriple> M = mapping.hasRootMorphism() ?
                createStackWithMorphism(mapping.rootObject(), mapping.rootMorphism(), rootRecord) : // K with root morphism
                createStackWithObject(mapping.rootObject(), rootRecord); // K with root object

			// processing of the tree
			while (!M.empty())
				processTopOfStack(M);
		}
	}
    
    private Stack<StackTriple> createStackWithObject(SchemaObject object, RootRecord record)
    {
        if (Debug.shouldLog(3))
            System.out.println("#### Create Stack With Object ####");
        
        InstanceObject qI = instanceFunctor.object(object);
        IdWithValues sid = fetchSid(object.superId(), record);
        Stack<StackTriple> M = new Stack<>();
        
        ActiveDomainRow row = modify(qI, sid);
        addPathChildrenToStack(M, rootAccessPath, row, record);
        
        if (Debug.shouldLog(3))
            System.out.println("# Stack size: " + M.size());
        
        return M;
    }
    
    private Stack<StackTriple> createStackWithMorphism(SchemaObject object, SchemaMorphism morphism, RootRecord record)
    {
        if (Debug.shouldLog(3))
            System.out.println("#### Create Stack With Morphism ####");
        
        Stack<StackTriple> M = new Stack<>();
        
        InstanceObject qI_dom = instanceFunctor.object(object);
        IdWithValues sids_dom = fetchSid(object.superId(), record);
        ActiveDomainRow sid_dom = modify(qI_dom, sids_dom);

        SchemaObject qS_cod = morphism.cod();
        IdWithValues sids_cod = fetchSid(qS_cod.superId(), record);
        
        InstanceObject qI_cod = instanceFunctor.object(qS_cod);
        ActiveDomainRow sid_cod = modify(qI_cod, sids_cod);

        InstanceMorphism mI = instanceFunctor.morphism(morphism);

        addRelation(mI, sid_dom, sid_cod);
        addRelation(mI.dual(), sid_cod, sid_dom);

        AccessPath t_dom = rootAccessPath.getSubpathBySignature(Signature.Empty());
        AccessPath t_cod = rootAccessPath.getSubpathBySignature(morphism.signature());

        AccessPath ap = rootAccessPath.minusSubpath(t_dom).minusSubpath(t_cod);

        addPathChildrenToStack(M, ap, sid_dom, record);
        addPathChildrenToStack(M, t_cod, sid_cod, record);
        
        if (Debug.shouldLog(3))
            System.out.println("# Stack size: " + M.size());
        
        return M;
    }
    
    private void processTopOfStack(Stack<StackTriple> M)
    {
        if (Debug.shouldLog(3))
            System.out.println("#### Process Top of Stack ####");
        if (Debug.shouldLog(2))
            printStack(M);
        
        StackTriple triple = M.pop();
        InstanceMorphism mI = instanceFunctor.morphism(triple.mS);
        SchemaObject oS = triple.mS.cod();
        InstanceObject qI = instanceFunctor.object(oS);
        Iterable<Pair<IdWithValues, ComplexRecord>> sids = fetchSids(oS.superId(), triple.record, triple.pid, triple.mS);

        for (Pair<IdWithValues, ComplexRecord> sid : sids)
        {
            ActiveDomainRow row = modify(qI, sid.getValue0());

            addRelation(mI, triple.pid, row);
            addRelation(mI.dual(), row, triple.pid);
            
            addPathChildrenToStack(M, triple.t, row, sid.getValue1());
        }
    }
    
    private void printStack(Stack<StackTriple> M)
    {
        StringBuilder builder = new StringBuilder();
        builder.append("\nSize: ").append(M.size());
        
        var array = M.toArray();
        for (int i = array.length - 1; i >= 0; i--)
            builder.append("\n").append(array[i]);
        builder.append("\n");
        
        System.out.println(builder.toString());
    }

    // Fetch id with values for given record
	//private IdWithValues fetchSid(Id superId, RootRecord record)
    private static IdWithValues fetchSid(Id superId, RootRecord rootRecord)
    {
        var builder = new IdWithValues.Builder();
        
        for (Signature signature : superId.signatures())
        {
            /*
            Object value = rootRecord.values().get(signature).getValue();
            if (value instanceof String stringValue)
                builder.add(signature, stringValue);
            */
            SimpleRecord<?> simpleRecord = rootRecord.values().get(signature);
            if (simpleRecord instanceof SimpleValueRecord<?> simpleValueRecord)
            {
                builder.add(signature, simpleValueRecord.getValue().toString());
            }
            else
            {
                throw new UnsupportedOperationException("FetchSid doesn't support array values.");
            }
        }
        
        return builder.build();
	}
    
    // Fetch id with values for given record
    // The return value is a set of (Signature, String) for each Signature in superId and its corresponding value from record
    // Actually, there can be multiple values in the record, so the set of sets is returned
    private static Iterable<Pair<IdWithValues, ComplexRecord>> fetchSids(Id superId, ComplexRecord parentRecord, ActiveDomainRow parentRow, SchemaMorphism morphism)
    {
        List<Pair<IdWithValues, ComplexRecord>> output = new ArrayList<>();
        
        if (superId.compareTo(Id.Empty()) == 0)
        {
            // Nejdřív zkusíme najít hodnotu v pidu
            String valueFromParentRow = parentRow.tuples().get(morphism.signature());
            if (valueFromParentRow != null)
            {
                var builder = new IdWithValues.Builder();
                builder.add(Signature.Empty(), valueFromParentRow);
                output.add(new Pair<>(builder.build(), null)); // Doesn't matter if there is null because the accessPath is also null so it isn't further traversed
            }
            else
            {
                // Pokud je v recordu hodnota - což odpovídá simple property
                SimpleRecord<?> simpleRecord = parentRecord.values().get(morphism.signature());
                if (simpleRecord != null)
                {
                    if (simpleRecord instanceof SimpleValueRecord<?> simpleValueRecord)
                    {
                        var builder = new IdWithValues.Builder();
                        builder.add(Signature.Empty(), simpleValueRecord.getValue().toString());
                        output.add(new Pair<>(builder.build(), null)); // Doesn't matter if there is null because the accessPath is also null so it isn't further traversed
                    }
                    else if (simpleRecord instanceof SimpleArrayRecord<?> simpleArrayRecord)
                    {
                        for (Object value : simpleArrayRecord.getValues())
                        {
                            var builder = new IdWithValues.Builder();
                            builder.add(Signature.Empty(), value.toString());
                            output.add(new Pair<>(builder.build(), null)); // Doesn't matter if there is null because the accessPath is also null so it isn't further traversed
                        }
                    }
                    else
                    {
                        throw new UnsupportedOperationException("Simple record must be either SimpleValueRecord<String> or SimpleArrayRecord<String>.");
                    }
                }
                else
                {
                    // Pokud tu není hodnota, je potřeba vrátit nějakou unikátní hodnotu
                    List<ComplexRecord> childRecords = parentRecord.children().get(morphism.signature());
                    if (childRecords != null)
                        for (ComplexRecord childRecord : childRecords)
                        {
                            var builder = new IdWithValues.Builder();
                            builder.add(Signature.Empty(), UniqueIdProvider.getNext());
                            output.add(new Pair<>(builder.build(), childRecord));
                        }
                }
            }
        }
        else
        {
            List<ComplexRecord> childRecords = parentRecord.children().get(morphism.signature());
            if (childRecords != null)
                for (ComplexRecord childRecord : childRecords)
                {
                    if (childRecord.firstDynamicValue() == null)
                        handleNonDynamicCase(superId, parentRow, morphism, childRecord, output);
                    else
                        handleDynamicCase(superId, parentRow, morphism, childRecord, output);
                }
        }
        
        return output;
    }
    
    private static void handleDynamicCase(Id superId, ActiveDomainRow parentRow, SchemaMorphism morphism, ComplexRecord childRecord, List<Pair<IdWithValues, ComplexRecord>> output)
    {
        Set<Signature> dynamicSignatures = new TreeSet<>();
        Set<Signature> nonDynamicSignatures = new TreeSet<>();
        
        for (Signature signature : superId.signatures())
            if (childRecord.containsDynamicSignature(signature))
                dynamicSignatures.add(signature);
            else
                nonDynamicSignatures.add(signature);
        
        if (nonDynamicSignatures.size() == superId.signatures().size())
        {
            handleNonDynamicCase(superId, parentRow, morphism, childRecord, output);
            return;
        }
        
        for (SimpleValueRecord<?> dynamicRecord : childRecord.dynamicValues())
        {
            var builder = new IdWithValues.Builder();
            for (Signature signature : nonDynamicSignatures)
            {
                var signatureInParentRow = signature.traverseThrough(morphism.signature());

                // Why java still doesn't support output arguments?
                String value;
                if (signatureInParentRow == null)
                {
                    SimpleRecord<?> simpleRecord = childRecord.values().get(signature);
                    if (simpleRecord instanceof SimpleValueRecord<?> simpleValueRecord)
                        value = simpleValueRecord.getValue().toString();
                    else
                        throw new UnsupportedOperationException("FetchSids doesn't support array values for complex records.");
                }
                else
                    value = parentRow.tuples().get(signatureInParentRow);

                builder.add(signature, value);
            }
            
            for (Signature signature : dynamicSignatures)
            {
                String value = dynamicRecord.signature().equals(signature) ? dynamicRecord.getValue().toString() : dynamicRecord.name().value();
                builder.add(signature, value);
            }

            output.add(new Pair<>(builder.build(), childRecord));
        }
    }
    
    private static void handleNonDynamicCase(Id superId, ActiveDomainRow parentRow, SchemaMorphism morphism, ComplexRecord childRecord, List<Pair<IdWithValues, ComplexRecord>> output)
    {
        var builder = new IdWithValues.Builder();
        for (Signature signature : superId.signatures())
        {
            var signatureInParentRow = signature.traverseThrough(morphism.signature());

            // Why java still doesn't support output arguments?
            String value;
            if (signatureInParentRow == null)
            {
                SimpleRecord<?> simpleRecord = childRecord.values().get(signature);
                if (simpleRecord instanceof SimpleValueRecord<?> simpleValueRecord)
                    value = simpleValueRecord.getValue().toString();
                else
                    throw new UnsupportedOperationException("FetchSids doesn't support array values for complex records.");
            }
            else
                value = parentRow.tuples().get(signatureInParentRow);

            builder.add(signature, value);
        }

        output.add(new Pair<>(builder.build(), childRecord));
    }

    // Create ActiveDomainRow from given IdWithValues and add it to the instance object
	private static ActiveDomainRow modify(InstanceObject qI, IdWithValues sid)
    {
        Set<ActiveDomainRow> rows = new TreeSet<>();
        Set<IdWithValues> idsWithValues = getIdsWithValues(qI.schemaObject().ids(), sid);
        
        for (IdWithValues idWithValues : idsWithValues)
        {
            Map<IdWithValues, ActiveDomainRow> map = qI.activeDomain().get(idWithValues.id());
            if (map == null)
                continue;
            
            ActiveDomainRow row = map.get(idWithValues);
            if (row != null && !rows.contains(row))
                rows.add(row);
        }
        
        var builder = new IdWithValues.Builder();
        for (ActiveDomainRow row : rows)
            for (Signature signature : row.tuples().keySet())
                builder.add(signature, row.tuples().get(signature));
        
        for (Signature signature : sid.signatures())
            builder.add(signature, sid.map().get(signature));
        
        ActiveDomainRow newRow = new ActiveDomainRow(builder.build());
        for (IdWithValues idWithValues : idsWithValues)
        {
            Map<IdWithValues, ActiveDomainRow> map = qI.activeDomain().get(idWithValues.id());
            if (map == null)
            {
                map = new TreeMap<>();
                qI.activeDomain().put(idWithValues.id(), map);
            }
            map.put(idWithValues, newRow);
        }
        
        // TODO: předělat existující morfizmy
        // WARNING: může se stát, že tu sloučím více řádků do jednoho - potom bude nutné sloučit jejich morfizmy do jednoho, což se nejlépe vyřeší lazy algoritmem
        // právě až to bude potřeba
        // TODO: optimalizovat
        
        return newRow;
	}
    
    private static Set<IdWithValues> getIdsWithValues(Set<Id> ids, IdWithValues sid)
    {
        Set<IdWithValues> output = new TreeSet<>();
        for (Id id : ids)
        {
            var builder = new IdWithValues.Builder();
            boolean idIsInSuperId = true;
            for (Signature signature : id.signatures())
            {
                String value = sid.map().get(signature);
                if (value == null)
                {
                    idIsInSuperId = false;
                    break;
                }
                builder.add(signature, value);
            }
            if (idIsInSuperId)
                output.add(builder.build());
        }
        return output;
    }
    
    private static void addRelation(InstanceMorphism morphism, ActiveDomainRow sid_dom, ActiveDomainRow sid_cod)
    {
		morphism.addMapping(new ActiveMappingRow(sid_dom, sid_cod));
	}
    
    private void addPathChildrenToStack(Stack<StackTriple> stack, AccessPath path, ActiveDomainRow sid, ComplexRecord record)
    //private static void addPathChildrenToStack(Stack<StackTriple> stack, AccessPath path, ActiveDomainRow sid, ComplexRecord record)
    {
        if (path instanceof ComplexProperty complexPath)
            for (Pair<Signature, ComplexProperty> child: children(complexPath))
            {
                // TODO signature to schema morphism
                if (Debug.shouldLog(1))
                {
                    System.out.println("$$$ Signature: ");
                    System.out.println(child.getValue(0));
                }
                SchemaMorphism morphism = schema.signatureToMorphism(child.getValue0());
                stack.push(new StackTriple(sid, morphism, child.getValue1(), record));
            }
    }

    /**
     * Determine possible sub-paths to be traversed from this complex property (inner node of an access path).
     * According to the paper, this function should return pairs of (context, value). But value of such sub-path can only be an {@link ComplexProperty}.
     * Similarly, context must be a signature of a morphism.
     * @return set of pairs (morphism signature, complex property) of all possible sub-paths.
     */
	private static Collection<Pair<Signature, ComplexProperty>> children(ComplexProperty complexProperty)
    {
        if (Debug.shouldLog(0))
        {
            System.out.println("$ Children:");
            System.out.println(complexProperty);
        }
        
        final List<Pair<Signature, ComplexProperty>> output = new ArrayList<>();
        
        for (AccessPath subpath: complexProperty.subpaths())
        {
            output.addAll(process(subpath.name()));
            output.addAll(process(subpath.context(), subpath.value()));
        }
        
        return output;
	}
    
    /**
     * Process (name, context and value) according to the "process" function from the paper.
     * This function is divided to two parts - one for name and other for context and value.
     * @param name
     * @return see {@link #children()}
     */
    private static Collection<Pair<Signature, ComplexProperty>> process(Name name)
    {
        if (Debug.shouldLog(0))
        {
            System.out.println("$ Process name:");
            System.out.println(name);
        }
        
        if (name.type() == Name.Type.DYNAMIC_NAME)
            return List.of(new Pair<>(name.signature(), ComplexProperty.Empty()));
        else // Static or anonymous (empty) name
            return Collections.<Pair<Signature, ComplexProperty>>emptyList();
    }
    
    private static Collection<Pair<Signature, ComplexProperty>> process(IContext context, IValue value)
    {
        if (Debug.shouldLog(0))
        {
            System.out.println("$ Process context, value:");
            System.out.println(context);
            System.out.println(value);
        }
        
        if (value instanceof SimpleValue simpleValue)
        {
            final Signature contextSignature = context instanceof Signature signature ? signature : Signature.Empty();
            final Signature newSignature = simpleValue.signature().concatenate(contextSignature);
            
            return List.of(new Pair<>(newSignature, ComplexProperty.Empty()));
        }
        
        if (value instanceof ComplexProperty complexProperty)
        {
            if (context instanceof Signature signature)
                return List.of(new Pair<>(signature, complexProperty));
            else
                return children(complexProperty);
        }
        
        throw new UnsupportedOperationException();
    }
}