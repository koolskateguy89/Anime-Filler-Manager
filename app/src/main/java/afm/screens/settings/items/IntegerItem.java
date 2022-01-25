package afm.screens.settings.items;

import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;

import lombok.Getter;

import afm.screens.settings.Category;

public class IntegerItem extends Item {

	@Getter
	private final Class<Integer> type = int.class;

	@Getter
	final IntegerProperty property = new SimpleIntegerProperty();

	public IntegerItem(@Nonnull Category category, @Nonnull String name) {
		super(category);
		this.name = name;
	}

	public IntegerItem(@Nonnull Category category, @Nonnull String name, @Nonnull String description) {
		this(category, name);
		this.description = description;
	}

	@Override
	public @Nonnull Integer getValue() {
		return property.getValue();
	}

	@Override
	public void setValue(@Nullable Object o) {
		if (o instanceof Integer) {
			property.setValue((Integer) o);
		} else if (o instanceof Number) {
			property.set(((Number) o).intValue());
		} else if (o == null) {
			property.setValue(null);
		}
	}

	@Override
	public @Nonnull Optional<ObservableValue<?>> getObservableValue() {
		return Optional.of(property);
	}

}
