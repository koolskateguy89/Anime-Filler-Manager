package afm.screens.settings.items;

import java.util.Optional;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;

import lombok.Getter;

import afm.screens.settings.Category;

public class StringItem extends Item {

	@Getter
	private final Class<?> type = String.class;

	@Getter
	StringProperty property = new SimpleStringProperty();

	@Getter
	String value;

	public StringItem(Category category, String name) {
		super(category);
		this.name = name;

		property.addListener((obs, oldVal, newVal) ->
			value = newVal
		);
	}

	public StringItem(Category category, String name, String description) {
		this(category, name);
		this.description = description;
	}

	@Override
	public void setValue(Object o) {
		if (o == null) {
			value = null;
		} else {
			value = o.toString();
		}
		property.setValue(value);
	}

	@Override
	public Optional<ObservableValue<?>> getObservableValue() {
		return Optional.of(property);
	}
	
}
