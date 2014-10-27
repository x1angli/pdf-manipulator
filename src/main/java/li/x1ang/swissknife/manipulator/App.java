package li.x1ang.swissknife.manipulator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdfparser.PDFStreamParser;
import org.apache.pdfbox.pdfwriter.ContentStreamWriter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;
import org.apache.pdfbox.util.PDFOperator;

public class App {
	public static void main(String[] args) {
		App app = new App();
		String[] toRemove = new String[] { "Xiang" };
		String input = "C:\\output1.pdf";
		String output = "C:\\output2.pdf";
		try {
			app.removeWatermark(input, output, toRemove);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private PDDocument doc;

	private void removeWatermark(String input, String output, String[] toRemove) throws IOException {
		PDDocument doc = PDDocument.load(input);
		this.doc = doc;

		List<?> allPages = doc.getDocumentCatalog().getAllPages();

		for (int i = 0; i < allPages.size(); i++) {
			PDPage pageI = (PDPage) allPages.get(i);
			int pageCounter = 0;

			pageCounter += removeTokens(pageI, toRemove);
			pageCounter += removeAnnotations(pageI, toRemove);

			System.out.println("Page " + (i + 1) + ": " + pageCounter);
		}// end of for-loop of pages.

		System.out.println("Saving....");
		try {
			doc.save(output);
		} catch (COSVisitorException e) {
			e.printStackTrace();
		}
		System.out.println("Finished.");

	}

	private int removeTokens(PDPage page, String[] toRemove) throws IOException {

		PDFStreamParser parser = new PDFStreamParser(page.getContents());
		parser.parse();
		List<Object> tokens = parser.getTokens();
		List<Object> newTokens = new ArrayList<Object>();
		int counter = 0;

		String expectedOperator = null;
		for (int i = 0; i < tokens.size(); i++) {
			Object tokenI = tokens.get(i);
			boolean skipTokenJ = false;

			if (expectedOperator == null) {
				if (tokenI instanceof COSString) {
					String tokenJstr = ((COSString) tokenI).getString();
					if (tokenJstr != null) {
						for (String removeStr : toRemove) {
							if (tokenJstr.indexOf(removeStr) >= 0) {
								expectedOperator = "TJ";
								skipTokenJ = true;
								counter++;
								break;
							}
						} // for-loop of toRemove
					}

				}
			} else { // there is expectedOperator
				if (tokenI instanceof PDFOperator) {
					PDFOperator op = (PDFOperator) tokenI;
					String actualOperation  = op.getOperation();
					if (actualOperation.equalsIgnoreCase(expectedOperator)) {
						skipTokenJ = true;
					} else {
						System.err.println("Expected " + expectedOperator + " operator token, but the actual is " + actualOperation);
					}
				} else {
					System.err.println("We are expecting a PDFOperator");
				}
				expectedOperator = null;
			} 
			if (!skipTokenJ) {
				newTokens.add(tokenI);
			}

		} // end of for-loop of tokens
		PDStream newContents = new PDStream(doc);
		ContentStreamWriter writer = new ContentStreamWriter(newContents.createOutputStream());
		writer.writeTokens(newTokens);
		newContents.addCompression();
		page.setContents(newContents);

		return counter;
	}

	private int removeAnnotations(PDPage page, String[] toRemove) throws IOException {
		if (page == null)
			return 0;

		int counter = 0;
		List<PDAnnotation> annotations = page.getAnnotations();
		for (int i = annotations.size() - 1; i >= 0; i--) {
			PDAnnotation annotI = annotations.get(i);
			if (annotI instanceof PDAnnotationLink) {
				PDAnnotationLink linkJ = (PDAnnotationLink) annotI;
				COSBase cosLink = linkJ.getDictionary().getDictionaryObject("A");
				if (cosLink != null && cosLink instanceof COSDictionary) {
					COSDictionary dict = (COSDictionary) cosLink;
					String uriStr = dict.getString(COSName.URI);
					if (uriStr != null) {
						for (String removeStr : toRemove) {
							if (uriStr.indexOf(removeStr) >= 0) {
								annotations.remove(annotI);
								counter++;
							}
						} // for-loop of toRemove
					}
				}
			} // if PDAnnotationLink
		}

		if (counter > 0) {
			page.setAnnotations(annotations);
		}
		return counter;
	}

}
