package cz.cuni.matfyz.transformations;

import cz.cuni.matfyz.core.instance.*;
import cz.cuni.matfyz.core.mapping.*;
import cz.cuni.matfyz.core.schema.*;
import cz.cuni.matfyz.abstractwrappers.AbstractPushWrapper;
import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.statements.DMLStatement;

import java.util.*;

/**
 *
 * @author jachymb.bartik
 */
public class DMLAlgorithm
{
    private InstanceCategory instance;
    private InstanceFunctor instanceFunctor;
    private String rootName; // TODO
    private Mapping mapping;
    private AbstractPushWrapper wrapper;

    public void input(SchemaCategory schema, InstanceCategory instance, String rootName, Mapping mapping, AbstractPushWrapper wrapper)
    {
        this.instance = instance;
        instanceFunctor = new InstanceFunctor(instance, schema);
        this.rootName = rootName; // Maybe it could be found as mapping.accessPath.Name.getStringName()
        this.mapping = mapping;
        this.wrapper = wrapper;
    }
    
    public List<DMLStatement> algorithm() throws Exception
    {
        return mapping.hasRootMorphism() ?
            processWithMorphism(mapping.rootMorphism()) : // K with root morphism
            processWithObject(mapping.rootObject()); // K with root object
    }

    private List<DMLStatement> processWithObject(SchemaObject object) throws Exception
    {
        InstanceObject qI = instanceFunctor.object(object);
        Set<ActiveDomainRow> S = fetchSids(qI);
        Stack<DMLStackTriple> M = new Stack<>();
        List<DMLStatement> output = new ArrayList<>();

        for (ActiveDomainRow sid : S)
        {
            M.push(new DMLStackTriple(sid, Name.Anonymous().getStringName(), mapping.accessPath()));
            output.add(buildStatement(M));
        }

        return output;
    }

    private List<DMLStatement> processWithMorphism(SchemaMorphism morphism) throws Exception
    {
        InstanceMorphism mI = instanceFunctor.morphism(morphism);
        Set<ActiveMappingRow> S = fetchRelations(mI);
        AccessPath codomainPath = mapping.accessPath().getSubpathBySignature(morphism.signature());
        Stack<DMLStackTriple> M = new Stack<>();
        List<DMLStatement> output = new ArrayList<>();

        if (codomainPath instanceof ComplexProperty complexPath)
        {
            for (ActiveMappingRow row : S)
            {
                M.push(new DMLStackTriple(row.domainRow(), Name.Anonymous().getStringName(), mapping.accessPath().minusSubpath(codomainPath)));
                M.push(new DMLStackTriple(row.codomainRow(), Name.Anonymous().getStringName(), complexPath));
                output.add(buildStatement(M));
            }

            return output;
        }

        throw new UnsupportedOperationException();
    }

    private Set<ActiveDomainRow> fetchSids(InstanceObject object)
    {
        Set<ActiveDomainRow> output = new TreeSet<>();

        for (var innerMap : object.activeDomain().values())
            output.addAll(innerMap.values());

        return output;
    }

    private Set<ActiveMappingRow> fetchRelations(InstanceMorphism morphism)
    {
        return morphism.mappings();
    }

    private DMLStatement buildStatement(Stack<DMLStackTriple> M)
    {
        wrapper.clear();
        wrapper.setKindName(rootName);

        while (!M.empty())
        {
            DMLStackTriple triple = M.pop();
            List<NameValuePair> pairs = collectNameValuePairs(triple.t, triple.pid);

            for (var pair : pairs)
            {
                String newName = triple.name + "/" + pair.name;

                if (pair.isSimple)
                    wrapper.append(newName, pair.simpleValue);
                else
                    M.push(new DMLStackTriple(pair.complexValue, newName, pair.subpath));
            }
        }

        return wrapper.createDMLStatement();
    }

    private List<NameValuePair> collectNameValuePairs(ComplexProperty path, ActiveDomainRow row)
    {
        List<NameValuePair> output = new ArrayList<>();

        for (AccessPath subpath : path.subpaths())
        {
            // Get all mapping rows that have signature of this subpath and originate in given row.
            InstanceMorphism morphism = instance.morphism(subpath.signature());
            morphism.mappings().stream().filter(mapping -> mapping.domainRow().equals(row))
                .forEach(mappingRow -> output.add(getNameValuePair(subpath, mappingRow)));
        }

        return output;
    }

    private NameValuePair getNameValuePair(AccessPath objectPath, ActiveMappingRow parentToObjectMapping)
    {
        ActiveDomainRow objectRow = parentToObjectMapping.codomainRow();
        String name = getStringName(objectPath, parentToObjectMapping);

        if (objectPath instanceof SimpleProperty simplePath)
        {
            String value = objectRow.tuples().get(Signature.Empty());

            return new NameValuePair(name, value);
        }
        else if (objectPath instanceof ComplexProperty complexPath)
        {
            return new NameValuePair(name, objectRow, complexPath);
        }

        throw new UnsupportedOperationException();
    }

    private String getStringName(AccessPath objectPath, ActiveMappingRow parentToObjectMapping)
    {
        Name name = objectPath.name();

        if (name.type() != Name.Type.DYNAMIC_NAME)
        {
            try { // Just a workaround
                return name.getStringName();
            }
            catch (Exception e) {}
        }

        // If the name is dynamic, we have to find its string value.
        ActiveDomainRow parentRow = parentToObjectMapping.domainRow();
        InstanceMorphism nameMorphism = instance.morphism(name.signature());
        Optional<ActiveMappingRow> nameRow = nameMorphism.mappings().stream().filter(mapping -> mapping.domainRow().equals(parentRow))
            .findFirst();

        if (nameRow.isPresent())
            return nameRow.get().codomainRow().tuples().get(Signature.Empty());

        throw new UnsupportedOperationException("Dynamic name value not found.");
    }

    private class NameValuePair
    {
        public final String name;
        public final String simpleValue;
        public final ActiveDomainRow complexValue;
        public final ComplexProperty subpath;
        public final boolean isSimple;

        public NameValuePair(String name, String simpleValue)
        {
            this.name = name;
            this.simpleValue = simpleValue;
            this.complexValue = null;
            this.subpath = null;
            this.isSimple = true;
        }

        public NameValuePair(String name, ActiveDomainRow complexValue, ComplexProperty subpath)
        {
            this.name = name;
            this.simpleValue = null;
            this.complexValue = complexValue;
            this.subpath = subpath;
            this.isSimple = false;
        }
    }
}