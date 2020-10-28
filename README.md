# Software Engineering Notebook

This repository is an Asciidoctor notebook of essays and example programs. The compiled HTML document may be browsed [here](https://tomboyo.github.io/software-engineering-notebook/).

# Build

To build locally, use the `build` script located at the root of the project. It
will output a compiled HTML document under the target/ directory.

# Github Pages

The gh-pages branch holds the current build of the repository, making it
available via [github.io](https://tomboyo.github.io/software-engineering-notebook/). A github action automatically compiles and updates the gh-pages branch in response to commits and merge requests. Since gh-pages is a compilation artifact, the branch does not have history; every update replaces the branch completely.
