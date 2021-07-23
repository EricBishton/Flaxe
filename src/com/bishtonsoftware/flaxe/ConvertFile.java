/*
 * Copyright (c) 2019 Bishton Software Solutions (www.bishtonsoftware.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bishtonsoftware.flaxe;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ConvertFile {
	private final File m_file ;
	private boolean m_DetailedLogging ;
	private final Patterns m_patterns = new Patterns() ;

	public ConvertFile(File srcFile) {
		m_file = srcFile ;
		m_DetailedLogging = isDetailedOutput(srcFile) ;
	}

	private boolean isDetailedOutput(File srcFile) {
		return (srcFile.getAbsolutePath().contains("src3\\Comments.as")) ;
	}

	public void log(String output) {
		if (m_DetailedLogging) {
			System.out.println(output);
		}
	}

	private String convertLine(String line, Flaxe.Action action) {
		String modifiedLine = line ;
		for (Pattern pattern : m_patterns.getPatterns()) {
			modifiedLine = modifiedLine.replace(pattern.getFrom(), pattern.getTo()) ;
		}

		return modifiedLine ;
	}

	/*
	 Convert
	 from
	 package foo.bar.baz
	 {
	 }
	 to
	 package foo.bar.baz;
	*/
	private List<String> fixPackage(List<String> lines, Flaxe.Action action) {
		List<String> modifiedLines = new ArrayList<>(lines.size()) ;

		boolean foundPackage = false ;
		boolean foundOpen = false ;
		int lastClose = -1 ;
		for (int i = 0 ; i < lines.size() ; i++) {
			String line = lines.get(i);
			String modifiedLine = line ;

			if (line.contains("package")) {
				foundPackage = true ;

				// Allow for { on same line as package
				if (line.contains("{")) {
					foundOpen = true;
					modifiedLine = line.substring(0, line.indexOf("{")) ;
				}

				//System.out.println("Fixed package " + modifiedLine) ;

				modifiedLine = modifiedLine + ";" ;	// Add semi for Haxe
			}

			if (foundPackage) {
				// Remove first {
				if (!foundOpen && line.contains("{")) {
					modifiedLine = "";
					foundOpen = true;
				}

				// Remove last line with }
				if (line.contains("}")) {
					lastClose = i;
				}
			}

			modifiedLines.add(modifiedLine) ;
		}

		// Take out the final } we found
		if (foundPackage && lastClose != -1) {
			modifiedLines.remove(lastClose) ;
		}

		return modifiedLines ;
	}

	/* Convert from:
		 public function Foo(     where Foo is name of the file
	   to
		 public function new(
	*/
	private List<String> fixConstructor(List<String> lines, Flaxe.Action action) {
		String className = FileHelper.removeExtension(m_file.getName()) ;

		// Look for Foo(
		String target = className + "(" ;

		List<String> modifiedLines = new ArrayList<>(lines.size()) ;

		for (String line : lines) {
			String modifiedLine = line ;

			if (line.contains("public") && line.contains("function") && line.contains(target)) {
				modifiedLine = modifiedLine.replaceFirst(className, "new") ;
				//System.out.println("Fixed constructor " + modifiedLine) ;
			}

			modifiedLines.add(modifiedLine) ;
		}

		return modifiedLines ;
	}

	/**
	 * If we use a Vector add
	 * import openfl.Vector;
	 * to the list of imports
	 */
	private List<String> injectImport(List<String> lines, Flaxe.Action action) {
		boolean containsVector = false ;
		int firstImport = -1 ;

		for (int i = 0 ; i < lines.size() ; i++) {
			String line = lines.get(i) ;
			if (line.contains("Vector"))
				containsVector = true ;
			if (firstImport == -1 && line.contains("import"))
				firstImport = i ;
		}

		if (containsVector && firstImport != -1) {
			lines.add(firstImport, "import openfl.Vector;") ;
		}

		return lines ;
	}

	public List<String> convertFileContents(List<String> lines, Flaxe.Action action) throws IOException {
		// If we're just copying the file, don't modify anything
		if (action == Flaxe.Action.kRename)
			return lines ;

		lines = fixConstructor(lines, action) ;
		lines = fixPackage(lines, action) ;
		lines = injectImport(lines, action) ;

		List<String> modifiedLines = new ArrayList<>(lines.size()) ;

		for (int i = 0 ; i < lines.size() ; i++) {
			String line = lines.get(i) ;

			String modifiedLine = convertLine(line, action) ;
			modifiedLines.add(modifiedLine) ;
		}

		return modifiedLines ;
	}
}
