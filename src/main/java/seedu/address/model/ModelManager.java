package seedu.address.model;

import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import seedu.address.events.model.LocalModelChangedEvent;
import seedu.address.exceptions.DuplicateTagException;
import seedu.address.main.ComponentManager;
import seedu.address.model.datatypes.AddressBook;
import seedu.address.model.datatypes.ReadOnlyAddressBook;
import seedu.address.model.person.Person;
import seedu.address.model.person.ReadOnlyPerson;
import seedu.address.model.person.UniquePersonList;
import seedu.address.model.person.UniquePersonList.PersonNotFoundException;
import seedu.address.parser.expr.Expr;
import seedu.address.util.Config;
import seedu.address.util.LoggerManager;
import seedu.address.util.collections.UnmodifiableObservableList;

import java.util.List;
import java.util.logging.Logger;

/**
 * Represents the in-memory model of the address book data.
 * All changes to any model should be synchronized.
 */
public class ModelManager extends ComponentManager implements ReadOnlyAddressBook {
    private static final Logger logger = LoggerManager.getLogger(ModelManager.class);

    private final AddressBook backingModel;
    private final FilteredList<Person> filteredPersons;

    public static final int GRACE_PERIOD_DURATION = 3;

    /**
     * Initializes a ModelManager with the given AddressBook
     * AddressBook and its variables should not be null
     */
    public ModelManager(AddressBook src, Config config) {
        super();
        if (src == null) {
            logger.severe("Attempted to initialize with a null AddressBook");
            assert false;
        }
        logger.fine("Initializing with address book: " + src);

        backingModel = new AddressBook(src);
        filteredPersons = new FilteredList<>(backingModel.getPersons());
    }

    public ModelManager(Config config) {
        this(new AddressBook(), config);
    }

    public ReadOnlyAddressBook getDefaultAddressBook() {
        return new AddressBook();
    }

    /**
     * Clears existing backing model and replaces with the provided new data.
     */
    public void resetData(ReadOnlyAddressBook newData) {
        backingModel.resetData(newData);
    }

//// EXPOSING MODEL

    @Override
    public UniqueTagList getUniqueTagList() {
        return backingModel.getUniqueTagList();
    }

    @Override
    public UniquePersonList getUniquePersonList() {
        return backingModel.getUniquePersonList();
    }

    @Override
    public List<ReadOnlyPerson> getPersonList() {
        return backingModel.getPersonList();
    }

    @Override
    public List<Tag> getTagList() {
        return backingModel.getTagList();
    }

    @Override
    public UnmodifiableObservableList<ReadOnlyPerson> getPersonsAsReadOnlyObservableList() {
        return new UnmodifiableObservableList<>(filteredPersons);
    }


    @Override
    public UnmodifiableObservableList<Tag> getTagsAsReadOnlyObservableList() {
        return backingModel.getTagsAsReadOnlyObservableList();
    }

    /**
     * @return reference to the tags list inside backing model
     */
    private ObservableList<Tag> backingTagList() {
        return backingModel.getTags();
    }

//// UI COMMANDS

    public void updateBackingStorage() {
        raise(new LocalModelChangedEvent(backingModel));
    }

    /**
     * Deletes a person.
     */
    public synchronized void deletePerson(ReadOnlyPerson target) throws PersonNotFoundException {
        backingModel.removePerson(target);
        updateBackingStorage();
    }

//// CREATE

    /**
     * Adds a tag to the model
     * @param tagToAdd
     * @throws DuplicateTagException when this operation would cause duplicates
     */
    public synchronized void addTagToBackingModel(Tag tagToAdd) throws DuplicateTagException {
        if (backingTagList().contains(tagToAdd)) {
            throw new DuplicateTagException(tagToAdd);
        }
        backingTagList().add(tagToAdd);
    }

    public synchronized void addPerson(Person person) throws UniquePersonList.DuplicatePersonException {
        backingModel.addPerson(person);
        clearListFilter();
        updateBackingStorage();
    }

    public void clearListFilter() {
        filteredPersons.setPredicate(null);
    }

//// DELETE

    /**
     * Deletes the tag from the model.
     * @param tagToDelete
     * @return true if there was a successful removal
     */
    public synchronized boolean deleteTag(Tag tagToDelete) {
        return backingTagList().remove(tagToDelete);
    }

    public void filterList(Expr expr) {
        filteredPersons.setPredicate(expr::satisfies);
    }

}
