package cz.cuni.matfyz.server.view;

import java.io.Serializable;
import java.util.List;

import cz.cuni.matfyz.server.entity.Database;

/**
 * 
 * @author jachym.bartik
 */
public class MappingOptionsView implements Serializable {

    public int categoryId;
    public List<Database> databases;

}
