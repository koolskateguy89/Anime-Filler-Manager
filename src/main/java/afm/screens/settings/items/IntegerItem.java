package afm.screens.settings.items;

import java.util.Optional;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;

import lombok.Getter;

import afm.screens.settings.Category;

public class IntegerItem extends Item {

	@Getter
	private final Class<?> type = int.class;

	@Getter
	IntegerProperty property = new SimpleIntegerProperty();

	public IntegerItem(Category category, String name) {
		super(category);
		this.name = name;
	}

	public IntegerItem(Category category, String name, String description) {
		this(category, name);
		this.description = description;
	}

	@Override
	public Integer getValue() {
		return property.getValue();
	}

	@Override
	public void setValue(Object o) {
		if (o instanceof Integer) {
			property.setValue((Integer) o);
		} else if (o instanceof Number) {
			property.set(((Number) o).intValue());
		} else if (o == null) {
			property.setValue(null);
		}
	}

	@Override
	public Optional<ObservableValue<?>> getObservableValue() {
		return Optional.of(property);
	}

}
