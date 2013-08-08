ImgLib2 is a general-purpose, multidimensional image processing library.

It provides an interface-driven design that supports numeric and
non-numeric data types (8-bit unsigned integer, 32-bit floating point,
etc.) in an extensible way. It implements several data sources and
sample organizations, including one single primitive array, one array
per plane, N-dimensional array "cells" cached to and from disk on
demand, and planes read on demand from disk.


Benefits
--------

1. By avoiding unnecessarily complex syntax (such as nested loops) ImgLib2
   allows developers to concentrate on the essence of the algorithm.

2. By being conciser, ImgLib2 makes it much harder to write buggy code.

3. ImgLib2 is dimension-independent. That means that you usually express your
   code in a way that can be applied to 2-, 3- or even 100-dimensional data.

4. ImgLib2 has no limit on channels. You can have a fine-grained spectrum for
   every single pixel, if your hardware allows for that.

5. ImgLib2 is actually not limited to images; e.g., we have examples working on
   RNA sequences.

6. ImgLib2 provides transparent data access. The algorithm does not need to
   know that it is working on a virtual stack, and the data can actually be
   generated on the fly. Think about a fractal and being able to zoom in
   indefinitely; this is an image that you can use with any ImgLib algorithm.

7. ImgLib2 makes it an ultra-cheap operation to work on sections of images.
   There is no need to copy data around.

8. ImgLib2 is so self-contained that it could serve as the underlying data
   handling library for every Java-based project.


Applications
------------

* ImgLib2 provides the
  [core data model for ImageJ2](http://developer.imagej.net/imglib).
* ImgLib2 is bundled with the [Fiji](http://fiji.sc/) distribution of ImageJ.
* The [SCIFIO](http://loci.wisc.edu/software/scifio) library utilizes
  ImgLib2's N-dimensional image API.
* ImgLib2 is a key component of the
  [SciJava software initiative](http://scijava.org/).


Resources
---------

* [ImgLib2 source code on GitHub](https://github.com/imagej/imglib).
* [Documentation on how to use ImgLib2](http://imglib2.net/).
* [ImgLib2 Examples](http://fiji.sc/ImgLib2_Examples)
* [Online Javadoc](http://jenkins.imagej.net/job/ImgLib-daily/javadoc/).
* [Performance benchmarks](http://developer.imagej.net/imglib-benchmarks).


Building the source code
------------------------

You can build the source from the command line using Maven:

    mvn

You can also import the source into Eclipse using the m2e plugin.
Download Eclipse IDE for Java Developers (3.7 Indigo or later), which
comes with m2e preinstalled. Then run:

    File > Import > Existing Maven Projects

Select the toplevel folder of your ImgLib working copy, and Eclipse will
find all the ImgLib projects.

Both NetBeans and IntelliJ IDEA also have built-in support for Maven
projects.


ImgLib1
-------

The previous incarnation of the library, known as ImgLib1, is still available
as [part of
Fiji](https://github.com/fiji/fiji/tree/master/src-plugins/legacy-imglib1).
However, we strongly encourage developers to use ImgLib2 instead, and migrate
existing ImgLib1 programs to ImgLib2 whenever possible.

