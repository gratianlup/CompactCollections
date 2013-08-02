Compact Collections 
===================

#### Memory consumption and speed benchmarks will be released in the near future.  

Compact and fast collections optimized for primitive types, implemented in Java.  
On average they use 4-5 times less memory than the default implementations from *java.util*, while being faster to build and query (due to a better utilization of the memory and cache subsystems).

#### Included collections:  

- **VariableIntArray**: compact variable-length integer array (1/4 bytes) using [Group Variant Encoding](http://www.stanford.edu/class/cs276/Jeff-Dean-compression-slides.pdf) and [Delta Encoding](http://en.wikipedia.org/wiki/Delta_encoding), provides fast query at random positions and support for value updating and caching.
- **SparseBitSet**: a sparse representation of a bit array, provides fast query at random positions.
- **IntHashMap**: maps *Integer* -> *Integer*.
- **IntObjectHashMap**: maps *Integer* -> *Object*.
- **IntPairHashMap**: maps *Integer x Integer* -> *Integer*.
- **IntPairObjectHashMap**: maps *Integer x Integer* -> *Object*.

