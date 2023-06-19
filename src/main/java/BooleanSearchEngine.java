import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public class BooleanSearchEngine implements SearchEngine {

    private Map<String, List<PageEntry>> indexing = new HashMap<>();
    private Set<String> unusefulWords = new HashSet<>();

    public BooleanSearchEngine(File pdfsDir) throws IOException {
        for (File file : requireNonNull(pdfsDir.listFiles())) {
            PdfDocument doc = new PdfDocument(new PdfReader(file.getAbsolutePath()));
            for (int i = 1; i <= doc.getNumberOfPages(); i++) {
                String text = PdfTextExtractor.getTextFromPage(doc.getPage(i));
                List<String> words = Arrays.asList(text.split("\\P{IsAlphabetic}+"));
                Map<String, Integer> freqs = new HashMap<>();
                for (String word : words) {
                    if (word.isEmpty() || unusefulWords.contains(word)) continue;
                    String lowerCaseWord = word.toLowerCase();
                    freqs.put(lowerCaseWord, freqs.getOrDefault(lowerCaseWord, 0) + 1);
                }
                for (Map.Entry<String, Integer> entry : freqs.entrySet()) {
                    String word = entry.getKey();
                    Integer count = entry.getValue();
                    PageEntry pageEntry = new PageEntry(file.getName(), i, count);
                    List<PageEntry> pageEntryList = indexing.getOrDefault(word, new ArrayList<>());
                    pageEntryList.add(pageEntry);
                    Collections.sort(pageEntryList);
                    indexing.put(word, pageEntryList);
                }
            }
        }
    }

    private List<PageEntry> combinePageEntries(List<PageEntry> list1, List<PageEntry> list2) {
        List<PageEntry> combinedList = new ArrayList<>();
        Iterator<PageEntry> list1Iterator = list1.iterator();
        Iterator<PageEntry> list2Iterator = list2.iterator();
        PageEntry currentEntry1 = null;
        PageEntry currentEntry2 = null;
        while (list1Iterator.hasNext() && list2Iterator.hasNext()) {
            if (currentEntry1 == null) {
                currentEntry1 = list1Iterator.next();
            }
            if (currentEntry2 == null) {
                currentEntry2 = list2Iterator.next();
            }
            if (currentEntry1.compareTo(currentEntry2) == 0) {
                combinedList.add(new PageEntry(currentEntry1.getPdfName(), currentEntry1.getPage(), currentEntry1.getCount() + currentEntry2.getCount()));
                currentEntry1 = null;
                currentEntry2 = null;
            } else if (currentEntry1.compareTo(currentEntry2) < 0) {
                combinedList.add(currentEntry1);
                currentEntry1 = null;
            } else {
                combinedList.add(currentEntry2);
                currentEntry2 = null;
            }
        }
        while (list1Iterator.hasNext()) {
            currentEntry1 = list1Iterator.next();
            combinedList.add(currentEntry1);
        }
        while (list2Iterator.hasNext()) {
            currentEntry2 = list2Iterator.next();
            combinedList.add(currentEntry2);
        }
        return combinedList;
    }

    @Override
    public List<PageEntry> search(String text) {
        Collectors Collectors = null;
        List<String> cleanWords = Arrays.asList(text.split("\\P{IsAlphabetic}+")).stream()
                .map(String::toLowerCase)
                .filter(word -> !word.isEmpty() && !unusefulWords.contains(word))
                .collect(Collectors.toList());
        if (cleanWords.isEmpty()) {
            return new ArrayList<>();
        }
        List<PageEntry> result = indexing.getOrDefault(cleanWords.get(0), new ArrayList<>());
        for (int i = 1; i < cleanWords.size(); i++) {
            String word = cleanWords.get(i);
            result = combinePageEntries(result, indexing.getOrDefault(word, new ArrayList<>()));
        }
        return result;
    }

    public void readUnusefulWords(String fileName) {
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                unusefulWords.add(line.toLowerCase());
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}