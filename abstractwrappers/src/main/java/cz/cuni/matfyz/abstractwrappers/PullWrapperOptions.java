package cz.cuni.matfyz.abstractwrappers;

/**
 * @author jachym.bartik
 */
public class PullWrapperOptions {
    
    private String kindName = null;

    public String getKindName() {
        return kindName;
    }
    
    private Integer offset = null;

    public int getOffset() {
        return offset;
    }

    public boolean hasOffset() {
        return offset != null;
    }

    private Integer limit = null;

    public int getLimit() {
        return limit;
    }

    public boolean hasLimit() {
        return limit != null;
    }

    public String query = null;

    public static class Builder {

        private PullWrapperOptions instance = new PullWrapperOptions();

        public Builder offset(Integer offset) {
            instance.offset = offset;
            return this;
        }

        public Builder limit(Integer limit) {
            instance.limit = limit;
            return this;
        }

        public Builder query(String query) {
            instance.query = query;
            return this;
        }

        public PullWrapperOptions buildWithKindName(String kindName) {
            instance.kindName = kindName;
            return instance;
        }

    }

}
