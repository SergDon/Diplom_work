public class PageEntry implements Comparable<PageEntry> {
    private final String pdfName;
    private final int page;
    private final int count;
    public PageEntry(String pdfName, int page, int count) {
        this.pdfName = pdfName;
        this.page = page;
        this.count = count;
    }
    public String getPdfName() {
        return pdfName;
    }

    public int getPage() {
        return page;
    }

    public int getCount() {
        return count;
    }

    @Override
    public int compareTo(PageEntry o) {
        if (this.count != o.count) {
            return Integer.compare(o.count, this.count);
        }

        if (this.pdfName.compareTo(o.pdfName) != 0) {
            return this.pdfName.compareTo(o.pdfName);
        }

        return Integer.compare(this.page, o.page);
    }

    @Override
    public String toString() {
        return "{\n" +
                "PDF Name: " + pdfName +
                "\nPage Number: " + page +
                "\nCount: " + count + "\n"+
                "}" + "\n";
    }
}