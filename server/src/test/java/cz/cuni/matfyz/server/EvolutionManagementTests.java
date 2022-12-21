package cz.cuni.matfyz.server;

import cz.cuni.matfyz.abstractwrappers.AbstractPullWrapper;
import cz.cuni.matfyz.core.category.Morphism.Max;
import cz.cuni.matfyz.core.category.Morphism.Min;
import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.core.instance.DomainRow;
import cz.cuni.matfyz.core.instance.IdWithValues;
import cz.cuni.matfyz.core.instance.InstanceCategory;
import cz.cuni.matfyz.core.instance.InstanceMorphism;
import cz.cuni.matfyz.core.instance.InstanceObject;
import cz.cuni.matfyz.core.instance.MappingRow;
import cz.cuni.matfyz.core.mapping.Mapping;
import cz.cuni.matfyz.core.schema.Id;
import cz.cuni.matfyz.core.schema.SchemaCategory;
import cz.cuni.matfyz.core.schema.SchemaMorphism;
import cz.cuni.matfyz.core.utils.Statistics;
import cz.cuni.matfyz.core.utils.Statistics.Counter;
import cz.cuni.matfyz.core.utils.Statistics.Interval;
import cz.cuni.matfyz.server.entity.database.Database;
import cz.cuni.matfyz.server.service.DatabaseService;
import cz.cuni.matfyz.server.service.WrapperService;
import cz.cuni.matfyz.transformations.algorithms.UniqueIdProvider;
import cz.cuni.matfyz.transformations.processes.DatabaseToInstance;

import java.net.URISyntaxException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author jachymb.bartik
 */
@SpringBootTest
public class EvolutionManagementTests {

    private static final Logger LOGGER = LoggerFactory.getLogger(EvolutionManagementTests.class);

    private TestData data;


    @Autowired
    private DatabaseService databaseService;

    @Autowired
    private WrapperService wrapperService;

    @BeforeEach
    public void setUp() {
        UniqueIdProvider.reset();
        data = new TestData();
        Statistics.reset();
        //schema = data.createDefaultSchemaCategory();
        //order = schema.getObject(data.getOrderKey());
    }

    @Test
    public void joinMoveTest() throws Exception {
        final int repetitions = 5;
        final int[] batches = new int[] { 1, 2 };
        //final int[] batches = new int[] { 1, 2, 4, 8, 16, 32, 64, 128, 256 };
        //final int[] batches = new int[] { 1, 2, 4, 8, 16, 32, 64 };
        //final int[] batches = new int[] { 48, 80, 96 };
        //final int[] batches = new int[] { 128 };
        final int mongodbDatabaseId = 4;

        Database database = databaseService.find(mongodbDatabaseId);
        AbstractPullWrapper pullWrapper = wrapperService.getPullWraper(database);

        for (int i = 0; i < repetitions; i++) {
            LOGGER.info("Repetition: " + (i + 1));

            for (var batch : batches) {
                
                // Create mapping and schema
                Assertions.assertDoesNotThrow(() -> setMapping());
                var schema = mapping.category();

                // Load data to instance
                Statistics.start(Interval.IMPORT_JOIN_MOVE);

                var process = new DatabaseToInstance();
                process.setLimit(batch * 1000);
                process.input(mapping, null, pullWrapper, null);

                var category = process.run().data;

                // JOIN
                Statistics.start(Interval.JOIN);
                join(schema, category);
                Statistics.end(Interval.JOIN);
                
                Statistics.start(Interval.MOVE);
                move(schema, category);
                Statistics.end(Interval.MOVE);
                Statistics.end(Interval.IMPORT_JOIN_MOVE);

                var message = "#" + Statistics.getInfo(Counter.JOIN_ROWS)
                    + ", " + Statistics.getInfo(Counter.MOVE_ROWS)
                    + ", " + Statistics.getInfo(Interval.JOIN)
                    + ", " + Statistics.getInfo(Interval.MOVE)
                    + ", " + Statistics.getInfo(Interval.IMPORT_JOIN_MOVE);

                LOGGER.info(message);
                Statistics.logInfo(Counter.PULLED_RECORDS);

                /*
                Statistics.logInfo(Interval.JOIN);
                Statistics.logInfo(Interval.MOVE);
                Statistics.logInfo(Interval.IMPORT_JOIN_MOVE);
                Statistics.logInfo(Counter.JOIN_ROWS);
                */
            }
        }

        //LOGGER.info(instance.toString());

        /*
        var finalMapping = data.createFinalMapping(schema);
        var instanceToDatabase = new InstanceToDatabase();
        instanceToDatabase.input(finalMapping, instance, ddlWrapper, pushWrapper);
        var finalResult = instanceToDatabase.run();
        */
        //LOGGER.info(finalResult.data);
    }

    private void join(SchemaCategory schema, InstanceCategory category) {
        var fullAddress = TestData.addSchemaObject(schema, data.fullAddressKey, "fullAddress", Id.createEmpty());
        var address = schema.getObject(data.addressKey);
        var addressToFullAddressMorphism = TestData.addMorphismWithDual(schema, data.addressToFullAddress, address, fullAddress, Min.ONE, Max.ONE, Min.ONE, Max.ONE);
        
        var fullAddressInstance = new InstanceObject(fullAddress);
        category.objects().put(fullAddressInstance.key(), fullAddressInstance);

        var addressInstance = category.getObject(data.addressKey);
        var addressToFullAddressMorphismInstance = createInstanceMorphismWithDual(category, addressToFullAddressMorphism);

        Statistics.set(Counter.JOIN_ROWS, addressInstance.allRows().size());
        for (var addressRow : addressInstance.allRows()) {
            // Get new value
            // TODO - tímto se vyhledává pouze v id daných řádků ... chtělo by to udělat funkci, která se automaticky podívá i do morfizmů
            // ideálně bychom měli tyto morfizmy uchovávat na řádcích
            var street = addressRow.getValue(data.addressToStreet);
            var city = addressRow.getValue(data.addressToCity);
            var fullAddressValue = joinFunction(street, city);

            // Create superId and row
            var builder = new IdWithValues.Builder();
            builder.add(Signature.createEmpty(), fullAddressValue);

            addressInstance.getOrCreateRowWithMorphism(builder.build(), addressRow, addressToFullAddressMorphismInstance);
        }
    }

    private void move(SchemaCategory schema, InstanceCategory category) {
        // Link order with full address
        var order = schema.getObject(data.orderKey);
        var fullAddress = schema.getObject(data.fullAddressKey);
        var orderToFullAddressMorphism = TestData.addMorphismWithDual(schema, data.orderToFullAddress, order, fullAddress, Min.ONE, Max.ONE, Min.ONE, Max.ONE);

        var orderToFullAddressMorphismInstance = createInstanceMorphismWithDual(category, orderToFullAddressMorphism);

        Statistics.set(Counter.MOVE_ROWS, category.getObject(data.orderKey).allRows().size());

        var originalSignature = Signature.concatenate(data.userToOrder.dual(), data.userToAddress, data.addressToFullAddress);
        var morphismPath = category.getMorphism(originalSignature);
        
        for (var orderRow : category.getObject(data.orderKey).allRows()) {
            var optionalRow = orderRow.traverseThrough(morphismPath).stream().findFirst();
            var fullAddressRow = optionalRow.get();
            orderToFullAddressMorphismInstance.addMapping(new MappingRow(orderRow, fullAddressRow));
            orderToFullAddressMorphismInstance.dual().addMapping(new MappingRow(fullAddressRow, orderRow));
        }

        // Delete address to full address
        var addressToFullAddress = schema.getMorphism(data.addressToFullAddress);
        schema.deleteMorphism(addressToFullAddress);
        schema.deleteMorphism(addressToFullAddress.dual());

        var addressToFullAddressInstance = category.getMorphism(addressToFullAddress);
        category.deleteMorphism(addressToFullAddressInstance);
        category.deleteMorphism(addressToFullAddressInstance.dual());
    }

    private static InstanceMorphism createInstanceMorphismWithDual(InstanceCategory category, SchemaMorphism morphism) {
        var dom = category.getObject(morphism.dom());
        var cod = category.getObject(morphism.cod());

        var morphismInstance = new InstanceMorphism(morphism, dom, cod, category);
        var dualInstance = new InstanceMorphism(morphism.dual(), cod, dom, category);
        category.morphisms().put(morphismInstance.signature(), morphismInstance);
        category.morphisms().put(dualInstance.signature(), dualInstance);

        return morphismInstance;
    }

    private static String joinFunction(String street, String city) {
        return street + " --- " + city;
    }

    private Mapping mapping;

    private void setMapping() throws URISyntaxException {
        mapping = data.createInitialMapping();
    }

}
