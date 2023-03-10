package cz.cuni.matfyz.wrapperpostgresql;

import cz.cuni.matfyz.abstractwrappers.AbstractPathWrapper;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jachymb.bartik
 */
public class PostgreSQLPathWrapper implements AbstractPathWrapper {

    private final List<String> properties = new ArrayList<>();
    
    @Override
    public void addProperty(String hierarchy) {
        this.properties.add(hierarchy);
    }

    @Override
    public boolean check() {
        return true; // This should be ok
    }

    // CHECKSTYLE:OFF
    @Override public boolean isPropertyToOneAllowed() { return true; }
    @Override public boolean isPropertyToManyAllowed() { return false; }
    @Override public boolean isInliningToOneAllowed() { return true; }
    @Override public boolean isInliningToManyAllowed() { return false; }
    @Override public boolean isGrouppingAllowed() { return false; }
    @Override public boolean isDynamicNamingAllowed() { return false; }
    @Override public boolean isAnonymousNamingAllowed() { return false; }
    @Override public boolean isReferenceAllowed() { return true; }
    @Override public boolean isComplexPropertyAllowed() { return false; }
    // CHECKSTYLE:ON
}
