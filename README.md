Compact Collections 
===================

Compact and fast collections optimized for primitive types, implemented in Java.  
On average they use 4-5 times less memory than the default implementations from *java.util*, while being faster to build and query (mostly due to a better utilization of the memory and cache subsystems).

#### Included collections:  

- **VariableIntArray**: compact variable-length integer array (1/4 bytes) using [Group Variant Encoding](http://www.stanford.edu/class/cs276/Jeff-Dean-compression-slides.pdf) and [Delta Encoding](http://en.wikipedia.org/wiki/Delta_encoding), provides fast query at random positions and support for value updating and caching.
- **SparseBitSet**: a sparse representation of a bit array, provides fast query at random positions.
- **IntHashMap**: maps *Integer* -> *Integer*.
- **IntObjectHashMap**: maps *Integer* -> *Object*.
- **IntPairHashMap**: maps *Integer x Integer* -> *Integer*.
- **IntPairObjectHashMap**: maps *Integer x Integer* -> *Object*.
- **VariableIntHashMap**: maps *Integer* -> *Integer*, uses variable-length integers for the values. Requires about 20% less memory than *VariableIntHashMap*, but has slower query time.
- **VariableIntPairHashMap**: maps *Integer x Integer* -> *Integer*, uses variable-length integers for the keys and values. Requires about 35% less memory then *VariableIntPairHashMap*, but has slower query time.
  
  
#### Benchmarks  
  
Below are the results of some simple benchmarks (add values to a hash map, then get them and compute their sum). All tests were done using the 64-bit Server JIT with randomly-generated values starting from the same seed value.  
  
It can be seen that much less memory is required (at least 4 times less), construction is in general much faster, while querying is much faster for *IntHashMap / IntPairHashMap* and slower for *VariableIntArray*.  

![VariableIntArray memory](http://www.gratianlup.com/documents/varint_mem.png)  
![VariableIntArray time](http://www.gratianlup.com/documents/varint_time.png)  
![IntHashMap memory](http://www.gratianlup.com/documents/inthash_mem.png)  
![IntHashMap time](http://www.gratianlup.com/documents/inthash_time.png)  
  
  
Faster construction time can be explained by the reduced stress on the Garbage Collector (no more *Integer* instances are created).  

Faster query time is a result of the compact memory layout, which reduces memory traffic and allows more values to be stored in the CPU cache (this is visible especially when accessing consecutive or nearby memory locations).  
