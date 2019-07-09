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
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileHelper {
	public static List<File> generateFileList(FileFilter filter, File folder, boolean recurse) {
		File[] children = folder.listFiles(filter) ;
		ArrayList<File> result = new ArrayList<File>() ;

		if (children == null)
			return result ;

		for (int i = 0 ; i < children.length ; i++) {
			if (children[i].isFile())
				result.add(children[i]) ;
			if (recurse && children[i].isDirectory()) {
				List<File> subResults = generateFileList(filter, children[i], recurse) ;
				result.addAll(subResults) ;
			}
		}

		return result ;
	}

	public static FileFilter createFileFilter(final String extension) {
		FileFilter filter = new FileFilter() {
			@Override
			public boolean accept(File f) {
				if (f.isFile() && f.getName().toLowerCase().indexOf(extension) != -1)
					return true ;

				// Include directories so we can do recursive searches
				// Don't search into .svn folder
				if (f.isDirectory() && !f.getName().startsWith("."))
					return true ;

				return false ;
			}
		} ;
		return filter ;
	}

	public static File[] getSubFolders(File folder) {
		FileFilter filter = new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return (pathname.isDirectory()) ;
			}
		} ;

		File[] children = folder.listFiles(filter) ;
		return children ;
	}

	public static long recursiveDelete(File folder, FileFilter filter, long deletedSoFar, long timeSinceLastModifiedMs) {
		long now = System.currentTimeMillis() ;

		List<File> files = generateFileList(filter, folder, false) ;
		for (File file : files) {
			long lastModified = file.lastModified() ;
			long elapsed = (now - lastModified) ;
			if (elapsed >= timeSinceLastModifiedMs) {
				boolean gone = file.delete() ;

				if (gone)
					deletedSoFar++ ;
			}
		}

		File[] subFolders = getSubFolders(folder) ;

		if (subFolders != null) {
			for (File subFolder : subFolders) {
				// Pass 0 in as the deletedSoFar to prevent doubling of the deletedSoFar value
				deletedSoFar += recursiveDelete(subFolder, filter, 0, timeSinceLastModifiedMs) ;
			}
		}

		// This will only succeed if we deleted all files in the folder successfully
		folder.delete() ;

		return deletedSoFar ;
	}

	public static String getRelativePath(File rootFolder, File path) throws IOException {
		String rootPath = rootFolder.getCanonicalPath() ;
		String subPath = path.getCanonicalPath() ;

		String removeRoot = subPath.replace(rootPath, "") ;

		if (removeRoot.length() == subPath.length()) {
			throw new IllegalArgumentException("Could not remove root " + rootPath + " from path " + subPath) ;
		}

		if (removeRoot.startsWith("/") || removeRoot.startsWith("\\"))
			removeRoot = removeRoot.substring(1) ;

		return removeRoot;
	}

	public static String getExtension(String filename) {
		if(filename == null) {
			return null;
		} else {
			int index = filename.lastIndexOf(".") ;
			return index == -1?"":filename.substring(index + 1);
		}
	}

	public static String removeExtension(String filename) {
		if(filename == null) {
			return null;
		} else {
			int index = filename.lastIndexOf(".") ;
			return index == -1?filename:filename.substring(0, index);
		}
	}

}
