package cz.cuni.matfyz.core.record;

import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.core.utils.IndentedStringBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * This class represents an inner node of the record tree.
 * The value of this record are its children.
 * @author jachymb.bartik
 */
public class ComplexRecord extends DataRecord implements IComplexRecord {

    //private final List<DataRecord> children = new ArrayList<>();
    //private final Map<Signature, Set<DataRecord>> children = new TreeMap<>();
    
    private final Map<Signature, List<ComplexRecord>> children = new TreeMap<>();
    private final List<ComplexRecord> dynamicNameChildren = new ArrayList<>();
    private Signature dynamicNameSignature;
    private final Map<Signature, SimpleRecord<?>> values = new TreeMap<>();
    private final List<SimpleValueRecord<?>> dynamicNameValues = new ArrayList<>();
    //private SimpleValueRecord<?> firstDynamicValue = null;
    
    protected ComplexRecord(RecordName name) {
        super(name);
    }
    
    /*
    public Map<Signature, List<ComplexRecord>> children() {
        return children;
    }
    */

    public boolean hasComplexRecords(Signature signature) {
        //return children.containsKey(signature) || (signature.equals(dynamicSignature));
        return children.containsKey(signature);
    }

    public List<? extends IComplexRecord> getComplexRecords(Signature signature) {
        //return signature.equals(dynamicSignature) ? dynamicChildren : children.get(signature);
        return children.get(signature);
    }
    
    public boolean hasDynamicNameChildren() {
        return !dynamicNameChildren.isEmpty();
    }
    
    public List<? extends IComplexRecord> getDynamicNameChildren() {
        return dynamicNameChildren;
    }

    public Signature dynamicNameSignature() {
        return dynamicNameSignature;
    }

    public boolean hasSimpleRecord(Signature signature) {
        return values.containsKey(signature);
    }

    public SimpleRecord<?> getSimpleRecord(Signature signature) {
        return values.get(signature);
    }

    public boolean hasDynamicNameValues() {
        return !dynamicNameValues.isEmpty();
    }
    
    public List<SimpleValueRecord<?>> getDynamicNameValues() {
        return dynamicNameValues;
    }
    
    public boolean containsDynamicNameValue(Signature signature) {
        if (!hasDynamicNameValues())
            return false;

        SimpleValueRecord<?> firstDynamicNameValue = dynamicNameValues.get(0);
        return signature.equals(firstDynamicNameValue.signature)
            || firstDynamicNameValue.name() instanceof DynamicRecordName dynamicName && signature.equals(dynamicName.signature());
    }
    
    public ComplexRecord addComplexRecord(RecordName name, Signature signature) {
        ComplexRecord complexRecord = new ComplexRecord(name);
        
        if (complexRecord.name instanceof StaticRecordName) {
            List<ComplexRecord> childSet = children.computeIfAbsent(signature, x -> new ArrayList<>());
            childSet.add(complexRecord);
        }
        else {
            dynamicNameChildren.add(complexRecord);
            dynamicNameSignature = signature;
        }
        
        return complexRecord;
    }
    
    public <T> SimpleArrayRecord<T> addSimpleArrayRecord(RecordName name, Signature signature, List<T> values) {
        var simpleArrayRecord = new SimpleArrayRecord<>(name, signature, values);
        this.values.put(signature, simpleArrayRecord);
        
        return simpleArrayRecord;
    }
    
    public <T> SimpleValueRecord<T> addSimpleValueRecord(RecordName name, Signature signature, T value) {
        var simpleValueRecord = new SimpleValueRecord<>(name, signature, value);

        if (name instanceof StaticRecordName)
            values.put(signature, simpleValueRecord);
        else if (name instanceof DynamicRecordName) {
            /*
            if (firstDynamicValue == null)
                firstDynamicValue = record;
            else {
                assert firstDynamicValue.name() instanceof DynamicRecordName firstDynamicName && dynamicName.signature().equals(firstDynamicName.signature()) : "Trying to add a dynamic name with different name signature";
                assert signature.equals(firstDynamicValue.signature()) : "Trying to add a dynamic name with different value signature";
            }
            */
            
            dynamicNameValues.add(simpleValueRecord);
        }

        
        return simpleValueRecord;
    }
    
    /*
    @Override
    Set<DataRecord> records() {
        Set output = Set.of(this);
        children.values().stream().forEach(set -> output.addAll(set));
        
        return output;
    }
    */
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{\n");
        
        var childrenBuilder = new IndentedStringBuilder(1);
        
        for (SimpleRecord<?> value : values.values())
            childrenBuilder.append(value).append(",\n");
        
        for (SimpleValueRecord<?> dynamicNameValue : dynamicNameValues)
            childrenBuilder.append(dynamicNameValue).append(",\n");

        for (List<ComplexRecord> list : children.values()) {
            ComplexRecord firstItem = list.get(0);
            
            if (list.size() > 1) {
                childrenBuilder.append(firstItem.name).append(": ");
                childrenBuilder.append("[\n");
            
                var innerBuilder = new IndentedStringBuilder(1);
                innerBuilder.append(firstItem);
                for (int i = 1; i < list.size(); i++)
                    innerBuilder.append(",\n").append(list.get(i));

                childrenBuilder.append(innerBuilder);
                if (list.size() > 1)
                    childrenBuilder.append("]");
            }
            // Normal complex property
            else {
                childrenBuilder.append(firstItem.name).append(": ").append(firstItem);
            }
            
            childrenBuilder.append(",\n");
        }

        for (ComplexRecord dynamicNameChild : dynamicNameChildren) {
            childrenBuilder.append(dynamicNameChild.name).append(": ").append(dynamicNameChild).append(",\n");
        }

        String childrenResult = childrenBuilder.toString();
        if (childrenResult.length() > 0)
            childrenResult = childrenResult.substring(0, childrenResult.length() - 2);
        
        builder.append(childrenResult);
        builder.append("\n}");
        
        return builder.toString();
    }
}
