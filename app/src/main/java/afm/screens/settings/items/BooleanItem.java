package afm.screens.settings.items;

import java.util.Optional;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;

import lombok.Getter;

import afm.screens.settings.Category;

public class BooleanItem extends Item {

	@Getter
	private final Class<Boolean> type = boolean.class;

	@Getter
	final BooleanProperty property = new SimpleBooleanProperty();

	public BooleanItem(Category category, String name) {
		super(category);
		this.name = name;
	}

	public BooleanItem(Category category, String name, String description) {
		this(category, name);
		this.description = description;
	}

	@Override
	public Boolean getValue() {
		return property.getValue();
	}

	@Override
	public void setValue(Object o) {
		if (o instanceof Boolean) {
			property.setValue((Boolean) o);
		} else if (o == null) {
			property.setValue(null);
		}
	}

	@Override
	public Optional<ObservableValue<?>> getObservableValue() {
		return Optional.of(property);
	}
}
