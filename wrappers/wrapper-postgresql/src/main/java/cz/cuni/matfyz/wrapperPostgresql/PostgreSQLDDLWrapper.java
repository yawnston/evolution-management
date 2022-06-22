package cz.cuni.matfyz.wrapperPostgresql;

import java.util.*;

import cz.cuni.matfyz.abstractWrappers.AbstractDDLWrapper;

/**
 *
 * @author jachymb.bartik
 */
public class PostgreSQLDDLWrapper implements AbstractDDLWrapper
{    
    private String kindName = null;
    private final List<Property> properties = new ArrayList<>();
    
    @Override
    public void setKindName(String name)
    {
        kindName = name;
    }

    @Override
    public boolean isSchemaLess() { return false; }

    @Override
    public boolean addSimpleProperty(Set<String> names, boolean required) throws UnsupportedOperationException
    {
        names.forEach(name -> {            
            String command = name + " TEXT" + (required ? " NOT NULL" : "");
            properties.add(new Property(name, command));
        });
        
        return true;
    }

    @Override
    public boolean addSimpleArrayProperty(Set<String> names, boolean required) throws UnsupportedOperationException
    {
        names.forEach(name -> {
            String command = name + " TEXT[]" + (required ? " NOT NULL" : "");
            properties.add(new Property(name, command));
        });
        
        return true;
    }

    @Override
    public boolean addComplexProperty(Set<String> names, boolean required) throws UnsupportedOperationException
    {
        throw new UnsupportedOperationException(); // It is supported in a newer version (see https://www.postgresql.org/docs/10/rowtypes.html) so it could be implemented later.
    }

    @Override
    public boolean addComplexArrayProperty(Set<String> names, boolean required) throws UnsupportedOperationException
    {
        throw new UnsupportedOperationException(); // It is supported in a newer version (see https://www.postgresql.org/docs/10/rowtypes.html) so it could be implemented later.
    }

    @Override
    public PostgreSQLDDLStatement createDDLStatement()
    {
        String commands = String.join(",\n", properties.stream().map(property -> AbstractDDLWrapper.INTENDATION + property.command).toList());
        String content = String.format("CREATE TABLE %s (\n%s\n);", kindName, commands);
        
        return new PostgreSQLDDLStatement(content);
    }
}

class Property
{
    public String name;
    public String command;
    
    public Property(String name, String command)
    {
        this.name = name;
        this.command = command;
    }
}