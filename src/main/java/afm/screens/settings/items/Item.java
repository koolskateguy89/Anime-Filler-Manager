package afm.screens.settings.items;

import org.controlsfx.control.PropertySheet;

import lombok.Getter;

import afm.screens.settings.Category;

@Getter
public abstract class Item implements PropertySheet.Item {

	protected String name;

	protected String description;

	protected String category;

	protected Item(Category category) {
		this.category = category.toString();
	}

	public abstract <T> T getProperty();

}
