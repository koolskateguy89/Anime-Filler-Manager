package afm.screens.settings.items;

import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;

import lombok.Getter;
import lombok.NonNull;

import afm.screens.settings.Category;

public class BooleanItem extends Item {

	@Getter
	private final Class<Boolean> type = boolean.class;

	@Getter
	final BooleanProperty property = new SimpleBooleanProperty();

	public BooleanItem(@Nonnull Category category, @Nullable String name) {
		super(category);
		this.name = name;
	}

	public BooleanItem(@Nonnull Category category, @Nullable String name, @Nullable String description) {
		this(category, name);
		this.description = description;
	}

	@Override
	public @NonNull Boolean getValue() {
		return property.get();
	}

	@Override
	public void setValue(@Nullable Object o) {
		if (o instanceof Boolean) {
			// will be false if null
			property.setValue((Boolean) o);
		} else if (o == null) {
			property.setValue(null);
		}
	}

	@Override
	public @Nonnull Optional<ObservableValue<?>> getObservableValue() {
		return Optional.of(property);
	}
}
