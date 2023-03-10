package cz.cuni.matfyz.core.category;

/**
 * @author pavel.koupil, jachym.bartik
 */
public interface Morphism {

    // Beware that the cardinality of morphism doesn't mean the cardinality from the relational point of view.
    // For example, 1..1 means there is exactly one morphism (which is a monomorphism i.e. an injection).
    // However, it doesn't mean there is one object from the codomain for each object from the domain.
    public enum Min {
        ZERO,
        ONE
    }
    
    public enum Max {
        ONE,
        STAR
    }

    public abstract CategoricalObject dom();

    public abstract CategoricalObject cod();

    public abstract Morphism dual();
    
    public abstract Signature signature();

    public abstract Min min();

    public abstract Max max();

}
