package afm.screens.settings.items;

import java.util.Optional;

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

	public EnumItem(Class<E> clazz, Category category, String name) {
		super(category);
		this.name = name;
		this.clazz = clazz;
		this.type = clazz;
	}

	public EnumItem(Class<E> clazz, Category category, String name, String description) {
		this(clazz, category, name);
		this.description = description;
	}

	@Override
	public E getValue() {
		return property.getValue();
	}

	@Override
	public void setValue(Object o) {
		if (clazz.isInstance(o)) {
			property.setValue(clazz.cast(o));
		} else {
			property.setValue(null);
		}
	}

	@Override
	public Optional<ObservableValue<?>> getObservableValue() {
		return Optional.of(property);
	}
	
}
