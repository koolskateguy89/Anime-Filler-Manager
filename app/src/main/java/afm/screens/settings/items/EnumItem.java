package afm.screens.settings.items;

import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;

import lombok.Getter;

import afm.screens.settings.Category;

public class EnumItem<E extends Enum<?>> extends Item {

	@Getter
	private final Class<E> type;

	private final Class<E> clazz;

	@Getter
	final ObjectProperty<E> property = new SimpleObjectProperty<>();

	public EnumItem(@Nonnull Class<E> clazz, @Nonnull Category category, @Nonnull String name) {
		super(category);
		this.name = name;
		this.clazz = clazz;
		this.type = clazz;
	}

	public EnumItem(@Nonnull Class<E> clazz, @Nonnull Category category, @Nonnull String name, @Nonnull String description) {
		this(clazz, category, name);
		this.description = description;
	}

	@Override
	public @Nullable E getValue() {
		return property.getValue();
	}

	@Override
	public void setValue(@Nullable Object o) {
		if (clazz.isInstance(o)) {
			property.setValue(clazz.cast(o));
		} else {
			property.setValue(null);
		}
	}

	@Override
	public @Nonnull Optional<ObservableValue<?>> getObservableValue() {
		return Optional.of(property);
	}
	
}
