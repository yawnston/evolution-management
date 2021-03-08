/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.transformations.algorithms2.model;

import cat.transformations.algorithms2.schema.AbstractSchema;
import cat.transformations.algorithms2.schema.RelationalSchema;

/**
 *
 * @author pavel.contos
 */
public class RelationalModel implements AbstractModel {

	private final AbstractSchema schema;

	public RelationalModel(RelationalSchema schema) {
		this.schema = schema;
	}

	@Override
	public AbstractSchema getSchema() {
		return schema;
	}

	@Override
	public Iterable<String> getKindNames() {
		return schema.getKindNames();
	}

	@Override
	public AbstractKind getKind(String name) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public Iterable<AbstractKind> getKinds() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public void putKind(String name, AbstractKind kind) {
		// TODO: uprav schema? over schema? mel bys mit dva druhy metod, jedna s upravou schematu a druha bez upravy schematu...
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public boolean isSchemaRequired() {
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(schema);
		builder.append("INSTANCE-TODO-NENI-ZOBRAZENA!\n");

		return builder.toString();
	}

}