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

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Flaxe {
	public enum Action { kCopy, kConvert} ;

	public static void main(String[] args) throws IOException {
		System.out.println("flaxe v1.0");

		if (args.length < 4) {
			StringBuilder usage = new StringBuilder();
			usage.append("Usage: flaxe.jar <base-src-folder> <base-dest-folder> <folder> <action>\n")
					.append("Where:\n")
					.append("- `<base-src-folder>` is the root directory of the project (or sub-project)\n")
					.append("   to convert.\n")
					.append("- `<base-dest-folder>` is the root directory where the conversion results\n")
					.append("   should be placed.\n")
					.append("- `<folder>` is the directory to convert.  All `.as` source files inside of\n")
					.append("   this directory will be processed.\n")
					.append("- `<action>` is one of `copy`, `convert`:\n")
					.append("  + `copy` means to rename source files and copy them to the destination\n")
					.append("    folder, but do not do any conversion of the contents.\n")
					.append("  + `convert` means to rename source files and copy them to the destination\n")
					.append("    folder while replacing recognized patterns with Haxe replacements.\n")
					.append('\n')
					.append("The program will refuse to run if the `<base-dest-folder>` exists prior\n")
					.append("to start.  There is currently no functionality to merge directories or \n")
					.append("detect file collisions.\n")
					.append('\n')
					.append("Typical usage:\n")
					.append("   `java -jar flaxe.jar D:\\MyProject\\FlashX\\ D:\\MyProject\\Haxe\\ Source copy`\n")
					.append("See README.md that accompanied this program for more information.\n");

			System.err.println(usage);
			System.exit(1);
		}

		// Pattern is
		// java -jar flash-preprocessor.jar D:\fp\Main\Source\FlashX\SharedX D:\fp\Main\Source\Haxe\Research PlaceNavigation
		// from which we build the full src is:
		// D:\fp\Main\Source\FlashX\SharedX\PlaceNavigation
		// and full dest is:
		// D:\fp\Main\Source\Haxe\Research\PlaceNavigation

		// The idea is that then it's easier to modify the actual module being converted
		// by replacing "PlaceNavigation" without having to keep updating 2 paths which are unlikely to change.

		File baseSrcFolder = new File(args[0]) ;
		File baseDestFolder = new File(args[1]) ;
		String folder = args[2] ;

		String act = args[3] ;
		Action action = null ;

		if (act.equalsIgnoreCase("copy"))
			action = Action.kCopy ;
		if (act.equalsIgnoreCase("convert"))
			action = Action.kConvert;

		if (action == null) {
			System.err.println("Last parameter must be one of: 'copy', 'convert'; not " + act) ;
			System.exit(1) ;
		}

		File srcFolder = new File(baseSrcFolder, folder) ;
		File destFolder = new File(baseDestFolder, folder) ;

		if (destFolder.exists()) {
			System.err.println("Refusing to overwrite destination folder '" + destFolder + "'.\n" +
					           "Please remove it or provide another base destination folder.") ;
			System.exit(2) ;
		}

		Flaxe app = new Flaxe() ;
		app.preProcess(srcFolder, destFolder, action) ;
		System.out.println("Finished.");
	}

	private String changeExtension(String original) {
		return original.replace(".as", ".hx") ;
	}

	private void preProcess(File srcFolder, File destFolder, Action action) throws IOException {
		FileFilter filter = FileHelper.createFileFilter(".as") ;
		List<File> srcFiles = FileHelper.generateFileList(filter, srcFolder, true) ;

		System.out.println("Deleting " + destFolder.getCanonicalPath()) ;
		destFolder.mkdirs() ;
		FileHelper.recursiveDelete(destFolder, filter, 0, Long.MAX_VALUE) ;

		for (File src : srcFiles) {
			String relativePath = FileHelper.getRelativePath(srcFolder, src) ;
			String modifiedExt = changeExtension(relativePath) ;
			File dest = new File(destFolder, modifiedExt) ;

			System.out.println(action + " "  + src + " to " + dest) ;
			convert(src, dest, action) ;
		}
	}

	public void convert(File inputFile, File outputFile, Action action) throws IOException {
		List<String> lines = new ArrayList<>() ;
		String line ;

		// We read in the entire input file
		// so that we can examine any part, not just limited to line by line analysis
		// Using try-with-resource to catch and close
		try (BufferedReader input = new BufferedReader(new FileReader(inputFile))) {
			while ((line = input.readLine()) != null) {
				lines.add(line);
			}
		}

		ConvertFile converter = new ConvertFile(inputFile) ;
		List<String> modifiedLines = converter.convertFileContents(lines, action) ;

		// Write out the new file
		outputFile.getParentFile().mkdirs() ;
		try (PrintWriter output = new PrintWriter(new FileWriter(outputFile))) {
			for (String outLine : modifiedLines) {
				output.println(outLine) ;
			}
		};
	}

}
