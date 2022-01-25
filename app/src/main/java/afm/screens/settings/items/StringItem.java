package afm.screens.settings.items;

import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;

import lombok.Getter;

import afm.screens.settings.Category;

public class StringItem extends Item {

	@Getter
	private final Class<String> type = String.class;

	@Getter
	final StringProperty property = new SimpleStringProperty();

	public StringItem(@Nonnull Category category, @Nonnull String name) {
		super(category);
		this.name = name;
	}

	public StringItem(@Nonnull Category category, @Nonnull String name, @Nonnull String description) {
		this(category, name);
		this.description = description;
	}

	@Override
	public @Nullable String getValue() {
		return property.getValue();
	}

	@Override
	public void setValue(@Nullable Object o) {
		property.setValue(String.valueOf(o));
	}

	@Override
	public @Nonnull Optional<ObservableValue<?>> getObservableValue() {
		return Optional.of(property);
	}
	
}
