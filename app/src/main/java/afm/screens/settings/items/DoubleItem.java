package afm.screens.settings.items;

import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ObservableValue;

import lombok.Getter;

import afm.screens.settings.Category;

public class DoubleItem extends Item {

	@Getter
	private final Class<Double> type = double.class;

	@Getter
	final DoubleProperty property = new SimpleDoubleProperty();

	public DoubleItem(@Nonnull Category category, @Nonnull String name) {
		super(category);
		this.name = name;
	}

	public DoubleItem(@Nonnull Category category, @Nonnull String name, @Nonnull String description) {
		this(category, name);
		this.description = description;
	}

	@Override
	public @Nonnull Double getValue() {
		return property.getValue();
	}

	@Override
	public void setValue(@Nullable Object o) {
		if (o instanceof Double) {
			property.setValue((Double) o);
		} else if (o instanceof Number) {
			property.set(((Number) o).doubleValue());
		} else if (o == null) {
			property.setValue(null);
		}
	}

	@Override
	public @Nonnull Optional<ObservableValue<?>> getObservableValue() {
		return Optional.of(property);
	}

}
