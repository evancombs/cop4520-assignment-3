# cop4520-assignment-3
## Problem 1: The Birthday Presents Party
Compile with:
  ```"javac MinotaurGifts.java"```

Run with: ```"java MinotaurGifts"```

Runs with 4 servants
Outputs to console when complete, with time of execution taken

Several things could have gone wrong, depending on how the servants handled
accessing the gift chain.
* When adding a new present to the chain, a servant could have accidentally attached
the chain to a present another servant is in the midst of removing, losing the presents
past that point!
* When removing a present, another servant could have tried to add a present after the
removed present, losing that present they just added!
These are just a few examples; even more could have gone wrong, but it all depends
on how these issues were handled.

In developing a concurrent linked list to solve the Minotaur's problem, we must
take a look at the requirements he requested. Notably:
* It must have add(), remove(), and contains() functionality
* The servants should not take breaks
* The servants should alternate adding and removing presents from the chain
* And as evidenced by the Minotaur's initial problem, the list must be able to
robustly handle multiple adders and deleters.
Given this, I have chosen to implement a modified version of the LockFreeList from
The Art of Multiprocessor Programming as the solution to the Minotaur's problem.
It provides lock-free add() and remove(), and wait-free contains().

In my implementation, I have made some changes to adapt it to the scenario, but
largely maintained the structure of the textbooks description. One such change is
a new method to remove from the start of the chain; the servants do this unless given
explicit instructions to remove a particular gift. This allows us to avoid maintaining
another concurrent structure of gifts to be removed.


## Problem 2: Atmospheric Temperature Reading Module
Compile with:
  ```"javac RoverTemperature.java"```

Run with: ```"java MinotaurGifts"```

Outputs to console with metrics of interest.

To approach this problem, we must develop both a method to simulate / track time,
handle the temperature reading at set intervals, and compile a report when
sufficient time has elapsed.

To handle time, each thread runs in a infinite loop, where one iteration is considered
one minute passing; thus, if each thread takes a reading once per loop, we simulate
a reading every minute. We elect one thread to be the "leader", which is responsible
for tracking when one hour has elapsed, and compiling a report at that time. We
perform this report making atomically.

To handle information about temperature readings, we will create a structure that
holds some metadata about the reading; namely, the sensor that recorded it and
the time at which it was recorded. This is important, because sensors may continue to
record data while the leader is compiling a report; we need to discard this data,
because it is *not* from the hour of interest.

When we wish to compile a report, we take a "snapshot" of the current log by
converting it to local array held in the leader's memory. We discard any times
that may have been recorded after the report begins compilation, then do various
operations on it to extract the metrics of interest.

Finally, to handle the log of readings, we use java's ConcurrentLinkedQueue.
ConcurrentLinkedQueue is already highly optimized and wait-free for non-iterator
functionality. The functionality we require is adding and retrieving the data as
an array, making it a good choice for this application.
