# node_build 

This directory hosts several
files important for the node build
* copysources.ts copies the sources from the node_modules folder into the sources folder
(key requirement we host the sourced within myfaces)
* copyfiles.ts copies the generated files into the maven build structure
* remap.ts is a helper which enables source maps within the faces namespace context (aka changes the map entries
so that the resource loader can reference the correct location of the mapping file)