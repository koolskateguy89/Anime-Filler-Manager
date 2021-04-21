package afm.screens.settings.items;

import java.util.Optional;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ObservableValue;

import lombok.Getter;

import afm.screens.settings.Category;

public class DoubleItem extends Item {

	@Getter
	private final Class<?> type = double.class;

	@Getter
	DoubleProperty property = new SimpleDoubleProperty();

	public DoubleItem(Category category, String name) {
		super(category);
		this.name = name;
	}

	public DoubleItem(Category category, String name, String description) {
		this(category, name);
		this.description = description;
	}

	@Override
	public Double getValue() {
		return property.getValue();
	}

	@Override
	public void setValue(Object o) {
		if (o instanceof Double) {
			property.setValue((Double) o);
		} else if (o instanceof Number) {
			property.set(((Number) o).doubleValue());
		} else if (o == null) {
			property.setValue(null);
		}
	}

	@Override
	public Optional<ObservableValue<?>> getObservableValue() {
		return Optional.of(property);
	}

}
