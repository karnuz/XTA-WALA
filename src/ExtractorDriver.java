import xta.Extractor;
import java.io.IOException;
import java.io.File;

class ExtractorDriver {
	public static void main(String args[]) throws IOException {
		File  f = new File("temp");
		f.mkdir();
		Extractor xtractor = new Extractor(f.toString());
		xtractor.extract(args);
	}
}