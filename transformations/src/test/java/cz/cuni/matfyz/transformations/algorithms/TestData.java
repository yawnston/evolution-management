package cz.cuni.matfyz.transformations.algorithms;

import cz.cuni.matfyz.core.category.Morphism.Max;
import cz.cuni.matfyz.core.category.Morphism.Min;
import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.core.instance.DomainRow;
import cz.cuni.matfyz.core.instance.InstanceCategory;
import cz.cuni.matfyz.core.instance.InstanceCategoryBuilder;
import cz.cuni.matfyz.core.mapping.ComplexProperty;
import cz.cuni.matfyz.core.mapping.SimpleProperty;
import cz.cuni.matfyz.core.mapping.StaticName;
import cz.cuni.matfyz.core.schema.Id;
import cz.cuni.matfyz.core.schema.Key;
import cz.cuni.matfyz.core.schema.SchemaCategory;
import cz.cuni.matfyz.core.schema.SchemaMorphism;
import cz.cuni.matfyz.core.schema.SchemaObject;

import java.util.Set;

/**
 * @author jachymb.bartik
 */
public class TestData {

    private final Key customerKey = new Key(100);
    private final Key idKey = new Key(101);
    private final Key orderedKey = new Key(102);
    private final Key orderKey = new Key(103);
    private final Key numberKey = new Key(104);
    private final Key arrayKey = new Key(105);
    private final Key contactKey = new Key(106);
    private final Key valueKey = new Key(107);
    private final Key typeKey = new Key(108);
    private final Key nameKey = new Key(109);
    private final Key nestedDocKey = new Key(110);
    private final Key propertyAKey = new Key(111);
    private final Key propertyBKey = new Key(112);
    private final Key propertyCKey = new Key(113);
    private final Key itemsKey = new Key(114);
    private final Key quantityKey = new Key(115);
    private final Key productKey = new Key(116);
    private final Key pidKey = new Key(117);
    private final Key pnameKey = new Key(118);
    private final Key priceKey = new Key(119);
    private final Key addressKey = new Key(120);
    private final Key labelKey = new Key(121);
    private final Key contentKey = new Key(122);
    private final Key textKey = new Key(123);
    private final Key localeKey = new Key(124);
    
    private final Signature customerToId = new Signature(1);
    private final Signature customerToOrdered = new Signature(2);
    private final Signature orderedToOrder = new Signature(3);
    private final Signature orderToNestedDoc = new Signature(4);
    private final Signature nestedDocToPropertyA = new Signature(5);
    private final Signature nestedDocToPropertyB = new Signature(6);
    private final Signature nestedDocToPropertyC = new Signature(7);
    private final Signature orderToItems = new Signature(8);
    private final Signature itemsToProduct = new Signature(9);
    private final Signature productToPid = new Signature(10);
    private final Signature productToPrice = new Signature(11);
    private final Signature productToPname = new Signature(12);
    private final Signature itemsToQuantity = new Signature(13);
    private final Signature orderToContact = new Signature(14);
    private final Signature contactToType = new Signature(15);
    private final Signature typeToName = new Signature(16);
    private final Signature contactToValue = new Signature(17);
    private final Signature orderToArray = new Signature(18);
    private final Signature orderToNumber = new Signature(19);
    private final Signature orderToAddress = new Signature(20);
    private final Signature addressToLabel = new Signature(21);
    private final Signature addressToContent = new Signature(22);
    private final Signature contentToText = new Signature(23);
    private final Signature contentToLocale = new Signature(24);
    
    private final Signature itemsToNumber = orderToItems.dual().concatenate(orderToNumber);
    private final Signature itemsToPid = itemsToProduct.concatenate(productToPid);
    private final Signature itemsToPname = itemsToProduct.concatenate(productToPname);
    private final Signature itemsToPrice = itemsToProduct.concatenate(productToPrice);
    
    private final Signature contactToNumber = orderToContact.dual().concatenate(orderToNumber);
    private final Signature contactToName = contactToType.concatenate(typeToName);
    
    private final Signature orderedToNumber = orderedToOrder.concatenate(orderToNumber);
    private final Signature orderedToId = customerToOrdered.dual().concatenate(customerToId);
    private final Signature orderToId = orderedToOrder.dual().concatenate(orderedToId);
    private final Signature orderToCustomer = orderedToOrder.dual().concatenate(customerToOrdered.dual()).concatenate(customerToId);
    
    private final Signature addressToNumber = orderToAddress.dual().concatenate(orderToNumber);

    private enum Cardinality {
        ONE_TO_ONE,
        ONE_TO_MANY,
        MANY_TO_ONE,
        MANY_TO_MANY
    }

    public SchemaCategory createDefaultSchemaCategory() {
        var schema = new SchemaCategory();
        var order = buildOrder(schema);
        addArray(schema, order);
        addItems(schema, order);
        addContact(schema, order);
        addNestedDoc(schema, order);
        addOrdered(schema, order);
        addAddress(schema, order);

        return schema;
    }

    public Key getOrderKey() {
        return orderKey;
    }

    private SchemaObject buildOrder(SchemaCategory schema) {
        var order = createSchemaObject(
            orderKey,
            "Order",
            new Id(orderToNumber)
        );
        schema.addObject(order);
        
        var number = createSchemaObject(
            numberKey,
            "Number",
            Id.createEmpty()
        );
        schema.addObject(number);
        addMorphismWithDual(schema, orderToNumber, order, number, Cardinality.ONE_TO_ONE);
        
        return order;
    }

    private void addArray(SchemaCategory schema, SchemaObject order) {
        var array = createSchemaObject(
            arrayKey,
            "Array",
            Id.createEmpty()
        );
        schema.addObject(array);
        addMorphismWithDual(schema, orderToArray, order, array, Cardinality.ONE_TO_MANY);
    }
    
    private void addItems(SchemaCategory schema, SchemaObject order) {
        //var number = schema.getObject(numberKey);

        var items = createSchemaObject(
            itemsKey,
            "Items",
            new Id(itemsToNumber, itemsToPid)
        );
        schema.addObject(items);
        addMorphismWithDual(schema, orderToItems, order, items, Cardinality.ONE_TO_MANY);
        //addMorphismWithDual(schema, itemsToNumber, items, number, Cardinality.MANY_TO_ONE);
        
        var quantity = createSchemaObject(
            quantityKey,
            "Quantity",
            Id.createEmpty()
        );
        schema.addObject(quantity);
        addMorphismWithDual(schema, itemsToQuantity, items, quantity, Cardinality.MANY_TO_ONE);
        
        var product = createSchemaObject(
            productKey,
            "Product",
            new Id(productToPid)
        );
        schema.addObject(product);
        addMorphismWithDual(schema, itemsToProduct, items, product, Cardinality.MANY_TO_ONE);
        
        var pid = createSchemaObject(
            pidKey,
            "Id",
            Id.createEmpty()
        );
        schema.addObject(pid);
        addMorphismWithDual(schema, productToPid, product, pid, Cardinality.ONE_TO_ONE);
        //addMorphismWithDual(schema, itemsToPid, items, pid, Cardinality.MANY_TO_ONE);

        var price = createSchemaObject(
            priceKey,
            "Price",
            Id.createEmpty()
        );
        schema.addObject(price);
        addMorphismWithDual(schema, productToPrice, product, price, Cardinality.MANY_TO_ONE);
        //addMorphismWithDual(schema, itemsToPrice, items, price, Cardinality.MANY_TO_ONE);

        var pname = createSchemaObject(
            pnameKey,
            "Name",
            Id.createEmpty()
        );
        schema.addObject(pname);
        addMorphismWithDual(schema, productToPname, product, pname, Cardinality.MANY_TO_ONE);
        //addMorphismWithDual(schema, itemsToPname, items, pname, Cardinality.MANY_TO_ONE);
    }
    
    private void addContact(SchemaCategory schema, SchemaObject order) {
        var contact = createSchemaObject(
            contactKey,
            "Contact",
            new Id(contactToNumber, contactToValue, contactToName)
        );
        schema.addObject(contact);
        addMorphismWithDual(schema, orderToContact, order, contact, Cardinality.MANY_TO_MANY);

        var value = createSchemaObject(
            valueKey,
            "Value",
            Id.createEmpty()
        );
        schema.addObject(value);
        addMorphismWithDual(schema, contactToValue, contact, value, Cardinality.ONE_TO_ONE);

        var type = createSchemaObject(
            typeKey,
            "Type",
            new Id(typeToName)
        );
        schema.addObject(type);
        addMorphismWithDual(schema, contactToType, contact, type, Cardinality.MANY_TO_ONE);

        var name = createSchemaObject(
            nameKey,
            "Name",
            Id.createEmpty()
        );
        schema.addObject(name);
        addMorphismWithDual(schema, typeToName, type, name, Cardinality.ONE_TO_ONE);
        addMorphismWithDual(schema, contactToName, contact, name, Cardinality.MANY_TO_ONE);
    }
    
    private void addNestedDoc(SchemaCategory schema, SchemaObject order) {   
        var nestedDoc = createSchemaObject(
            nestedDocKey,
            "NestedDoc",
            Id.createEmpty()
        );
        schema.addObject(nestedDoc);
        addMorphismWithDual(schema, orderToNestedDoc, order, nestedDoc, Cardinality.ONE_TO_ONE);
        
        var propertyA = createSchemaObject(
            propertyAKey,
            "PropertyA",
            Id.createEmpty()
        );
        schema.addObject(propertyA);
        addMorphismWithDual(schema, nestedDocToPropertyA, nestedDoc, propertyA, Cardinality.ONE_TO_ONE);

        var propertyB = createSchemaObject(
            propertyBKey,
            "PropertyB",
            Id.createEmpty()
        );
        schema.addObject(propertyB);
        addMorphismWithDual(schema, nestedDocToPropertyB, nestedDoc, propertyB, Cardinality.ONE_TO_ONE);

        var propertyC = createSchemaObject(
            propertyCKey,
            "PropertyC",
            Id.createEmpty()
        );
        schema.addObject(propertyC);
        addMorphismWithDual(schema, nestedDocToPropertyC, nestedDoc, propertyC, Cardinality.ONE_TO_ONE);
    }
    
    private void addOrdered(SchemaCategory schema, SchemaObject order) {
        var ordered = createSchemaObject(
            orderedKey,
            "Ordered",
            new Id(orderedToNumber, orderedToId)
        );
        schema.addObject(ordered);
        addMorphismWithDual(schema, orderedToOrder.dual(), order, ordered, Cardinality.ONE_TO_ONE);

        var customer = createSchemaObject(
            customerKey,
            "Customer",
            new Id(customerToId)
        );
        schema.addObject(customer);
        addMorphismWithDual(schema, customerToOrdered.dual(), ordered, customer, Cardinality.ONE_TO_MANY);

        var id = createSchemaObject(
            idKey,
            "Id",
            Id.createEmpty()
        );
        schema.addObject(id);
        addMorphismWithDual(schema, customerToId, customer, id, Cardinality.ONE_TO_ONE);
        addMorphismWithDual(schema, orderToId, order, id, Cardinality.MANY_TO_ONE);
    }

    private void addAddress(SchemaCategory schema, SchemaObject order) {
        var address = createSchemaObject(
            addressKey,
            "address",
            new Id(addressToNumber, addressToLabel)
        );
        schema.addObject(address);
        addMorphismWithDual(schema, orderToAddress, order, address, Cardinality.MANY_TO_MANY);

        var label = createSchemaObject(
            labelKey,
            "label",
            Id.createEmpty()
        );
        schema.addObject(label);
        addMorphismWithDual(schema, addressToLabel, address, label, Cardinality.ONE_TO_ONE);

        var content = createSchemaObject(
            contentKey,
            "content",
            Id.createEmpty()
        );
        schema.addObject(content);
        addMorphismWithDual(schema, addressToContent, address, content, Cardinality.ONE_TO_ONE);
        
        var text = createSchemaObject(
            textKey,
            "text",
            Id.createEmpty()
        );
        schema.addObject(text);
        addMorphismWithDual(schema, contentToText, content, text, Cardinality.ONE_TO_ONE);
        
        var locale = new SchemaObject(
            localeKey,
            "locale",
            Id.createEmpty(),
            Set.of(Id.createEmpty())
        );
        schema.addObject(locale);
        addMorphismWithDual(schema, contentToLocale, content, locale, Cardinality.ONE_TO_ONE);
    }
    
    private SchemaObject createSchemaObject(Key key, String name, Id id) {
        return new SchemaObject(key, name, id, Set.of(id));
    }

    private void addMorphismWithDual(SchemaCategory schema, Signature signature, SchemaObject dom, SchemaObject cod, Cardinality cardinality) {
        switch (cardinality) {
            case ONE_TO_ONE:
                addMorphismWithDual(schema, signature, dom, cod, Min.ONE, Max.ONE, Min.ONE, Max.ONE);
                break;
            case ONE_TO_MANY:
                addMorphismWithDual(schema, signature, dom, cod, Min.ZERO, Max.STAR, Min.ONE, Max.ONE);
                break;
            case MANY_TO_ONE:
                addMorphismWithDual(schema, signature, dom, cod, Min.ONE, Max.ONE, Min.ZERO, Max.STAR);
                break;
            case MANY_TO_MANY:
                addMorphismWithDual(schema, signature, dom, cod, Min.ZERO, Max.STAR, Min.ZERO, Max.STAR);
                break;
        }
    }
    
    private void addMorphismWithDual(SchemaCategory schema, Signature signature, SchemaObject dom, SchemaObject cod, Min min, Max max, Min dualMin, Max dualMax) {
        var builder = new SchemaMorphism.Builder();
        var morphism = builder.fromArguments(signature, dom, cod, min, max);
        var dual = builder.fromDual(morphism, dualMin, dualMax);

        schema.addMorphism(morphism);
        schema.addMorphism(dual);
    }
    
    private InstanceCategory buildInstanceScenario(SchemaCategory schema) {
        return new InstanceCategoryBuilder().setSchemaCategory(schema).build();
    }

    public InstanceCategory expectedInstance_order(SchemaCategory schema) {
        InstanceCategory instance = buildInstanceScenario(schema);
        var builder = new TestInstanceCategoryBuilder(instance);
        
        expectedOrder(builder, "2043");
        expectedOrder(builder, "1653");
        
        return instance;
    }

    private DomainRow expectedOrder(TestInstanceCategoryBuilder builder, String orderNumber) {
        var order = builder.value(orderToNumber, orderNumber).object(orderKey);
        var number = builder.value(Signature.createEmpty(), orderNumber).object(numberKey);
        builder.morphism(orderToNumber, order, number);
        
        return order;
    }

    public InstanceCategory expectedInstance_nestedDoc(SchemaCategory schema) {
        InstanceCategory instance = buildInstanceScenario(schema);
        var builder = new TestInstanceCategoryBuilder(instance);
        
        expectedOrder_nestedDoc(builder, "2043", "0", "hodnotaA", "hodnotaB", "hodnotaC");
        expectedOrder_nestedDoc(builder, "1653", "1", "hodnotaA2", "hodnotaB2", "hodnotaC2");
        
        return instance;
    }

    private void expectedOrder_nestedDoc(TestInstanceCategoryBuilder builder, String orderNumber, String uniqueId, String valueA, String valueB, String valueC) {
        var order = expectedOrder(builder, orderNumber);
        var nestedDoc = builder.value(Signature.createEmpty(), uniqueId).object(nestedDocKey);
        var propertyA = builder.value(Signature.createEmpty(), valueA).object(propertyAKey);
        var propertyB = builder.value(Signature.createEmpty(), valueB).object(propertyBKey);
        var propertyC = builder.value(Signature.createEmpty(), valueC).object(propertyCKey);
        
        builder.morphism(orderToNestedDoc, order, nestedDoc);
        builder.morphism(nestedDocToPropertyA, nestedDoc, propertyA);
        builder.morphism(nestedDocToPropertyB, nestedDoc, propertyB);
        builder.morphism(nestedDocToPropertyC, nestedDoc, propertyC);
    }

    public InstanceCategory expectedInstance_array(SchemaCategory schema) {
        InstanceCategory instance = buildInstanceScenario(schema);
        var builder = new TestInstanceCategoryBuilder(instance);
        
        expectedOrder_array(builder, "2043", new String[]{ "123", "456", "789" });
        expectedOrder_array(builder, "1653", new String[]{ "123", "String456", "String789" });
        
        return instance;
    }

    private void expectedOrder_array(TestInstanceCategoryBuilder builder, String orderNumber, String[] array) {
        var order = expectedOrder(builder, orderNumber);
        for (String value : array) {
            var arrayItem = builder.value(Signature.createEmpty(), value).object(arrayKey);
            builder.morphism(orderToArray, order, arrayItem);
        }
    }

    public InstanceCategory expectedInstance_items(SchemaCategory schema) {
        InstanceCategory instance = buildInstanceScenario(schema);
        var builder = new TestInstanceCategoryBuilder(instance);

        var order = expectedOrder(builder, "2043");
        expectedItem(builder, order, "2043", "123", "Toy", "125", "1");
        expectedItem(builder, order, "2043", "765", "Book", "199", "2");
        expectedItem(builder, order, "2043", "457", "Knife", "299", "7");
        expectedItem(builder, order, "2043", "734", "Doll", "350", "3");
        
        return instance;
    }

    private void expectedItem(TestInstanceCategoryBuilder builder, DomainRow order, String orderNumber, String pidValue, String pnameValue, String priceValue, String quantityValue) {
        var items = builder.value(itemsToNumber, orderNumber).value(itemsToPid, pidValue).object(itemsKey);
        builder.morphism(orderToItems, order, items);
        var quantity = builder.value(Signature.createEmpty(), quantityValue).object(quantityKey);
        builder.morphism(itemsToQuantity, items, quantity);

        var product = builder.value(productToPid, pidValue).object(productKey);
        builder.morphism(itemsToProduct, items, product);

        var pid = builder.value(Signature.createEmpty(), pidValue).object(pidKey);
        builder.morphism(productToPid, product, pid);
        // Empty morphisms are needed because of string comparison of actual and expected instance categories.
        builder.morphism(itemsToPid);
        var pname = builder.value(Signature.createEmpty(), pnameValue).object(pnameKey);
        builder.morphism(productToPname, product, pname);
        builder.morphism(itemsToPname);
        var price = builder.value(Signature.createEmpty(), priceValue).object(priceKey);
        builder.morphism(productToPrice, product, price);
        builder.morphism(itemsToPrice);
    }

    public InstanceCategory expectedInstance_contact(SchemaCategory schema) {
        InstanceCategory instance = buildInstanceScenario(schema);
        var builder = new TestInstanceCategoryBuilder(instance);
        
        var order1 = expectedOrder(builder, "2043");
        addExpectedContact(builder, order1, "2043", "anna@seznam.cz", "email");
        addExpectedContact(builder, order1, "2043", "+420777123456", "cellphone");
        
        var order2 = expectedOrder(builder, "1653");
        addExpectedContact(builder, order2, "1653", "skype123", "skype");
        addExpectedContact(builder, order2, "1653", "+420123456789", "cellphone");
        
        return instance;
    }

    private void addExpectedContact(TestInstanceCategoryBuilder builder, DomainRow order, String numberValue, String valueValue, String nameValue) {
        var contact = builder.value(contactToNumber, numberValue).value(contactToValue, valueValue).value(contactToName, nameValue).object(contactKey);
        var value = builder.value(Signature.createEmpty(), valueValue).object(valueKey);
        var name = builder.value(Signature.createEmpty(), nameValue).object(nameKey);
        var type = builder.value(typeToName, nameValue).object(typeKey);
        
        builder.morphism(contactToValue, contact, value);
        builder.morphism(contactToType, contact, type);
        builder.morphism(typeToName, type, name);
        builder.morphism(orderToContact, order, contact);
    }

    public InstanceCategory expectedInstance_ordered(SchemaCategory schema) {
        InstanceCategory instance = buildInstanceScenario(schema);
        var builder = new TestInstanceCategoryBuilder(instance);
        
        expectedOrder_ordered(builder, "2043", "1");
        expectedOrder_ordered(builder, "1653", "1");
        
        return instance;
    }

    public DomainRow expectedOrder_ordered(TestInstanceCategoryBuilder builder, String orderNumber, String customerId) {
        var order = expectedOrder(builder, orderNumber);
        var id = builder.value(Signature.createEmpty(), customerId).object(idKey);
        var customer = builder.value(customerToId, customerId).object(customerKey);
        var ordered = builder.value(orderedToId, customerId).value(orderedToNumber, orderNumber).object(orderedKey);

        //builder.morphism(orderToId, order, id);
        builder.morphism(orderedToOrder, ordered, order);
        builder.morphism(customerToOrdered, customer, ordered);
        builder.morphism(customerToId, customer, id);

        return order;
    }

    public InstanceCategory expectedInstance_nestedDocMissingSimple(SchemaCategory schema) {
        InstanceCategory instance = buildInstanceScenario(schema);
        var builder = new TestInstanceCategoryBuilder(instance);
        
        var order1 = expectedOrder(builder, "2043");
        var nestedDoc1 = builder.value(Signature.createEmpty(), "0").object(nestedDocKey);
        var propertyA1 = builder.value(Signature.createEmpty(), "hodnotaA").object(propertyAKey);
        var propertyC1 = builder.value(Signature.createEmpty(), "hodnotaC").object(propertyCKey);
        
        builder.morphism(orderToNestedDoc, order1, nestedDoc1);
        builder.morphism(nestedDocToPropertyA, nestedDoc1, propertyA1);
        builder.morphism(nestedDocToPropertyC, nestedDoc1, propertyC1);
        
        var order2 = expectedOrder(builder, "1653");
        var nestedDoc2 = builder.value(Signature.createEmpty(), "1").object(nestedDocKey);
        var propertyA2 = builder.value(Signature.createEmpty(), "hodnotaA2").object(propertyAKey);
        var propertyC2 = builder.value(Signature.createEmpty(), "hodnotaC2").object(propertyCKey);
        
        builder.morphism(orderToNestedDoc, order2, nestedDoc2);
        builder.morphism(nestedDocToPropertyA, nestedDoc2, propertyA2);
        builder.morphism(nestedDocToPropertyC, nestedDoc2, propertyC2);
        
        return instance;
    }

    public InstanceCategory expectedInstance_nestedDocMissingComplex(SchemaCategory schema) {
        InstanceCategory instance = buildInstanceScenario(schema);
        var builder = new TestInstanceCategoryBuilder(instance);
        
        expectedOrder(builder, "2043");
        expectedOrder(builder, "1653");
        
        return instance;
    }

    public InstanceCategory expectedInstance_itemsEmpty(SchemaCategory schema) {
        InstanceCategory instance = buildInstanceScenario(schema);
        var builder = new TestInstanceCategoryBuilder(instance);
        
        expectedOrder(builder, "2043");
        
        return instance;
    }

    public InstanceCategory expectedInstance_address(SchemaCategory schema) {
        InstanceCategory instance = buildInstanceScenario(schema);
        var builder = new TestInstanceCategoryBuilder(instance);
        
        var order1 = expectedOrder(builder, "2043");
        addExpectedAddress(builder, order1, "0", "2043", "city", "Praha", "cs");
        addExpectedAddress(builder, order1, "1", "2043", "country", "Czech republic", "en");

        var order2 = expectedOrder(builder, "1653");
        addExpectedAddress(builder, order2, "2", "1653", "location", "Praha", "cs");
        addExpectedAddress(builder, order2, "3", "1653", "country", "??esk?? republika", "cs");
        
        return instance;
    }

    private void addExpectedAddress(TestInstanceCategoryBuilder builder, DomainRow order, String uniqueId, String number, String label, String text, String locale) {
        var address = builder.value(addressToNumber, number).value(addressToLabel, label).object(addressKey);
        var labelRow = builder.value(Signature.createEmpty(), label).object(labelKey);
        var contentRow = builder.value(Signature.createEmpty(), uniqueId).object(contentKey);
        var textRow = builder.value(Signature.createEmpty(), text).object(textKey);
        var localeRow = builder.value(Signature.createEmpty(), locale).object(localeKey);
        
        builder.morphism(addressToLabel, address, labelRow);
        builder.morphism(addressToContent, address, contentRow);
        builder.morphism(contentToText, contentRow, textRow);
        builder.morphism(contentToLocale, contentRow, localeRow);
        builder.morphism(orderToAddress, order, address);
    }

    public InstanceCategory expectedInstance_itemsMissing(SchemaCategory schema) {
        InstanceCategory instance = buildInstanceScenario(schema);
        var builder = new TestInstanceCategoryBuilder(instance);
        
        var order = expectedOrder(builder, "2043");
        expectedItemMissing(builder, order, "2043", "123", "Toy", "125", null);
        expectedItemMissing(builder, order, "2043", "456", null, "500", "4");
        expectedItemMissing(builder, order, "2043", "765", "Book", null, "2");
        expectedItemMissing(builder, order, "2043", "457", null, "299", "7");
        expectedItemMissing(builder, order, "2043", "734", null, "350", "3");
        
        return instance;
    }

    private void expectedItemMissing(TestInstanceCategoryBuilder builder, DomainRow order, String orderNumber, String pidValue, String pnameValue, String priceValue, String quantityValue) {
        var items = builder.value(itemsToNumber, orderNumber).value(itemsToPid, pidValue).object(itemsKey);
        builder.morphism(orderToItems, order, items);

        if (quantityValue != null) {
            var quantity = builder.value(Signature.createEmpty(), quantityValue).object(quantityKey);
            builder.morphism(itemsToQuantity, items, quantity);
        }

        var product = builder.value(productToPid, pidValue).object(productKey);
        builder.morphism(itemsToProduct, items, product);

        if (pidValue != null) {
            var pid = builder.value(Signature.createEmpty(), pidValue).object(pidKey);
            builder.morphism(productToPid, product, pid);
            builder.morphism(itemsToPid);
        }

        if (pnameValue != null) {
            var pname = builder.value(Signature.createEmpty(), pnameValue).object(pnameKey);
            builder.morphism(productToPname, product, pname);
            builder.morphism(itemsToPname);
        }

        if (priceValue != null) {
            var price = builder.value(Signature.createEmpty(), priceValue).object(priceKey);
            builder.morphism(productToPrice, product, price);
            builder.morphism(itemsToPrice);
        }
    }

    public ComplexProperty path_order() {
        return new ComplexProperty(StaticName.createAnonymous(), Signature.createNull(),
            new SimpleProperty("number", orderToNumber)
        );
    }

    public ComplexProperty path_nestedDoc() {
        return new ComplexProperty(StaticName.createAnonymous(), Signature.createNull(),
            new SimpleProperty("number", orderToNumber),
            new ComplexProperty("nested", orderToNestedDoc,
                new SimpleProperty("propertyA", nestedDocToPropertyA),
                new SimpleProperty("propertyB", nestedDocToPropertyB),
                new SimpleProperty("propertyC", nestedDocToPropertyC)
            )
        );
    }

    public ComplexProperty path_array() {
        return new ComplexProperty(StaticName.createAnonymous(), Signature.createNull(),
            new SimpleProperty("number", orderToNumber),
            new SimpleProperty("array", orderToArray)
        );
    }

    public ComplexProperty path_items() {
        return new ComplexProperty(StaticName.createAnonymous(), Signature.createNull(),
            new SimpleProperty("number", orderToNumber),
            new ComplexProperty("items", orderToItems,
                new SimpleProperty("productId", itemsToPid),
                new SimpleProperty("name", itemsToPname),
                new SimpleProperty("price", itemsToPrice),
                new SimpleProperty("quantity", itemsToQuantity)
            )
        );
    }

    public ComplexProperty path_contact() {
        return new ComplexProperty(StaticName.createAnonymous(), Signature.createNull(),
            new SimpleProperty("number", orderToNumber),
            new ComplexProperty("contact", orderToContact,
                new SimpleProperty(contactToName, contactToValue)
            )
        );
    }

    public ComplexProperty path_ordered() {
        return new ComplexProperty(StaticName.createAnonymous(), Signature.createNull(),
            new ComplexProperty("_id", Signature.createNull(),
                new SimpleProperty("customer", orderToCustomer),
                new SimpleProperty("number", orderToNumber)
            )
        );
    }

    public ComplexProperty path_address() {
        return new ComplexProperty(StaticName.createAnonymous(), Signature.createNull(),
            new SimpleProperty("number", orderToNumber),
            new ComplexProperty("address", orderToAddress,
                new ComplexProperty(addressToLabel, addressToContent,
                    new SimpleProperty("text", contentToText),
                    new SimpleProperty("locale", contentToLocale)
                )
            )
        );
    }
}
