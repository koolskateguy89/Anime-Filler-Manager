package afm.database;

import java.util.Collection;
import java.util.Set;

import javax.annotation.Nonnull;

import afm.anime.Anime;

// the functions in MyList & ToWatch
public interface Funcs {
	void init();

	void addSilent(Anime anime);

	void add(Anime anime);

	void addAll(Collection<Anime>col);

	void remove(Anime anime);

	boolean contains(@Nonnull Anime anime);

	int size();

	void clear();

	Set<Anime> values();

	Set<Anime> getAdded();

	String getRemovedSQL();
}
