package cz.cuni.matfyz.editor.model.widgets;

import cz.cuni.matfyz.editor.model.Widget;

/**
 *
 * @author pavel.koupil
 */
public class CategoricalObjectWidget extends Widget {

	public CategoricalObjectWidget(String type) {
		super(type);
	}

	public CategoricalObjectWidget(String type, String id, String name, double x, double y, double width, double height) {
		super(type, id, name, x, y, width, height);
	}

}
