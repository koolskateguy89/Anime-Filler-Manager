package afm.screens.settings.items;

import java.util.Optional;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;

import lombok.Getter;

import afm.screens.settings.Category;

public class BooleanItem extends Item {

	@Getter
	private final Class<?> type = boolean.class;

	@Getter
	BooleanProperty property = new SimpleBooleanProperty();

	boolean value;

	public BooleanItem(Category category, String name) {
		super(category);
		this.name = name;

		property.addListener((obs, oldVal, newVal) ->
			value = Boolean.TRUE.equals(newVal)
		);
	}

	public BooleanItem(Category category, String name, String description) {
		this(category, name);
		this.description = description;
	}

	@Override
	public Boolean getValue() {
		return value;
	}

	@Override
	public void setValue(Object o) {
		if (o instanceof Boolean) {
			value = (Boolean) o;
		} else if (o == null) {
			value = false;
		}
		property.setValue(value);
	}

	@Override
	public Optional<ObservableValue<?>> getObservableValue() {
		return Optional.of(property);
	}
}
