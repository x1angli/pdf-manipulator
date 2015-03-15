package li.x1ang.swissknife.manipulator;

import java.io.IOException;

import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.common.PDPageLabelRange;
import org.apache.pdfbox.pdmodel.common.PDPageLabels;

public class PageRangeLabelApp extends App {
	public static void main(String[] args) {
		PageRangeLabelApp app = new PageRangeLabelApp();
		String input = "C:/Users/Dave/Pictures/ingest/Road Games.pdf";
		String output = input + ".reseq.pdf";
		try {
			app.replacePageRangeLabelsStartingAt(input, output, 134, "fred");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Replace existing labels with Decimal numbered labels starting at physical
	 * page 0 with the label startPage
	 * 
	 * NOTE: Replaces any existing labels!
	 * 
	 * @see PDF Specification 1.7, section 8.3.1
	 * 
	 * @param input
	 *            original PDF file.
	 * @param output
	 *            renumbered PDF file with original labels removed.
	 * @param startPage
	 *            int value for the first page, numbered sequentially to follow.
	 * @param prefix
	 *            A text value to be prepended to any numbering. Trailing space
	 *            must be supplied explicitly (is not automatic).
	 * @throws IOException
	 *             bad file access, memory etc.
	 */
	private void replacePageRangeLabelsStartingAt(String input, String output, int startPage, String prefix) throws IOException {
		PDDocument doc = PDDocument.load(input);
		this.doc = doc;

		PDDocumentCatalog catalog = doc.getDocumentCatalog(); catalog.getPageLabels();
		PDPageLabels labels = new PDPageLabels(doc);
		PDPageLabelRange item = new PDPageLabelRange();
		if (prefix != null) {
			item.setPrefix(prefix);
		}
		item.setStart(startPage);
		item.setStyle(PDPageLabelRange.STYLE_DECIMAL);
		labels.setLabelItem(0, item);
		catalog.setPageLabels(labels);
		
		System.out.println("Saving....");
		try {
			doc.save(output);
		} catch (COSVisitorException e) {
			e.printStackTrace();
		}
		System.out.println("Finished.");

	}

}
