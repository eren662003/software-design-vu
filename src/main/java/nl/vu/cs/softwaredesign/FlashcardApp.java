package nl.vu.cs.softwaredesign;

import java.util.*;


public class FlashcardApp {
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        DeckManager deckManager = DeckManager.getInstance();
        System.out.println("Flashcard Deck Creation and Study App");

        while (true) {
            System.out.println("\nAvailable actions:");
            System.out.println("1 - Create a new deck");
            System.out.println("2 - Add flashcards to a deck");
            System.out.println("3 - Display a deck");
            System.out.println("4 - Study a deck");
            System.out.println("5 - Search flashcards by tag");
            System.out.println("6 - Exit");
            System.out.print("Choose an action: ");
            String action = scanner.nextLine();

            switch (action) {
                case "1":
                    System.out.print("Enter the name of the new deck: ");
                    String name = scanner.nextLine();
                    deckManager.createDeck(name);
                    break;
                case "2":
                    addFlashcardsToDeck(deckManager);
                    break;
                case "3":
                    displayDeck(deckManager);
                    break;
                case "4":
                    studyDeck(deckManager);
                    break;
                case "5":
                    searchByTag(deckManager);
                    break;
                case "6":
                    System.out.println("Exiting...");
                    return;
                default:
                    System.out.println("Invalid action, please choose again.");
            }
        }
    }

    private static void addFlashcardsToDeck(DeckManager deckManager) {
        if (deckManager.getDecks().isEmpty()) {
            System.out.println("No decks available. Please create a deck first.");
            return;
        }

        deckManager.listDecks();
        System.out.print("Enter the name of the deck to add flashcards to: ");
        String deckName = scanner.nextLine();
        Deck deck = deckManager.getDeck(deckName);
        if (deck == null) {
            System.out.println("Deck not found.");
            return;
        }
        String continueCreating;
        do {
            System.out.print("Enter the question (front) of the flashcard: ");
            String question = scanner.nextLine();
            System.out.print("Enter the answer (back) of the flashcard: ");
            String answer = scanner.nextLine();

            Flashcard flashcard = new Flashcard(question, answer);

            System.out.print("Enter tags for this flashcard (comma-separated, hit enter to skip): ");
            String tagInput = scanner.nextLine();
            Arrays.stream(tagInput.split(","))
                    .map(String::trim)
                    .filter(tag -> !tag.isEmpty())
                    .map(Tag::new)
                    .forEach(flashcard::addTag);

            deck.addFlashcard(flashcard);

            System.out.print("Do you want to add another flashcard to this deck? (yes/no): ");
            continueCreating = scanner.nextLine();
        } while (continueCreating.equalsIgnoreCase("yes"));
    }

    private static void displayDeck(DeckManager deckManager) {
        System.out.println("Available decks:");
        deckManager.listDecks();

        System.out.print("Enter the name of the deck to display: ");
        String deckName = scanner.nextLine();
        Deck deck = deckManager.getDeck(deckName);

        if (deck == null) {
            System.out.println("Deck not found.");
        } else {
            System.out.println("Displaying flashcards in deck '" + deckName + "':");
            deck.displayFlashcards();
        }
    }

    private static void studyDeck(DeckManager deckManager) {
        System.out.print("Enter the name of the deck to study: ");
        String deckName = scanner.nextLine();
        Deck deck = deckManager.getDeck(deckName);
        if (deck == null) {
            System.out.println("Deck not found.");
            return;
        }

        List<Flashcard> flashcards = new ArrayList<>(deck.getFlashcards()); // Clone the list for safe iteration
        if (flashcards.isEmpty()) {
            System.out.println("This deck is empty.");
            return;
        }

        // Create a list to track flashcards for repetition
        List<Flashcard> toRepeat = new LinkedList<>();

        System.out.println("Starting study session...");
        while (!flashcards.isEmpty() || !toRepeat.isEmpty()) {
            Flashcard flashcard = !flashcards.isEmpty() ? flashcards.remove(0) : toRepeat.remove(0);

            System.out.println("Question: " + flashcard.getFront());
            System.out.println("Press enter to reveal the answer...");
            scanner.nextLine();
            System.out.println("Answer: " + flashcard.getBack());

            System.out.print("How well did you know this? (1 - Didn't know, 2 - Partially knew, 3 - Knew well): ");
            int ratingValue = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            // Determine if the flashcard needs to be repeated
            if (ratingValue < 3) {
                toRepeat.add(flashcard);
                System.out.println("You'll see this flashcard again for review.");
            }

            // If the main list is empty, refill it with flashcards to repeat, then clear the repeat list
            if (flashcards.isEmpty() && !toRepeat.isEmpty()) {
                System.out.println("Repeating flashcards for additional review...");
                flashcards.addAll(toRepeat);
                toRepeat.clear();
                Collections.shuffle(flashcards); // Shuffle for varied repetition
            }

            System.out.println();
        }

        System.out.println("Deck study session completed.");
    }



    private static void searchByTag(DeckManager deckManager) {
        System.out.print("Enter a tag to search for flashcards: ");
        Tag searchTag = new Tag(scanner.nextLine());

        System.out.println("Flashcards with tag '" + searchTag + "':");
        deckManager.getDecks().values().forEach(deck -> deck.getFlashcards().stream()
                .filter(flashcard -> flashcard.getTags().contains(searchTag))
                .forEach(System.out::println));
    }
}

class Tag {
    private String name;

    public Tag(String name) {
        this.name = name.trim().toLowerCase();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tag)) return false;
        Tag tag = (Tag) o;
        return Objects.equals(name, tag.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return name;
    }
}

class Rating {
    private int value;

    public Rating(int value) {
        this.value = Math.max(1, Math.min(value, 3));
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        String ratingString;
        switch (value) {
            case 1:
                ratingString = "Didn't know";
                break;
            case 2:
                ratingString = "Partially knew";
                break;
            case 3:
                ratingString = "Knew well";
                break;
            default:
                ratingString = "Unknown rating";
                break;
        }
        return ratingString;
    }
}


class Flashcard {
    private String front;
    private String back;
    private Set<Tag> tags;

    public Flashcard(String front, String back) {
        this.front = front;
        this.back = back;
        this.tags = new HashSet<>();
    }

    public void addTag(Tag tag) {
        tags.add(tag);
    }

    public String getFront() {
        return front;
    }

    public String getBack() {
        return back;
    }

    public Set<Tag> getTags() {
        return tags;
    }

    @Override
    public String toString() {
        return "Flashcard{" +
                "Question='" + front + '\'' +
                ", Answer='" + back + '\'' +
                ", Tags=" + tags +
                '}';
    }
}

class Deck {
    private List<Flashcard> flashcards;

    public Deck() {
        this.flashcards = new ArrayList<>();
    }

    public void addFlashcard(Flashcard flashcard) {
        flashcards.add(flashcard);
    }

    public List<Flashcard> getFlashcards() {
        return new ArrayList<>(flashcards);
    }

    public void displayFlashcards() {
        if (flashcards.isEmpty()) {
            System.out.println("This deck is empty.");
        } else {
            flashcards.forEach(System.out::println);
        }
    }
}

class DeckManager {
    private static DeckManager instance;
    private final Map<String, Deck> decks;

    private DeckManager() {
        decks = new HashMap<>();
    }

    public static synchronized DeckManager getInstance() {
        if (instance == null) {
            instance = new DeckManager();
        }
        return instance;
    }

    public Deck createDeck(String name) {
        Deck newDeck = new Deck();
        decks.put(name, newDeck);
        System.out.println("Deck '" + name + "' created.");
        return newDeck;
    }

    public Deck getDeck(String name) {
        return decks.get(name);
    }

    public void listDecks() {
        if (decks.isEmpty()) {
            System.out.println("No decks available.");
        } else {
            System.out.println("Available decks:");
            decks.keySet().forEach(deckName -> System.out.println("- " + deckName));
        }
    }

    public Map<String, Deck> getDecks() {
        return Collections.unmodifiableMap(decks);
    }
}
