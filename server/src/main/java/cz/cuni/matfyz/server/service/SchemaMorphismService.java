package cz.cuni.matfyz.server.service;

import cz.cuni.matfyz.server.entity.schema.SchemaMorphismWrapper;
import cz.cuni.matfyz.server.repository.SchemaMorphismRepository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * @author jachym.bartik
 */
@Service
public class SchemaMorphismService {

    @Autowired
    private SchemaMorphismRepository repository;

    public List<SchemaMorphismWrapper> findAllInCategory(int categoryId) {
        return repository.findAllInCategory(categoryId);
    }

    public SchemaMorphismWrapper find(int id) {
        return repository.find(id);
    }

    /*
    public Integer add(SchemaMorphism morphism) {
        return repository.add(morphism);
    }
    */
}
