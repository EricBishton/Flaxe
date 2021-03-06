Flaxe
=====

An AS3 to Haxe conversion script.

AS3Haxe is an excellent tool that does more than this
program does.  However, it also can introduce subtle bugs
in the code that cannot be detected until run-time.  When
this program was designed, the intent was to do most of 
the boilerplate conversion of AS3 sources to Haxe sources,
and leave logic conversions to humans instead.  While this
project doesn't do as much, it only does things that cannot
introduce errors.  (At least that's the hope!)

For the most part, this program is a simple pattern
replacement tool.  Things like `:Number` => `:Float`, and
`Vector.<Int>` => `Vector<Int>;`.  For certain things, it
will introduce a new import statement.  You can find and 
extend the patterns in the Patterns.java file.  If you
create some useful new patterns, please submit a pull
request or open an issue.

How to contact the maintainer:
+ Bugs or other issues: https://github.com/EricBishton/Flaxe/Issues
+ Releases: https://github.com/EricBishton/Flaxe/Releases
+ Source code: https://github.com/EricBishton/Flaxe

Usage
-----

**Pre-requisites:**

This tool is implemented in Java.  Therefore, you must have
Java 8 or later installed.

**Running the program:**

Usage: `flaxe.jar <base-src-folder> <base-dest-folder> <folder> <action>`

Where: 
- `<base-src-folder>` is the root directory of the project (or sub-project)
to convert.
- `<base-dest-folder>` is the root directory where the conversion results
should be placed.
- `<folder>` is the directory to convert.  All `.as` source files inside of this
directory will be processed.
- `<action>` is one of `copy`, `convert`:
  - `copy` means to rename source files and copy them to the destination
folder, but do not do any conversion of the contents.
  - `convert` means to rename source files and copy them to the destination
folder while replacing recognized patterns with Haxe replacements.

The program will refuse to run if the `<base-dest-folder>` exists prior
to start.  There is currently no functionality to merge directories or detect file collisions.


**Doing a conversion run:**

The general command line will be similar to this:

`java -jar flaxe.jar D:\Sandbox\MyProject\Source\FlashX\ D:\Sandbox\MyProject\Source\Haxe\ Source copy`

from which we build the full src-folder:
  `D:\Sandbox\MyProject\Source\FlashX\Source`

and full dest-folder:
  `D:\Sandbox\MyProject\Haxe\Source`

The idea is that it's easier to modify the actual module being 
converted by replacing "Source" without having to keep updating two 
paths which are unlikely to change.  This should make it easier to
create a wrapper script for your specific project.


Building:
---------

This project is built using gradle.  If you have gradle installed, 
you may build using the `gradlew assemble`.  The output jar will be
under your project directory in the `build\libs\flaxe.jar` file.

